package mx.unam.buzz.rutinas.service;

import mx.unam.buzz.rutinas.common.security.UsuarioSesionVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.CambioEstadoIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.CreateIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.FindListIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.IncidenciaResponseVO;

import java.util.List;

public interface IIncidenciaService {

    SingleResponseVO<IncidenciaResponseVO> create(RequestVO<CreateIncidenciaRequestVO> request);

    ResponseVO<IncidenciaResponseVO> findList(RequestVO<FindListIncidenciaRequestVO> request);

    SingleResponseVO<IncidenciaResponseVO> cambiarEstado(RequestVO<CambioEstadoIncidenciaRequestVO> request);

    /** Incidencias de una ronda, con el DTO que corresponde al perfil de quien pregunta. */
    List<IncidenciaResponseVO> findByEjecucion(Long idEjecucion, UsuarioSesionVO sesion);
}
