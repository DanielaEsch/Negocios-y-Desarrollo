package mx.unam.buzz.rutinas.service.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.CommonErrorCode;
import mx.unam.buzz.rutinas.common.constant.EjecucionErrorCode;
import mx.unam.buzz.rutinas.common.constant.IncidenciaErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import mx.unam.buzz.rutinas.common.security.Alcance;
import mx.unam.buzz.rutinas.common.security.UsuarioSesionVO;
import mx.unam.buzz.rutinas.common.util.ValidationUtil;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.CambioEstadoIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.CreateIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.FindListIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.IncidenciaDetalleVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.IncidenciaResponseVO;
import mx.unam.buzz.rutinas.dominio.incidencia.EstadoIncidencia;
import mx.unam.buzz.rutinas.dominio.incidencia.Incidencia;
import mx.unam.buzz.rutinas.modelo.entity.EjecucionDO;
import mx.unam.buzz.rutinas.modelo.entity.IncidenciaDO;
import mx.unam.buzz.rutinas.modelo.entity.RutinaDO;
import mx.unam.buzz.rutinas.persistence.IEjecucionRepository;
import mx.unam.buzz.rutinas.persistence.IIncidenciaRepository;
import mx.unam.buzz.rutinas.service.IIncidenciaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Incidencias. Cualquiera con acceso al area reporta; revisar y resolver lo
 * restringe el controlador a SUPERVISOR y ADMINISTRADOR, y el dominio lo vuelve a
 * exigir. El operador recibe IncidenciaResponseVO; quien supervisa,
 * IncidenciaDetalleVO (con reporta y revisor).
 */
@Service
@RequiredArgsConstructor
public class IncidenciaServiceImpl implements IIncidenciaService {

    private static final Set<String> ORDER_FIELDS = Set.of("id", "fechaReporte", "estado");

    private final IIncidenciaRepository incidenciaRepository;
    private final IEjecucionRepository ejecucionRepository;
    private final ConsultaNombres nombres;

    @Override
    @Transactional
    public SingleResponseVO<IncidenciaResponseVO> create(RequestVO<CreateIncidenciaRequestVO> request) {
        UsuarioSesionVO sesion = request.getSesion();
        CreateIncidenciaRequestVO parameters = request.getParameters();
        EjecucionDO ejecucion = ejecucionRepository.findByIdAndActivoTrue(parameters.getIdEjecucion())
                .orElseThrow(() -> new BusinessException(EjecucionErrorCode.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND));
        RutinaDO rutina = nombres.rutinas(List.of(ejecucion.getIdRutina())).get(ejecucion.getIdRutina());
        ValidationUtil.validate(!sesion.tieneArea(rutina.getIdArea()),
                EjecucionErrorCode.AREA_FORBIDDEN.getCode(), HttpStatus.FORBIDDEN);

        Incidencia incidencia = Incidencia.reportar(ejecucion.getId(), sesion.getId(), parameters.getDescripcion());
        IncidenciaDO entity = new IncidenciaDO();
        entity.setIdEjecucion(incidencia.getIdEjecucion());
        entity.setIdReporta(incidencia.getIdReporta());
        entity.setDescripcion(StringUtils.left(incidencia.getDescripcion(), 500));
        entity.setEstado(incidencia.getEstado().name());
        entity.setFechaReporte(incidencia.getFechaReporte());
        incidenciaRepository.save(entity);

        return SingleResponseVO.ok(toResponses(List.of(entity), sesion).getFirst());
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseVO<IncidenciaResponseVO> findList(RequestVO<FindListIncidenciaRequestVO> request) {
        UsuarioSesionVO sesion = request.getSesion();
        FindListIncidenciaRequestVO filters = request.getParameters() != null
                ? request.getParameters() : new FindListIncidenciaRequestVO();
        ValidationUtil.validate(!ORDER_FIELDS.contains(request.getOrderBy()),
                CommonErrorCode.INVALID_REQUEST.getCode(), HttpStatus.BAD_REQUEST);
        Sort sort = Sort.by(Sort.Direction.fromOptionalString(request.getOrderType()).orElse(Sort.Direction.DESC),
                request.getOrderBy());

        // El operador solo ve lo que el reporto; quien supervisa, todo lo de sus areas
        Page<IncidenciaDO> page = incidenciaRepository.findList(
                StringUtils.defaultString(StringUtils.upperCase(StringUtils.trimToNull(filters.getEstado()))),
                sesion.puedeSupervisar() ? 0L : sesion.getId(),
                Alcance.sinFiltro(sesion, filters.getIdArea()), Alcance.areas(sesion, filters.getIdArea()),
                PageRequest.of(request.getPage(), request.getSize(), sort));
        return ResponseVO.of(page, toResponses(page.getContent(), sesion));
    }

    @Override
    @Transactional
    public SingleResponseVO<IncidenciaResponseVO> cambiarEstado(RequestVO<CambioEstadoIncidenciaRequestVO> request) {
        UsuarioSesionVO sesion = request.getSesion();
        CambioEstadoIncidenciaRequestVO parameters = request.getParameters();
        ValidationUtil.validate(ValidationUtil.isNullOrZero(parameters.getId()),
                CommonErrorCode.ID_REQUIRED.getCode(), HttpStatus.BAD_REQUEST);
        IncidenciaDO entity = incidenciaRepository.findByIdAndActivoTrue(parameters.getId())
                .orElseThrow(() -> new BusinessException(IncidenciaErrorCode.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND));
        checkArea(sesion, entity);

        Incidencia incidencia = Incidencia.reconstituir(entity.getIdEjecucion(), entity.getIdReporta(),
                entity.getDescripcion(), entity.getFechaReporte(), EstadoIncidencia.valueOf(entity.getEstado()),
                entity.getIdRevisor(), entity.getComentarioResolucion(), entity.getFechaResolucion());
        switch (estado(parameters.getEstado())) {
            case REVISION -> incidencia.tomarEnRevision(sesion.getId(), sesion.getPerfil());
            case RESUELTA -> incidencia.resolver(parameters.getComentarioResolucion());
            default -> throw new BusinessException(IncidenciaErrorCode.STATE_NOT_SUPPORTED.getCode(),
                    HttpStatus.BAD_REQUEST);
        }
        entity.setEstado(incidencia.getEstado().name());
        entity.setIdRevisor(incidencia.getIdRevisor());
        entity.setComentarioResolucion(incidencia.getComentarioResolucion());
        entity.setFechaResolucion(incidencia.getFechaResolucion());
        incidenciaRepository.save(entity);

        return SingleResponseVO.ok(toResponses(List.of(entity), sesion).getFirst());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaResponseVO> findByEjecucion(Long idEjecucion, UsuarioSesionVO sesion) {
        return toResponses(incidenciaRepository.findByIdEjecucionAndActivoTrueOrderByFechaReporteAsc(idEjecucion),
                sesion);
    }

    private List<IncidenciaResponseVO> toResponses(List<IncidenciaDO> entities, UsuarioSesionVO sesion) {
        if (entities.isEmpty()) {
            return List.of();
        }
        boolean detalle = sesion.puedeSupervisar();
        Map<Long, EjecucionDO> ejecuciones = nombres.ejecuciones(entities.stream().map(IncidenciaDO::getIdEjecucion).toList());
        Map<Long, RutinaDO> rutinas = nombres.rutinas(ejecuciones.values().stream().map(EjecucionDO::getIdRutina).toList());
        Map<Long, String> areas = nombres.areas(rutinas.values().stream().map(RutinaDO::getIdArea).toList());
        Map<Long, String> usuarios = detalle
                ? nombres.usuarios(entities.stream().flatMap(i -> Stream.of(i.getIdReporta(), i.getIdRevisor())).toList())
                : Map.of();

        return entities.stream().map(entity -> {
            RutinaDO rutina = rutinas.get(ejecuciones.get(entity.getIdEjecucion()).getIdRutina());
            IncidenciaResponseVO vo = detalle ? new IncidenciaDetalleVO() : new IncidenciaResponseVO();
            vo.setId(entity.getId());
            vo.setIdEjecucion(entity.getIdEjecucion());
            vo.setRutina(rutina.getNombre());
            vo.setIdArea(rutina.getIdArea());
            vo.setArea(areas.get(rutina.getIdArea()));
            vo.setDescripcion(entity.getDescripcion());
            vo.setEstado(entity.getEstado());
            vo.setFechaReporte(entity.getFechaReporte());
            vo.setComentarioResolucion(entity.getComentarioResolucion());
            vo.setFechaResolucion(entity.getFechaResolucion());
            if (vo instanceof IncidenciaDetalleVO full) {
                full.setIdReporta(entity.getIdReporta());
                full.setReporta(usuarios.get(entity.getIdReporta()));
                full.setIdRevisor(entity.getIdRevisor());
                full.setRevisor(usuarios.get(entity.getIdRevisor()));
            }
            return vo;
        }).toList();
    }

    private void checkArea(UsuarioSesionVO sesion, IncidenciaDO entity) {
        EjecucionDO ejecucion = nombres.ejecuciones(List.of(entity.getIdEjecucion())).get(entity.getIdEjecucion());
        RutinaDO rutina = nombres.rutinas(List.of(ejecucion.getIdRutina())).get(ejecucion.getIdRutina());
        ValidationUtil.validate(!sesion.tieneArea(rutina.getIdArea()),
                IncidenciaErrorCode.AREA_FORBIDDEN.getCode(), HttpStatus.FORBIDDEN);
    }

    private static EstadoIncidencia estado(String valor) {
        try {
            return EstadoIncidencia.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(IncidenciaErrorCode.STATE_NOT_SUPPORTED.getCode(), HttpStatus.BAD_REQUEST);
        }
    }
}
