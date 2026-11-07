package mx.unam.buzz.rutinas.facade.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.CambioEstadoIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.FindListIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.IncidenciaResponseVO;
import mx.unam.buzz.rutinas.facade.IIncidenciaFacade;
import mx.unam.buzz.rutinas.service.IIncidenciaService;
import org.springframework.stereotype.Component;

import static mx.unam.buzz.rutinas.facade.impl.FacadeSupport.conSesion;

/**
 * Agrega la sesion y delega al servicio.
 */
@Component
@RequiredArgsConstructor
public class IncidenciaFacadeImpl implements IIncidenciaFacade {

    private final IIncidenciaService incidenciaService;

    @Override
    public ResponseVO<IncidenciaResponseVO> findList(RequestVO<FindListIncidenciaRequestVO> request) {
        return incidenciaService.findList(conSesion(request));
    }

    @Override
    public SingleResponseVO<IncidenciaResponseVO> cambiarEstado(RequestVO<CambioEstadoIncidenciaRequestVO> request) {
        return incidenciaService.cambiarEstado(conSesion(request));
    }
}
