package mx.unam.buzz.rutinas.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.unam.buzz.rutinas.common.constant.EjecucionConstant;
import mx.unam.buzz.rutinas.dominio.ejecucion.Ejecucion;
import mx.unam.buzz.rutinas.dominio.ejecucion.EstadoEjecucion;
import mx.unam.buzz.rutinas.dominio.ejecucion.EstadoTareaEjecucion;
import mx.unam.buzz.rutinas.dominio.incidencia.Incidencia;
import mx.unam.buzz.rutinas.dominio.rutina.Frecuencia;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import mx.unam.buzz.rutinas.modelo.entity.AreaDO;
import mx.unam.buzz.rutinas.modelo.entity.EjecucionDO;
import mx.unam.buzz.rutinas.modelo.entity.IncidenciaDO;
import mx.unam.buzz.rutinas.modelo.entity.RutinaDO;
import mx.unam.buzz.rutinas.modelo.entity.TareaEjecucionDO;
import mx.unam.buzz.rutinas.modelo.entity.UsuarioDO;
import mx.unam.buzz.rutinas.persistence.IAreaRepository;
import mx.unam.buzz.rutinas.persistence.IEjecucionRepository;
import mx.unam.buzz.rutinas.persistence.IIncidenciaRepository;
import mx.unam.buzz.rutinas.persistence.IRutinaRepository;
import mx.unam.buzz.rutinas.persistence.ITareaEjecucionRepository;
import mx.unam.buzz.rutinas.persistence.IUsuarioRepository;
import mx.unam.buzz.rutinas.service.IDemoService;
import mx.unam.buzz.rutinas.service.IEjecucionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Simulacion para la demo. Usa las mismas reglas del dominio que la API (iniciar,
 * completar tareas, reportar, revisar, resolver); solo cambia las horas para que
 * parezcan del pasado. El azar tiene semilla por dia: dos arranques dan lo mismo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemoServiceImpl implements IDemoService {

    private static final int PORCENTAJE_COMPLETADAS = 86;
    private static final int PORCENTAJE_CON_INCIDENCIA = 12;
    private static final int PORCENTAJE_CON_INCIDENCIA_HOY = 30;
    private static final int PORCENTAJE_RESUELTAS = 80;

    /** Cada problema con lo que un supervisor contestaria al cerrarlo. */
    private record Problema(String descripcion, String resolucion) {
    }

    private static final Map<String, List<Problema>> PROBLEMAS = Map.of(
            "MANTENIMIENTO", List.of(
                    new Problema("Se acabo el jabon en el bano de mujeres", "Se repuso y se pidio mas al proveedor"),
                    new Problema("El lavabo 2 gotea", "Se cambio el empaque de la llave"),
                    new Problema("No hay papel de repuesto en bodega", "Se compro papel para la semana"),
                    new Problema("La puerta del bano de hombres no cierra bien", "Se ajustaron las bisagras")),
            "COCINA", List.of(
                    new Problema("El refrigerador 2 marca 7 grados", "Se ajusto el termostato y se volvio a medir"),
                    new Problema("Producto caducado en el refrigerador de lacteos",
                            "Se retiro el producto y se registro la merma"),
                    new Problema("La campana hace un ruido raro", "Se llamo al tecnico; quedo reparada"),
                    new Problema("Falta desinfectante para superficies", "Se repuso desde el almacen")),
            "RECEPCION", List.of(
                    new Problema("La terminal de pago no enciende", "Se reinicio y se cargo la bateria"),
                    new Problema("Faltan 50 pesos en el corte", "Se aclaro con el turno anterior"),
                    new Problema("Reservacion duplicada en el sistema", "Se cancelo la reservacion repetida")));

    private final IEjecucionService ejecucionService;
    private final IEjecucionRepository ejecucionRepository;
    private final ITareaEjecucionRepository tareaEjecucionRepository;
    private final IRutinaRepository rutinaRepository;
    private final IAreaRepository areaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IIncidenciaRepository incidenciaRepository;

    @Override
    @Transactional
    public void simular(int diasHistorial, LocalDateTime ahora) {
        LocalDate hoy = ahora.toLocalDate();
        for (LocalDate dia = hoy.minusDays(diasHistorial); !dia.isAfter(hoy); dia = dia.plusDays(1)) {
            if (ejecucionRepository.existsByFechaProgramadaBetween(dia.atStartOfDay(), dia.atTime(LocalTime.MAX))) {
                continue;
            }
            ejecucionService.programarDia(dia, dia.atStartOfDay());
            simularDia(dia, ahora);
        }
    }

    private void simularDia(LocalDate dia, LocalDateTime ahora) {
        boolean esHoy = dia.equals(ahora.toLocalDate());
        Random azar = new Random(dia.toEpochDay());
        List<EjecucionDO> rondas = ejecucionRepository.findRango(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay());
        Map<Long, RutinaDO> rutinas = rutinaRepository.findAllById(rondas.stream().map(EjecucionDO::getIdRutina)
                .collect(Collectors.toSet())).stream().collect(Collectors.toMap(RutinaDO::getId, Function.identity()));
        Map<Long, String> areas = areaRepository.findAll().stream()
                .collect(Collectors.toMap(AreaDO::getId, AreaDO::getNombre));

        // Hoy queda una ronda a medias: la ultima de una rutina cada 2 horas que ya empezo
        EjecucionDO aMedias = esHoy ? rondas.stream()
                .filter(e -> !e.getFechaProgramada().isAfter(ahora))
                .filter(e -> Frecuencia.CADA_2_HORAS.name().equals(rutinas.get(e.getIdRutina()).getFrecuencia()))
                .reduce((primera, segunda) -> segunda).orElse(null) : null;

        int completadas = 0;
        int vencidas = 0;
        for (EjecucionDO ronda : rondas) {
            LocalDateTime limite = ronda.getFechaProgramada().plusMinutes(EjecucionConstant.TOLERANCIA_MINUTOS);
            Long idArea = rutinas.get(ronda.getIdRutina()).getIdArea();
            Optional<Long> operador = primero(idArea, Perfil.OPERADOR);
            if (operador.isEmpty() || ronda.getFechaProgramada().isAfter(ahora)) {
                continue;
            }
            if (ronda == aMedias) {
                avanzar(ronda, operador.get(), ahora, 2);
            } else if (limite.isAfter(ahora)) {
                continue; // todavia esta a tiempo: se queda PROGRAMADA
            } else if (azar.nextInt(100) < PORCENTAJE_COMPLETADAS) {
                completar(ronda, operador.get(), idArea, areas.get(idArea), azar, esHoy, ahora);
                completadas++;
            } else {
                Ejecucion ejecucion = toDomain(ronda);
                ejecucion.marcarVencida(ahora);
                ronda.setEstado(ejecucion.getEstado().name());
                ejecucionRepository.save(ronda);
                vencidas++;
            }
        }
        log.info("Demo {}: {} rondas completadas y {} vencidas", dia, completadas, vencidas);
    }

    private void completar(EjecucionDO ronda, Long operador, Long idArea, String area, Random azar, boolean esHoy,
                           LocalDateTime ahora) {
        List<TareaEjecucionDO> tareas = tareaEjecucionRepository.findByIdEjecucionAndActivoTrueOrderByIdAsc(ronda.getId());
        Ejecucion ejecucion = toDomain(ronda, tareas);
        ejecucion.iniciar(operador, ejecucion.getVersion());
        LocalDateTime hora = ronda.getFechaProgramada().plusMinutes(azar.nextInt(25));
        ronda.setFechaInicio(hora);
        for (TareaEjecucionDO tarea : tareas) {
            ejecucion.iniciarTarea(tarea.getIdTarea(), operador);
            ejecucion.completarTarea(tarea.getIdTarea(), operador);
            hora = hora.plusMinutes(1 + azar.nextInt(4));
            tarea.setEstado(EstadoTareaEjecucion.COMPLETADA.name());
            tarea.setFechaCompletada(hora);
            tareaEjecucionRepository.save(tarea);
        }
        ejecucion.completar();
        ronda.setEstado(ejecucion.getEstado().name());
        ronda.setIdOperador(operador);
        ronda.setFechaFin(hora);
        ejecucionRepository.save(ronda);

        int probabilidad = esHoy ? PORCENTAJE_CON_INCIDENCIA_HOY : PORCENTAJE_CON_INCIDENCIA;
        if (azar.nextInt(100) < probabilidad) {
            reportar(ronda, operador, idArea, area, azar, esHoy, ahora);
        }
    }

    private void reportar(EjecucionDO ronda, Long operador, Long idArea, String area, Random azar, boolean esHoy,
                          LocalDateTime ahora) {
        List<Problema> problemas = PROBLEMAS.getOrDefault(area, PROBLEMAS.get("MANTENIMIENTO"));
        Problema problema = problemas.get(azar.nextInt(problemas.size()));
        Incidencia incidencia = Incidencia.reportar(ronda.getId(), operador, problema.descripcion());
        IncidenciaDO entity = new IncidenciaDO();
        entity.setIdEjecucion(ronda.getId());
        entity.setIdReporta(operador);
        entity.setDescripcion(incidencia.getDescripcion());
        entity.setFechaReporte(ronda.getFechaFin().minusMinutes(1));

        Optional<Long> supervisor = primero(idArea, Perfil.SUPERVISOR);
        boolean resolver = !esHoy && azar.nextInt(100) < PORCENTAJE_RESUELTAS;
        boolean revisar = resolver || (esHoy && azar.nextBoolean());
        if (supervisor.isPresent() && revisar) {
            incidencia.tomarEnRevision(supervisor.get(), Perfil.SUPERVISOR);
            if (resolver) {
                incidencia.resolver(problema.resolucion());
                LocalDateTime resuelta = ronda.getFechaFin().plusMinutes(20 + azar.nextInt(160));
                entity.setFechaResolucion(resuelta.isAfter(ahora) ? ahora : resuelta);
            }
        }
        entity.setEstado(incidencia.getEstado().name());
        entity.setIdRevisor(incidencia.getIdRevisor());
        entity.setComentarioResolucion(incidencia.getComentarioResolucion());
        incidenciaRepository.save(entity);
    }

    /** Deja una ronda en curso con algunas tareas hechas, como si el operador siguiera en ella. */
    private void avanzar(EjecucionDO ronda, Long operador, LocalDateTime ahora, int tareasHechas) {
        List<TareaEjecucionDO> tareas = tareaEjecucionRepository.findByIdEjecucionAndActivoTrueOrderByIdAsc(ronda.getId());
        Ejecucion ejecucion = toDomain(ronda, tareas);
        ejecucion.iniciar(operador, ejecucion.getVersion());
        LocalDateTime inicio = ronda.getFechaProgramada().plusMinutes(5);
        ronda.setFechaInicio(inicio.isAfter(ahora) ? ahora : inicio);
        for (TareaEjecucionDO tarea : tareas.subList(0, Math.min(tareasHechas, tareas.size()))) {
            ejecucion.iniciarTarea(tarea.getIdTarea(), operador);
            ejecucion.completarTarea(tarea.getIdTarea(), operador);
            tarea.setEstado(EstadoTareaEjecucion.COMPLETADA.name());
            tarea.setFechaCompletada(ronda.getFechaInicio());
            tareaEjecucionRepository.save(tarea);
        }
        ronda.setEstado(ejecucion.getEstado().name());
        ronda.setIdOperador(operador);
        ejecucionRepository.save(ronda);
    }

    private Optional<Long> primero(Long idArea, Perfil perfil) {
        return usuarioRepository.findByAreaAndPerfil(idArea, perfil.name()).stream()
                .findFirst().map(UsuarioDO::getId);
    }

    private Ejecucion toDomain(EjecucionDO ronda) {
        return toDomain(ronda, tareaEjecucionRepository.findByIdEjecucionAndActivoTrueOrderByIdAsc(ronda.getId()));
    }

    private static Ejecucion toDomain(EjecucionDO ronda, List<TareaEjecucionDO> tareas) {
        Map<Long, EstadoTareaEjecucion> estados = new LinkedHashMap<>();
        tareas.forEach(tarea -> estados.put(tarea.getIdTarea(), EstadoTareaEjecucion.valueOf(tarea.getEstado())));
        return Ejecucion.reconstituir(ronda.getIdRutina(), ronda.getFechaProgramada(), estados,
                EstadoEjecucion.valueOf(ronda.getEstado()), ronda.getIdOperador(), ronda.getVersion());
    }
}
