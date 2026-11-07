package mx.unam.buzz.rutinas.facade;

import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.CambioEstadoEjecucionRequestVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.EjecucionResponseVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.EjecucionResumenVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.FindListEjecucionRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.CreateIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.IncidenciaResponseVO;

/**
 * Fachada de rondas. Orquesta dos servicios: el detalle de una ronda lleva sus
 * incidencias, que son de otro modulo.
 */
public interface IEjecucionFacade {

    ResponseVO<EjecucionResumenVO> findList(RequestVO<FindListEjecucionRequestVO> request);

    SingleResponseVO<EjecucionResponseVO> findDetail(RequestVO<IdRequestVO> request);

    SingleResponseVO<EjecucionResponseVO> cambiarEstado(RequestVO<CambioEstadoEjecucionRequestVO> request);

    SingleResponseVO<EjecucionResponseVO> cambiarEstadoTarea(RequestVO<CambioEstadoEjecucionRequestVO> request);

    SingleResponseVO<EjecucionResponseVO> lanzar(RequestVO<IdRequestVO> request);

    SingleResponseVO<IncidenciaResponseVO> reportar(RequestVO<CreateIncidenciaRequestVO> request);
}
