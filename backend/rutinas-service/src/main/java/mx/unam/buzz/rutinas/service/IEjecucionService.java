package mx.unam.buzz.rutinas.service;

import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.CambioEstadoEjecucionRequestVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.EjecucionResponseVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.EjecucionResumenVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.FindListEjecucionRequestVO;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Rondas: listado, detalle, maquina de estados y programacion por calendario.
 * El detalle sale sin incidencias; la fachada las agrega.
 */
public interface IEjecucionService {

    ResponseVO<EjecucionResumenVO> findList(RequestVO<FindListEjecucionRequestVO> request);

    SingleResponseVO<EjecucionResponseVO> findDetail(RequestVO<IdRequestVO> request);

    SingleResponseVO<EjecucionResponseVO> cambiarEstado(RequestVO<CambioEstadoEjecucionRequestVO> request);

    SingleResponseVO<EjecucionResponseVO> cambiarEstadoTarea(RequestVO<CambioEstadoEjecucionRequestVO> request);

    /** Crea una ronda en este momento para una rutina POR_EVENTO (el id es el de la rutina). */
    SingleResponseVO<EjecucionResponseVO> lanzar(RequestVO<IdRequestVO> request);

    /** Programa las rondas de todas las rutinas activas en ese dia, a partir de desde. Idempotente. */
    int programarDia(LocalDate dia, LocalDateTime desde);

    /** Programa las rondas de una sola rutina en ese dia, a partir de desde. Idempotente. */
    int programarRutina(Long idRutina, LocalDate dia, LocalDateTime desde);

    /** Pasa a VENCIDA las rondas PROGRAMADA cuya hora mas la tolerancia ya paso. */
    int marcarVencidas(LocalDateTime ahora);

    /** Cancela las rondas PROGRAMADA futuras de una rutina (al darla de baja o cambiarla). */
    int cancelarProgramadas(Long idRutina, LocalDateTime desde);
}
