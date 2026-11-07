package mx.unam.buzz.rutinas.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.unam.buzz.rutinas.common.constant.AuthErrorCode;
import mx.unam.buzz.rutinas.common.constant.CommonErrorCode;
import mx.unam.buzz.rutinas.common.constant.EjecucionConstant;
import mx.unam.buzz.rutinas.common.constant.EjecucionErrorCode;
import mx.unam.buzz.rutinas.common.constant.RutinaErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import mx.unam.buzz.rutinas.common.security.Alcance;
import mx.unam.buzz.rutinas.common.security.UsuarioSesionVO;
import mx.unam.buzz.rutinas.common.util.ValidationUtil;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.CambioEstadoEjecucionRequestVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.EjecucionResponseVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.EjecucionResumenVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.FindListEjecucionRequestVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.TareaEjecucionVO;
import mx.unam.buzz.rutinas.dominio.ejecucion.Ejecucion;
import mx.unam.buzz.rutinas.dominio.ejecucion.EstadoEjecucion;
import mx.unam.buzz.rutinas.dominio.ejecucion.EstadoTareaEjecucion;
import mx.unam.buzz.rutinas.dominio.rutina.Frecuencia;
import mx.unam.buzz.rutinas.modelo.entity.EjecucionDO;
import mx.unam.buzz.rutinas.modelo.entity.RutinaDO;
import mx.unam.buzz.rutinas.modelo.entity.TareaDO;
import mx.unam.buzz.rutinas.modelo.entity.TareaEjecucionDO;
import mx.unam.buzz.rutinas.persistence.IEjecucionRepository;
import mx.unam.buzz.rutinas.persistence.IIncidenciaRepository;
import mx.unam.buzz.rutinas.persistence.IRutinaRepository;
import mx.unam.buzz.rutinas.persistence.ITareaEjecucionRepository;
import mx.unam.buzz.rutinas.persistence.ITareaRepository;
import mx.unam.buzz.rutinas.service.IEjecucionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rondas. Cada cambio de estado sigue el mismo camino: leer la entidad, rehidratar
 * el objeto de dominio, pedirle la transicion (el dominio decide si es valida) y
 * copiar el resultado a la entidad. La version de JPA cierra el bloqueo optimista
 * contra la base.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EjecucionServiceImpl implements IEjecucionService {

    private static final Set<String> ORDER_FIELDS = Set.of("id", "fechaProgramada", "estado");

    private final IEjecucionRepository ejecucionRepository;
    private final ITareaEjecucionRepository tareaEjecucionRepository;
    private final ITareaRepository tareaRepository;
    private final IRutinaRepository rutinaRepository;
    private final IIncidenciaRepository incidenciaRepository;
    private final ConsultaNombres nombres;

    @Override
    @Transactional(readOnly = true)
    public ResponseVO<EjecucionResumenVO> findList(RequestVO<FindListEjecucionRequestVO> request) {
        UsuarioSesionVO sesion = request.getSesion();
        FindListEjecucionRequestVO filters = request.getParameters() != null
                ? request.getParameters() : new FindListEjecucionRequestVO();
        ValidationUtil.validate(!ORDER_FIELDS.contains(request.getOrderBy()),
                CommonErrorCode.INVALID_REQUEST.getCode(), HttpStatus.BAD_REQUEST);
        Sort sort = Sort.by(Sort.Direction.fromOptionalString(request.getOrderType()).orElse(Sort.Direction.ASC),
                request.getOrderBy()).and(Sort.by("id"));

        LocalDate dia = filters.getFecha() != null ? filters.getFecha() : LocalDate.now();
        Page<EjecucionDO> page = ejecucionRepository.findList(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay(),
                StringUtils.defaultString(StringUtils.upperCase(StringUtils.trimToNull(filters.getEstado()))),
                filters.isSoloMias() ? sesion.getId() : 0L,
                Alcance.sinFiltro(sesion, filters.getIdArea()), Alcance.areas(sesion, filters.getIdArea()),
                PageRequest.of(request.getPage(), request.getSize(), sort));

        return ResponseVO.of(page, toResumenes(page.getContent(), EjecucionResumenVO::new));
    }

    @Override
    @Transactional(readOnly = true)
    public SingleResponseVO<EjecucionResponseVO> findDetail(RequestVO<IdRequestVO> request) {
        EjecucionDO entity = findActive(request.getParameters().getId());
        checkArea(request.getSesion(), entity);
        return SingleResponseVO.ok(toDetalle(entity));
    }

    @Override
    @Transactional
    public SingleResponseVO<EjecucionResponseVO> cambiarEstado(RequestVO<CambioEstadoEjecucionRequestVO> request) {
        UsuarioSesionVO sesion = request.getSesion();
        CambioEstadoEjecucionRequestVO parameters = request.getParameters();
        EjecucionDO entity = findActive(parameters.getId());
        checkArea(sesion, entity);
        checkVersion(entity, parameters.getVersion());

        Ejecucion ejecucion = toDomain(entity);
        LocalDateTime ahora = LocalDateTime.now();
        switch (estado(parameters.getEstado())) {
            case EN_CURSO -> {
                ejecucion.iniciar(sesion.getId(), ejecucion.getVersion());
                entity.setIdOperador(sesion.getId());
                entity.setFechaInicio(ahora);
            }
            case COMPLETADA -> {
                ejecucion.completar();
                entity.setFechaFin(ahora);
            }
            case CANCELADA -> {
                // Cancelar es decision de quien supervisa, no del operador
                ValidationUtil.validate(!sesion.puedeSupervisar(),
                        AuthErrorCode.FORBIDDEN.getCode(), HttpStatus.FORBIDDEN);
                ejecucion.cancelar();
                entity.setFechaFin(ahora);
            }
            default -> throw new BusinessException(EjecucionErrorCode.STATE_NOT_SUPPORTED.getCode(),
                    HttpStatus.BAD_REQUEST);
        }
        entity.setEstado(ejecucion.getEstado().name());
        ejecucionRepository.saveAndFlush(entity);
        return SingleResponseVO.ok(toDetalle(entity));
    }

    @Override
    @Transactional
    public SingleResponseVO<EjecucionResponseVO> cambiarEstadoTarea(RequestVO<CambioEstadoEjecucionRequestVO> request) {
        UsuarioSesionVO sesion = request.getSesion();
        CambioEstadoEjecucionRequestVO parameters = request.getParameters();
        EjecucionDO entity = findActive(parameters.getId());
        checkArea(sesion, entity);
        checkVersion(entity, parameters.getVersion());

        List<TareaEjecucionDO> tareas = tareaEjecucionRepository.findByIdEjecucionAndActivoTrueOrderByIdAsc(entity.getId());
        TareaEjecucionDO tarea = tareas.stream()
                .filter(item -> item.getIdTarea().equals(parameters.getIdTarea()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(EjecucionErrorCode.TASK_NOT_FOUND.getCode(),
                        HttpStatus.NOT_FOUND));

        Ejecucion ejecucion = toDomain(entity, tareas);
        EstadoTareaEjecucion destino = estadoTarea(parameters.getEstado());
        // Marcar como hecha una tarea pendiente recorre la maquina completa (PENDIENTE ->
        // EN_PROCESO -> COMPLETADA) en una sola peticion: no se salta ningun estado.
        if (ejecucion.getEstadoTarea(tarea.getIdTarea()) == EstadoTareaEjecucion.PENDIENTE) {
            ejecucion.iniciarTarea(tarea.getIdTarea(), sesion.getId());
        }
        if (destino == EstadoTareaEjecucion.COMPLETADA) {
            ejecucion.completarTarea(tarea.getIdTarea(), sesion.getId());
            tarea.setFechaCompletada(LocalDateTime.now());
        }
        tarea.setEstado(ejecucion.getEstadoTarea(tarea.getIdTarea()).name());
        tareaEjecucionRepository.save(tarea);

        // El avance de una tarea tambien cambia la ronda: se toca la fila padre para que
        // su version suba y otro dispositivo con la version anterior reciba 409
        entity.setFechaModificacion(LocalDateTime.now());
        ejecucionRepository.saveAndFlush(entity);
        return SingleResponseVO.ok(toDetalle(entity));
    }

    @Override
    @Transactional
    public SingleResponseVO<EjecucionResponseVO> lanzar(RequestVO<IdRequestVO> request) {
        RutinaDO rutina = rutinaRepository.findByIdAndActivoTrue(request.getParameters().getId())
                .orElseThrow(() -> new BusinessException(RutinaErrorCode.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND));
        ValidationUtil.validate(!request.getSesion().tieneArea(rutina.getIdArea()),
                RutinaErrorCode.AREA_FORBIDDEN.getCode(), HttpStatus.FORBIDDEN);
        ValidationUtil.validate(!Frecuencia.POR_EVENTO.name().equals(rutina.getFrecuencia()),
                RutinaErrorCode.NOT_BY_EVENT.getCode(), HttpStatus.BAD_REQUEST);

        EjecucionDO entity = programar(rutina, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        return SingleResponseVO.ok(toDetalle(entity));
    }

    @Override
    @Transactional
    public int programarDia(LocalDate dia, LocalDateTime desde) {
        int total = rutinaRepository.findByActivoTrue().stream()
                .mapToInt(rutina -> programar(rutina, dia, desde))
                .sum();
        log.info("Programacion del {}: {} rondas nuevas", dia, total);
        return total;
    }

    @Override
    @Transactional
    public int programarRutina(Long idRutina, LocalDate dia, LocalDateTime desde) {
        return rutinaRepository.findByIdAndActivoTrue(idRutina)
                .map(rutina -> programar(rutina, dia, desde))
                .orElse(0);
    }

    @Override
    @Transactional
    public int marcarVencidas(LocalDateTime ahora) {
        List<EjecucionDO> vencidas = ejecucionRepository.findByEstadoAndFechaProgramadaBeforeAndActivoTrue(
                EstadoEjecucion.PROGRAMADA.name(), ahora.minusMinutes(EjecucionConstant.TOLERANCIA_MINUTOS));
        vencidas.forEach(entity -> {
            Ejecucion ejecucion = toDomain(entity);
            ejecucion.marcarVencida(ahora);
            entity.setEstado(ejecucion.getEstado().name());
            ejecucionRepository.save(entity);
        });
        if (!vencidas.isEmpty()) {
            log.info("{} rondas pasaron a VENCIDA", vencidas.size());
        }
        return vencidas.size();
    }

    @Override
    @Transactional
    public int cancelarProgramadas(Long idRutina, LocalDateTime desde) {
        List<EjecucionDO> programadas = ejecucionRepository.findByIdRutinaAndEstadoAndFechaProgramadaAfterAndActivoTrue(
                idRutina, EstadoEjecucion.PROGRAMADA.name(), desde);
        programadas.forEach(entity -> {
            Ejecucion ejecucion = toDomain(entity);
            ejecucion.cancelar();
            entity.setEstado(ejecucion.getEstado().name());
            ejecucionRepository.save(entity);
        });
        return programadas.size();
    }

    /** Strategy: la frecuencia de la rutina decide a que horas toca ese dia. */
    private int programar(RutinaDO rutina, LocalDate dia, LocalDateTime desde) {
        List<LocalDateTime> horarios = Frecuencia.valueOf(rutina.getFrecuencia())
                .horarios(dia, RutinaServiceImpl.horario(rutina));
        int nuevas = 0;
        for (LocalDateTime hora : horarios) {
            if (!hora.isBefore(desde)
                    && !ejecucionRepository.existsByIdRutinaAndFechaProgramadaAndActivoTrue(rutina.getId(), hora)) {
                programar(rutina, hora);
                nuevas++;
            }
        }
        return nuevas;
    }

    private EjecucionDO programar(RutinaDO rutina, LocalDateTime hora) {
        List<Long> idsTareas = tareaRepository.findByIdRutinaAndActivoTrueOrderByOrdenAsc(rutina.getId()).stream()
                .map(TareaDO::getId).toList();
        // El dominio valida que haya tareas y arma la ronda en PROGRAMADA con todo PENDIENTE
        Ejecucion ejecucion = Ejecucion.programar(rutina.getId(), hora, idsTareas);

        EjecucionDO entity = new EjecucionDO();
        entity.setIdRutina(rutina.getId());
        entity.setFechaProgramada(hora);
        entity.setEstado(ejecucion.getEstado().name());
        ejecucionRepository.save(entity);
        ejecucion.getTareas().forEach((idTarea, estado) -> {
            TareaEjecucionDO tarea = new TareaEjecucionDO();
            tarea.setIdEjecucion(entity.getId());
            tarea.setIdTarea(idTarea);
            tarea.setEstado(estado.name());
            tareaEjecucionRepository.save(tarea);
        });
        return entity;
    }

    private Ejecucion toDomain(EjecucionDO entity) {
        return toDomain(entity, tareaEjecucionRepository.findByIdEjecucionAndActivoTrueOrderByIdAsc(entity.getId()));
    }

    private Ejecucion toDomain(EjecucionDO entity, List<TareaEjecucionDO> tareas) {
        Map<Long, EstadoTareaEjecucion> estados = new LinkedHashMap<>();
        tareas.forEach(tarea -> estados.put(tarea.getIdTarea(), EstadoTareaEjecucion.valueOf(tarea.getEstado())));
        return Ejecucion.reconstituir(entity.getIdRutina(), entity.getFechaProgramada(), estados,
                EstadoEjecucion.valueOf(entity.getEstado()), entity.getIdOperador(), entity.getVersion());
    }

    private EjecucionResponseVO toDetalle(EjecucionDO entity) {
        EjecucionResponseVO vo = toResumenes(List.of(entity), EjecucionResponseVO::new).getFirst();
        RutinaDO rutina = nombres.rutinas(List.of(entity.getIdRutina())).get(entity.getIdRutina());
        vo.setDescripcionRutina(rutina.getDescripcion());
        vo.setFechaInicio(entity.getFechaInicio());
        vo.setFechaFin(entity.getFechaFin());

        List<TareaEjecucionDO> avance = tareaEjecucionRepository.findByIdEjecucionAndActivoTrueOrderByIdAsc(entity.getId());
        Map<Long, TareaDO> tareas = tareaRepository.findAllById(avance.stream().map(TareaEjecucionDO::getIdTarea).toList())
                .stream().collect(Collectors.toMap(TareaDO::getId, Function.identity()));
        vo.setTareas(avance.stream().map(item -> {
            TareaEjecucionVO tarea = new TareaEjecucionVO();
            tarea.setIdTarea(item.getIdTarea());
            tarea.setNombre(tareas.get(item.getIdTarea()).getNombre());
            tarea.setOrden(tareas.get(item.getIdTarea()).getOrden());
            tarea.setEstado(item.getEstado());
            tarea.setFechaCompletada(item.getFechaCompletada());
            return tarea;
        }).sorted((a, b) -> Integer.compare(a.getOrden(), b.getOrden())).toList());
        vo.setIncidencias(List.of());
        return vo;
    }

    /** Arma los resumenes de varias rondas con una consulta por tabla. */
    private <T extends EjecucionResumenVO> List<T> toResumenes(List<EjecucionDO> entities,
                                                               java.util.function.Supplier<T> factory) {
        if (entities.isEmpty()) {
            return List.of();
        }
        List<Long> ids = entities.stream().map(EjecucionDO::getId).toList();
        Map<Long, RutinaDO> rutinas = nombres.rutinas(entities.stream().map(EjecucionDO::getIdRutina).toList());
        Map<Long, String> areas = nombres.areas(rutinas.values().stream().map(RutinaDO::getIdArea).toList());
        Map<Long, String> operadores = nombres.usuarios(entities.stream().map(EjecucionDO::getIdOperador).toList());
        Map<Long, long[]> avance = new HashMap<>();
        tareaEjecucionRepository.countAvance(ids).forEach(row -> avance.put((Long) row[0],
                new long[]{((Number) row[1]).longValue(), ((Number) row[2]).longValue()}));
        Map<Long, Long> abiertas = new HashMap<>();
        incidenciaRepository.countAbiertas(ids).forEach(row -> abiertas.put((Long) row[0], ((Number) row[1]).longValue()));

        return entities.stream().map(entity -> {
            RutinaDO rutina = rutinas.get(entity.getIdRutina());
            T vo = factory.get();
            vo.setId(entity.getId());
            vo.setIdRutina(rutina.getId());
            vo.setRutina(rutina.getNombre());
            vo.setIdArea(rutina.getIdArea());
            vo.setArea(areas.get(rutina.getIdArea()));
            vo.setFrecuencia(rutina.getFrecuencia());
            vo.setFechaProgramada(entity.getFechaProgramada());
            vo.setEstado(entity.getEstado());
            vo.setIdOperador(entity.getIdOperador());
            vo.setOperador(operadores.get(entity.getIdOperador()));
            long[] conteo = avance.getOrDefault(entity.getId(), new long[]{0, 0});
            vo.setTareasTotal((int) conteo[0]);
            vo.setTareasCompletadas((int) conteo[1]);
            vo.setIncidenciasAbiertas(abiertas.getOrDefault(entity.getId(), 0L).intValue());
            vo.setVersion(entity.getVersion());
            return vo;
        }).toList();
    }

    private void checkArea(UsuarioSesionVO sesion, EjecucionDO entity) {
        RutinaDO rutina = nombres.rutinas(List.of(entity.getIdRutina())).get(entity.getIdRutina());
        ValidationUtil.validate(!sesion.tieneArea(rutina.getIdArea()),
                EjecucionErrorCode.AREA_FORBIDDEN.getCode(), HttpStatus.FORBIDDEN);
    }

    /** El cliente manda la version que leyo; si ya cambio, no se pisa el trabajo de otro. */
    private static void checkVersion(EjecucionDO entity, Long versionLeida) {
        ValidationUtil.validate(!entity.getVersion().equals(versionLeida),
                EjecucionErrorCode.VERSION_CONFLICT.getCode(), HttpStatus.CONFLICT);
    }

    private static EstadoEjecucion estado(String valor) {
        try {
            return EstadoEjecucion.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(EjecucionErrorCode.STATE_NOT_SUPPORTED.getCode(), HttpStatus.BAD_REQUEST);
        }
    }

    private static EstadoTareaEjecucion estadoTarea(String valor) {
        EstadoTareaEjecucion estado;
        try {
            estado = EstadoTareaEjecucion.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            estado = EstadoTareaEjecucion.PENDIENTE;
        }
        ValidationUtil.validate(estado == EstadoTareaEjecucion.PENDIENTE,
                EjecucionErrorCode.STATE_NOT_SUPPORTED.getCode(), HttpStatus.BAD_REQUEST);
        return estado;
    }

    private EjecucionDO findActive(Long id) {
        ValidationUtil.validate(ValidationUtil.isNullOrZero(id),
                CommonErrorCode.ID_REQUIRED.getCode(), HttpStatus.BAD_REQUEST);
        return ejecucionRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new BusinessException(EjecucionErrorCode.NOT_FOUND.getCode(), HttpStatus.NOT_FOUND));
    }
}
