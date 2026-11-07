package mx.unam.buzz.rutinas.service.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.EjecucionConstant;
import mx.unam.buzz.rutinas.common.security.Alcance;
import mx.unam.buzz.rutinas.common.security.UsuarioSesionVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.tablero.ConteoVO;
import mx.unam.buzz.rutinas.common.vo.tablero.TableroVO;
import mx.unam.buzz.rutinas.dominio.ejecucion.EstadoEjecucion;
import mx.unam.buzz.rutinas.persistence.IEjecucionRepository;
import mx.unam.buzz.rutinas.persistence.IIncidenciaRepository;
import mx.unam.buzz.rutinas.service.ITableroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Estadisticas de cumplimiento (modulo 7 del ADR). El cumplimiento cuenta solo las
 * rondas que ya se decidieron: completadas entre completadas mas vencidas. Una
 * ronda que todavia esta a tiempo no baja el porcentaje.
 */
@Service
@RequiredArgsConstructor
public class TableroServiceImpl implements ITableroService {

    private final IEjecucionRepository ejecucionRepository;
    private final IIncidenciaRepository incidenciaRepository;
    private final ConsultaNombres nombres;

    @Override
    @Transactional(readOnly = true)
    public SingleResponseVO<TableroVO> resumen(RequestVO<LocalDate> request) {
        UsuarioSesionVO sesion = request.getSesion();
        LocalDate dia = request.getParameters() != null ? request.getParameters() : LocalDate.now();
        boolean todas = Alcance.sinFiltro(sesion, null);
        List<Long> areas = Alcance.areas(sesion, null);

        // Por area
        Map<Long, ConteoVO> porArea = new LinkedHashMap<>();
        for (Object[] row : ejecucionRepository.countByAreaAndEstado(dia.atStartOfDay(),
                dia.plusDays(1).atStartOfDay(), todas, areas)) {
            ConteoVO conteo = porArea.computeIfAbsent((Long) row[0], id -> new ConteoVO());
            sumar(conteo, (String) row[1], ((Number) row[2]).intValue());
        }
        Map<Long, String> nombresArea = nombres.areas(porArea.keySet());
        porArea.forEach((idArea, conteo) -> {
            conteo.setNombre(nombresArea.get(idArea));
            conteo.setFecha(dia);
            cerrar(conteo);
        });

        // Totales del dia
        ConteoVO total = new ConteoVO();
        total.setFecha(dia);
        porArea.values().forEach(conteo -> {
            sumar(total, EstadoEjecucion.COMPLETADA.name(), conteo.getCompletadas());
            sumar(total, EstadoEjecucion.EN_CURSO.name(), conteo.getEnCurso());
            sumar(total, EstadoEjecucion.PROGRAMADA.name(), conteo.getProgramadas());
            sumar(total, EstadoEjecucion.VENCIDA.name(), conteo.getVencidas());
            sumar(total, EstadoEjecucion.CANCELADA.name(), conteo.getCanceladas());
        });
        cerrar(total);

        // Tendencia de los ultimos dias, uno por dia aunque no haya rondas
        LocalDate desde = dia.minusDays(EjecucionConstant.DIAS_TENDENCIA - 1L);
        Map<LocalDate, ConteoVO> tendencia = new TreeMap<>();
        for (LocalDate fecha = desde; !fecha.isAfter(dia); fecha = fecha.plusDays(1)) {
            ConteoVO conteo = new ConteoVO();
            conteo.setFecha(fecha);
            tendencia.put(fecha, conteo);
        }
        for (Object[] row : ejecucionRepository.findEstados(desde.atStartOfDay(), dia.plusDays(1).atStartOfDay(),
                todas, areas)) {
            sumar(tendencia.get(((LocalDateTime) row[0]).toLocalDate()), (String) row[1], 1);
        }
        tendencia.values().forEach(TableroServiceImpl::cerrar);

        TableroVO tablero = new TableroVO();
        tablero.setFecha(dia);
        tablero.setDia(total);
        List<ConteoVO> areasOrdenadas = new ArrayList<>(porArea.values());
        areasOrdenadas.sort(Comparator.comparing(ConteoVO::getNombre, Comparator.nullsLast(Comparator.naturalOrder())));
        tablero.setPorArea(areasOrdenadas);
        tablero.setTendencia(new ArrayList<>(tendencia.values()));
        tablero.setIncidenciasAbiertas(incidenciaRepository.countAbiertas(todas, areas));
        return SingleResponseVO.ok(tablero);
    }

    private static void sumar(ConteoVO conteo, String estado, int cantidad) {
        switch (EstadoEjecucion.valueOf(estado)) {
            case COMPLETADA -> conteo.setCompletadas(conteo.getCompletadas() + cantidad);
            case EN_CURSO -> conteo.setEnCurso(conteo.getEnCurso() + cantidad);
            case PROGRAMADA -> conteo.setProgramadas(conteo.getProgramadas() + cantidad);
            case VENCIDA -> conteo.setVencidas(conteo.getVencidas() + cantidad);
            case CANCELADA -> conteo.setCanceladas(conteo.getCanceladas() + cantidad);
        }
    }

    private static void cerrar(ConteoVO conteo) {
        conteo.setTotal(conteo.getCompletadas() + conteo.getEnCurso() + conteo.getProgramadas()
                + conteo.getVencidas() + conteo.getCanceladas());
        int decididas = conteo.getCompletadas() + conteo.getVencidas();
        conteo.setCumplimiento(decididas == 0 ? null
                : Math.round(conteo.getCompletadas() * 1000.0 / decididas) / 10.0);
    }
}
