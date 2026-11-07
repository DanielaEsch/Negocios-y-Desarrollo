package mx.unam.buzz.rutinas.service.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.CommonErrorCode;
import mx.unam.buzz.rutinas.common.constant.UsuarioConstant;
import mx.unam.buzz.rutinas.common.constant.UsuarioErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import mx.unam.buzz.rutinas.common.security.Alcance;
import mx.unam.buzz.rutinas.common.security.UsuarioSesionVO;
import mx.unam.buzz.rutinas.common.util.ValidationUtil;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.usuario.AreaResumenVO;
import mx.unam.buzz.rutinas.common.vo.usuario.CreateUsuarioRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.FindListUsuarioRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UpdateAreasRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UpdatePerfilRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UsuarioAdminVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UsuarioResponseVO;
import mx.unam.buzz.rutinas.dominio.usuario.CifradorContrasena;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import mx.unam.buzz.rutinas.dominio.usuario.Usuario;
import mx.unam.buzz.rutinas.modelo.entity.UsuarioAreaDO;
import mx.unam.buzz.rutinas.modelo.entity.UsuarioDO;
import mx.unam.buzz.rutinas.persistence.IAreaRepository;
import mx.unam.buzz.rutinas.persistence.IUsuarioAreaRepository;
import mx.unam.buzz.rutinas.persistence.IUsuarioRepository;
import mx.unam.buzz.rutinas.service.IUsuarioService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Usuarios y sus areas. Las reglas (consentimiento, hash, promocion) son del
 * dominio: aqui se rehidrata el Usuario, se le aplica la regla y se guarda.
 */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private final IUsuarioRepository usuarioRepository;
    private final IUsuarioAreaRepository usuarioAreaRepository;
    private final IAreaRepository areaRepository;
    private final CifradorContrasena cifrador;
    private final ConsultaNombres nombres;

    @Override
    @Transactional(readOnly = true)
    public SingleResponseVO<UsuarioResponseVO> findMe(RequestVO<Void> request) {
        return SingleResponseVO.ok(toResponse(findActive(request.getSesion().getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseVO<UsuarioResponseVO> findList(RequestVO<FindListUsuarioRequestVO> request) {
        UsuarioSesionVO sesion = request.getSesion();
        FindListUsuarioRequestVO filters = request.getParameters() != null
                ? request.getParameters() : new FindListUsuarioRequestVO();
        ValidationUtil.validate(!UsuarioConstant.ORDER_FIELDS.contains(request.getOrderBy()),
                CommonErrorCode.INVALID_REQUEST.getCode(), HttpStatus.BAD_REQUEST);
        Sort sort = Sort.by(Sort.Direction.fromOptionalString(request.getOrderType()).orElse(Sort.Direction.ASC),
                request.getOrderBy());

        // El supervisor solo ve a la gente de sus areas; el administrador, a todos
        Page<UsuarioDO> page = usuarioRepository.findList(
                StringUtils.defaultString(ValidationUtil.normalize(filters.getNombre(), UsuarioConstant.MAX_SIZE_NOMBRE)),
                StringUtils.defaultString(ValidationUtil.normalize(filters.getPerfil(), 20)),
                Alcance.sinFiltro(sesion, null), Alcance.areas(sesion, null),
                PageRequest.of(request.getPage(), request.getSize(), sort));

        List<UsuarioResponseVO> data = toResponses(page.getContent(), sesion.esAdministrador());
        return ResponseVO.of(page, data);
    }

    @Override
    @Transactional
    public SingleResponseVO<UsuarioResponseVO> create(RequestVO<CreateUsuarioRequestVO> request) {
        CreateUsuarioRequestVO parameters = request.getParameters();
        String correo = parameters.getCorreo().trim().toLowerCase();
        ValidationUtil.validate(usuarioRepository.existsByCorreoAndActivoTrue(correo),
                UsuarioErrorCode.EMAIL_ALREADY_EXISTS.getCode(), HttpStatus.CONFLICT);
        Set<Long> areas = validateAreas(parameters.getAreas());

        // El dominio valida el consentimiento y genera el hash; aqui solo se persiste
        Usuario usuario = Usuario.registrar(parameters.getNombre(), correo, parameters.getContrasena(),
                parameters.isAceptaAvisoPrivacidad(), cifrador);
        areas.forEach(usuario::asignarArea);

        UsuarioDO entity = new UsuarioDO();
        entity.setNombre(usuario.getNombre());
        entity.setCorreo(usuario.getCorreo());
        entity.setHashContrasena(usuario.getHashContrasena());
        entity.setPerfil(usuario.getPerfil().name());
        entity.setConsentimientoDatos(usuario.isConsentimientoDatos());
        entity.setFechaConsentimiento(usuario.getFechaConsentimiento());
        usuarioRepository.save(entity);
        replaceAreas(entity.getId(), usuario.getAreas());

        return SingleResponseVO.ok(toResponse(entity));
    }

    @Override
    @Transactional
    public SingleResponseVO<UsuarioResponseVO> updatePerfil(RequestVO<UpdatePerfilRequestVO> request) {
        UpdatePerfilRequestVO parameters = request.getParameters();
        ValidationUtil.validate(parameters.getPerfil() != Perfil.SUPERVISOR,
                UsuarioErrorCode.PROFILE_NOT_SUPPORTED.getCode(), HttpStatus.BAD_REQUEST);
        UsuarioDO entity = findActive(parameters.getId());

        Usuario usuario = toDomain(entity);
        usuario.promoverASupervisor();
        entity.setPerfil(usuario.getPerfil().name());
        usuarioRepository.save(entity);

        return SingleResponseVO.ok(toResponse(entity));
    }

    @Override
    @Transactional
    public SingleResponseVO<UsuarioResponseVO> updateAreas(RequestVO<UpdateAreasRequestVO> request) {
        UpdateAreasRequestVO parameters = request.getParameters();
        UsuarioDO entity = findActive(parameters.getId());
        replaceAreas(entity.getId(), validateAreas(parameters.getAreas()));
        return SingleResponseVO.ok(toResponse(entity));
    }

    @Override
    @Transactional
    public SingleResponseVO<Boolean> delete(RequestVO<IdRequestVO> request) {
        Long id = request.getParameters().getId();
        ValidationUtil.validate(id.equals(request.getSesion().getId()),
                UsuarioErrorCode.SELF_DELETE.getCode(), HttpStatus.BAD_REQUEST);
        UsuarioDO entity = findActive(id);
        entity.setActivo(Boolean.FALSE);
        usuarioRepository.save(entity);
        replaceAreas(id, Set.of());
        return SingleResponseVO.ok(Boolean.TRUE);
    }

    @Override
    public UsuarioResponseVO toResponse(UsuarioDO entity) {
        return toResponses(List.of(entity), false).getFirst();
    }

    private List<UsuarioResponseVO> toResponses(List<UsuarioDO> entities, boolean admin) {
        Map<Long, List<Long>> areasPorUsuario = usuarioAreaRepository
                .findByIdUsuarioInAndActivoTrue(entities.stream().map(UsuarioDO::getId).toList()).stream()
                .collect(Collectors.groupingBy(UsuarioAreaDO::getIdUsuario,
                        Collectors.mapping(UsuarioAreaDO::getIdArea, Collectors.toList())));
        Map<Long, String> nombresArea = nombres.areas(areasPorUsuario.values().stream()
                .flatMap(List::stream).collect(Collectors.toSet()));

        return entities.stream().map(entity -> {
            UsuarioResponseVO vo = admin ? new UsuarioAdminVO() : new UsuarioResponseVO();
            vo.setId(entity.getId());
            vo.setNombre(entity.getNombre());
            vo.setCorreo(entity.getCorreo());
            vo.setPerfil(entity.getPerfil());
            vo.setAreas(areasPorUsuario.getOrDefault(entity.getId(), List.of()).stream()
                    .filter(nombresArea::containsKey)
                    .map(idArea -> new AreaResumenVO(idArea, nombresArea.get(idArea)))
                    .sorted(Comparator.comparing(AreaResumenVO::getNombre))
                    .toList());
            if (vo instanceof UsuarioAdminVO adminVO) {
                adminVO.setFechaConsentimiento(entity.getFechaConsentimiento());
                adminVO.setFechaCreacion(entity.getFechaCreacion());
            }
            return vo;
        }).toList();
    }

    private Usuario toDomain(UsuarioDO entity) {
        Set<Long> areas = usuarioAreaRepository.findByIdUsuarioAndActivoTrue(entity.getId()).stream()
                .map(UsuarioAreaDO::getIdArea).collect(Collectors.toSet());
        return Usuario.reconstituir(entity.getNombre(), entity.getCorreo(), entity.getHashContrasena(),
                Perfil.valueOf(entity.getPerfil()), entity.getActivo(), entity.getConsentimientoDatos(),
                entity.getFechaConsentimiento(), areas);
    }

    /** Da de baja las asignaciones que ya no van y agrega las nuevas (borrado logico). */
    private void replaceAreas(Long idUsuario, Set<Long> nuevas) {
        Set<Long> pendientes = new HashSet<>(nuevas);
        for (UsuarioAreaDO actual : usuarioAreaRepository.findByIdUsuarioAndActivoTrue(idUsuario)) {
            if (!pendientes.remove(actual.getIdArea())) {
                actual.setActivo(Boolean.FALSE);
                usuarioAreaRepository.save(actual);
            }
        }
        pendientes.forEach(idArea -> {
            UsuarioAreaDO asignacion = new UsuarioAreaDO();
            asignacion.setIdUsuario(idUsuario);
            asignacion.setIdArea(idArea);
            usuarioAreaRepository.save(asignacion);
        });
    }

    private Set<Long> validateAreas(List<Long> ids) {
        Set<Long> areas = new LinkedHashSet<>(ids);
        areas.forEach(idArea -> ValidationUtil.validate(
                idArea == null || areaRepository.findByIdAndActivoTrue(idArea).isEmpty(),
                UsuarioErrorCode.AREA_NOT_FOUND.getCode(), HttpStatus.BAD_REQUEST));
        return areas;
    }

    private UsuarioDO findActive(Long id) {
        ValidationUtil.validate(ValidationUtil.isNullOrZero(id),
                CommonErrorCode.ID_REQUIRED.getCode(), HttpStatus.BAD_REQUEST);
        return usuarioRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException(UsuarioErrorCode.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND));
    }
}
