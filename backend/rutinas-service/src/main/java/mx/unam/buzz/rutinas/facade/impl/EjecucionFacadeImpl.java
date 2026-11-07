package mx.unam.buzz.rutinas.facade.impl;

import lombok.RequiredArgsConstructor;
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
import mx.unam.buzz.rutinas.facade.IEjecucionFacade;
import mx.unam.buzz.rutinas.service.IEjecucionService;
import mx.unam.buzz.rutinas.service.IIncidenciaService;
import org.springframework.stereotype.Component;

import static mx.unam.buzz.rutinas.facade.impl.FacadeSupport.conSesion;

/**
 * Rondas mas sus incidencias.
 */
@Component
@RequiredArgsConstructor
public class EjecucionFacadeImpl implements IEjecucionFacade {

    private final IEjecucionService ejecucionService;
    private final IIncidenciaService incidenciaService;

    @Override
    public ResponseVO<EjecucionResumenVO> findList(RequestVO<FindListEjecucionRequestVO> request) {
        return ejecucionService.findList(conSesion(request));
    }

    @Override
    public SingleResponseVO<EjecucionResponseVO> findDetail(RequestVO<IdRequestVO> request) {
        return conIncidencias(ejecucionService.findDetail(conSesion(request)), request);
    }

    @Override
    public SingleResponseVO<EjecucionResponseVO> cambiarEstado(RequestVO<CambioEstadoEjecucionRequestVO> request) {
        return conIncidencias(ejecucionService.cambiarEstado(conSesion(request)), request);
    }

    @Override
    public SingleResponseVO<EjecucionResponseVO> cambiarEstadoTarea(RequestVO<CambioEstadoEjecucionRequestVO> request) {
        return conIncidencias(ejecucionService.cambiarEstadoTarea(conSesion(request)), request);
    }

    @Override
    public SingleResponseVO<EjecucionResponseVO> lanzar(RequestVO<IdRequestVO> request) {
        return conIncidencias(ejecucionService.lanzar(conSesion(request)), request);
    }

    @Override
    public SingleResponseVO<IncidenciaResponseVO> reportar(RequestVO<CreateIncidenciaRequestVO> request) {
        return incidenciaService.create(conSesion(request));
    }

    private SingleResponseVO<EjecucionResponseVO> conIncidencias(SingleResponseVO<EjecucionResponseVO> response,
                                                                 RequestVO<?> request) {
        EjecucionResponseVO detalle = response.getData();
        detalle.setIncidencias(incidenciaService.findByEjecucion(detalle.getId(), request.getSesion()));
        return response;
    }
}
