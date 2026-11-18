package mx.unam.buzz.rutinas.facade.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.ArchivoVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.EvidenciaVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.SubirEvidenciaRequestVO;
import mx.unam.buzz.rutinas.facade.IEvidenciaFacade;
import mx.unam.buzz.rutinas.service.IEvidenciaService;
import org.springframework.stereotype.Component;

import java.util.List;

import static mx.unam.buzz.rutinas.facade.impl.FacadeSupport.conSesion;

/**
 * Agrega la sesion y delega al servicio.
 */
@Component
@RequiredArgsConstructor
public class EvidenciaFacadeImpl implements IEvidenciaFacade {

    private final IEvidenciaService evidenciaService;

    @Override
    public SingleResponseVO<EvidenciaVO> adjuntar(RequestVO<SubirEvidenciaRequestVO> request) {
        return evidenciaService.adjuntar(conSesion(request));
    }

    @Override
    public SingleResponseVO<List<EvidenciaVO>> findByEjecucion(RequestVO<IdRequestVO> request) {
        return evidenciaService.findByEjecucion(conSesion(request));
    }

    @Override
    public ArchivoVO archivo(RequestVO<IdRequestVO> request) {
        return evidenciaService.archivo(conSesion(request));
    }
}
