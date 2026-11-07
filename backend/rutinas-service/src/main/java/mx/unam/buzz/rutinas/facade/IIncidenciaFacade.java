package mx.unam.buzz.rutinas.facade;

import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.CambioEstadoIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.FindListIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.IncidenciaResponseVO;

/**
 * Fachada del modulo de incidencias.
 */
public interface IIncidenciaFacade {

    ResponseVO<IncidenciaResponseVO> findList(RequestVO<FindListIncidenciaRequestVO> request);

    SingleResponseVO<IncidenciaResponseVO> cambiarEstado(RequestVO<CambioEstadoIncidenciaRequestVO> request);
}
