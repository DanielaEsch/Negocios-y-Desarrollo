package mx.unam.buzz.rutinas.service.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.CommonErrorCode;
import mx.unam.buzz.rutinas.common.constant.RutinaConstant;
import mx.unam.buzz.rutinas.common.constant.RutinaErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import mx.unam.buzz.rutinas.common.security.Alcance;
import mx.unam.buzz.rutinas.common.security.UsuarioSesionVO;
import mx.unam.buzz.rutinas.common.util.ValidationUtil;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.rutina.FindListRutinaRequestVO;
import mx.unam.buzz.rutinas.common.vo.rutina.RutinaResponseVO;
import mx.unam.buzz.rutinas.common.vo.rutina.SaveRutinaRequestVO;
import mx.unam.buzz.rutinas.common.vo.rutina.TareaVO;
import mx.unam.buzz.rutinas.dominio.rutina.Frecuencia;
import mx.unam.buzz.rutinas.dominio.rutina.Horario;
import mx.unam.buzz.rutinas.modelo.entity.RutinaDO;
import mx.unam.buzz.rutinas.modelo.entity.TareaDO;
import mx.unam.buzz.rutinas.persistence.IAreaRepository;
import mx.unam.buzz.rutinas.persistence.IRutinaRepository;
import mx.unam.buzz.rutinas.persistence.ITareaRepository;
import mx.unam.buzz.rutinas.service.IRutinaService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Rutinas y sus tareas. La validacion del horario la hace la estrategia de la
 * frecuencia (dominio), no un switch aqui.
 */
@Service
@RequiredArgsConstructor
public class RutinaServiceImpl implements IRutinaService {

    private final IRutinaRepository rutinaRepository;
    private final ITareaRepository tareaRepository;
    private final IAreaRepository areaRepository;
    private final ConsultaNombres nombres;

    @Override
    @Transactional(readOnly = true)
    public ResponseVO<RutinaResponseVO> findList(RequestVO<FindListRutinaRequestVO> request) {
        UsuarioSesionVO sesion = request.getSesion();
        FindListRutinaRequestVO filters = request.getParameters() != null
                ? request.getParameters() : new FindListRutinaRequestVO();
        ValidationUtil.validate(!RutinaConstant.ORDER_FIELDS.contains(request.getOrderBy()),
                CommonErrorCode.INVALID_REQUEST.getCode(), HttpStatus.BAD_REQUEST);
        Sort sort = Sort.by(Sort.Direction.fromOptionalString(request.getOrderType()).orElse(Sort.Direction.ASC),
                request.getOrderBy());

        Page<RutinaDO> page = rutinaRepository.findList(
                Alcance.sinFiltro(sesion, filters.getIdArea()), Alcance.areas(sesion, filters.getIdArea()),
                StringUtils.defaultString(ValidationUtil.normalize(filters.getNombre(), RutinaConstant.MAX_SIZE_NOMBRE)),
                PageRequest.of(request.getPage(), request.getSize(), sort));
        return ResponseVO.of(page, toResponses(page.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public SingleResponseVO<RutinaResponseVO> findDetail(RequestVO<IdRequestVO> request) {
        RutinaDO entity = findActive(request.getParameters().getId());
        ValidationUtil.validate(!request.getSesion().tieneArea(entity.getIdArea()),
                RutinaErrorCode.AREA_FORBIDDEN.getCode(), HttpStatus.FORBIDDEN);
        return SingleResponseVO.ok(toResponses(List.of(entity)).getFirst());
    }

    @Override
    @Transactional
    public SingleResponseVO<RutinaResponseVO> create(RequestVO<SaveRutinaRequestVO> request) {
        SaveRutinaRequestVO parameters = request.getParameters();
        String nombre = validate(parameters, null);

        RutinaDO entity = new RutinaDO();
        apply(entity, parameters, nombre);
        rutinaRepository.save(entity);
        saveTareas(entity.getId(), parameters.getTareas());

        return SingleResponseVO.ok(toResponses(List.of(entity)).getFirst());
    }

    @Override
    @Transactional
    public SingleResponseVO<RutinaResponseVO> update(RequestVO<SaveRutinaRequestVO> request) {
        SaveRutinaRequestVO parameters = request.getParameters();
        RutinaDO entity = findActive(parameters.getId());
        String nombre = validate(parameters, entity.getId());

        apply(entity, parameters, nombre);
        rutinaRepository.save(entity);
        // Las tareas viejas se dan de baja, no se borran: las rondas pasadas las siguen citando
        tareaRepository.findByIdRutinaAndActivoTrueOrderByOrdenAsc(entity.getId()).forEach(tarea -> {
            tarea.setActivo(Boolean.FALSE);
            tareaRepository.save(tarea);
        });
        saveTareas(entity.getId(), parameters.getTareas());

        return SingleResponseVO.ok(toResponses(List.of(entity)).getFirst());
    }

    @Override
    @Transactional
    public SingleResponseVO<Boolean> delete(RequestVO<IdRequestVO> request) {
        RutinaDO entity = findActive(request.getParameters().getId());
        entity.setActivo(Boolean.FALSE);
        rutinaRepository.save(entity);
        return SingleResponseVO.ok(Boolean.TRUE);
    }

    private String validate(SaveRutinaRequestVO parameters, Long idActual) {
        ValidationUtil.validate(areaRepository.findByIdAndActivoTrue(parameters.getIdArea()).isEmpty(),
                RutinaErrorCode.AREA_NOT_FOUND.getCode(), HttpStatus.BAD_REQUEST);
        String nombre = ValidationUtil.normalize(parameters.getNombre(), RutinaConstant.MAX_SIZE_NOMBRE);
        boolean repetido = idActual == null
                ? rutinaRepository.existsByIdAreaAndNombreAndActivoTrue(parameters.getIdArea(), nombre)
                : rutinaRepository.existsByIdAreaAndNombreAndActivoTrueAndIdNot(parameters.getIdArea(), nombre, idActual);
        ValidationUtil.validate(repetido, RutinaErrorCode.ALREADY_EXISTS.getCode(), HttpStatus.CONFLICT);

        // Strategy: cada frecuencia sabe que datos de horario necesita (ReglaNegocioException -> 400)
        parameters.getFrecuencia().validar(horario(parameters));
        return nombre;
    }

    private void apply(RutinaDO entity, SaveRutinaRequestVO parameters, String nombre) {
        Frecuencia frecuencia = parameters.getFrecuencia();
        entity.setIdArea(parameters.getIdArea());
        entity.setNombre(nombre);
        entity.setDescripcion(ValidationUtil.normalize(parameters.getDescripcion(), RutinaConstant.MAX_SIZE_DESCRIPCION));
        entity.setFrecuencia(frecuencia.name());
        // Solo se guardan los datos de horario que la frecuencia usa
        entity.setHoraInicio(frecuencia == Frecuencia.POR_EVENTO ? null : parameters.getHoraInicio());
        entity.setHoraFin(frecuencia == Frecuencia.CADA_2_HORAS ? parameters.getHoraFin() : null);
        entity.setDiaSemana(frecuencia == Frecuencia.SEMANAL ? parameters.getDiaSemana() : null);
    }

    private void saveTareas(Long idRutina, List<String> tareas) {
        for (int i = 0; i < tareas.size(); i++) {
            TareaDO tarea = new TareaDO();
            tarea.setIdRutina(idRutina);
            tarea.setNombre(StringUtils.left(tareas.get(i).trim(), RutinaConstant.MAX_SIZE_TAREA));
            tarea.setOrden(i + 1);
            tareaRepository.save(tarea);
        }
    }

    private List<RutinaResponseVO> toResponses(List<RutinaDO> entities) {
        Map<Long, String> areas = nombres.areas(entities.stream().map(RutinaDO::getIdArea).toList());
        Map<Long, List<TareaDO>> tareas = tareaRepository
                .findByIdRutinaInAndActivoTrueOrderByOrdenAsc(entities.stream().map(RutinaDO::getId).toList())
                .stream().collect(Collectors.groupingBy(TareaDO::getIdRutina));

        return entities.stream().map(entity -> {
            RutinaResponseVO vo = new RutinaResponseVO();
            vo.setId(entity.getId());
            vo.setIdArea(entity.getIdArea());
            vo.setArea(areas.get(entity.getIdArea()));
            vo.setNombre(entity.getNombre());
            vo.setDescripcion(entity.getDescripcion());
            vo.setFrecuencia(entity.getFrecuencia());
            vo.setHoraInicio(entity.getHoraInicio());
            vo.setHoraFin(entity.getHoraFin());
            vo.setDiaSemana(entity.getDiaSemana());
            vo.setTareas(tareas.getOrDefault(entity.getId(), List.of()).stream()
                    .map(tarea -> new TareaVO(tarea.getId(), tarea.getNombre(), tarea.getOrden()))
                    .toList());
            return vo;
        }).toList();
    }

    private RutinaDO findActive(Long id) {
        ValidationUtil.validate(ValidationUtil.isNullOrZero(id),
                CommonErrorCode.ID_REQUIRED.getCode(), HttpStatus.BAD_REQUEST);
        return rutinaRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException(RutinaErrorCode.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND));
    }

    static Horario horario(SaveRutinaRequestVO parameters) {
        return new Horario(parameters.getHoraInicio(), parameters.getHoraFin(),
                parameters.getDiaSemana() == null ? null : DayOfWeek.of(parameters.getDiaSemana()));
    }

    /** Horario guardado de una rutina, en terminos del dominio. */
    static Horario horario(RutinaDO entity) {
        return new Horario(entity.getHoraInicio(), entity.getHoraFin(),
                entity.getDiaSemana() == null ? null : DayOfWeek.of(entity.getDiaSemana()));
    }
}
