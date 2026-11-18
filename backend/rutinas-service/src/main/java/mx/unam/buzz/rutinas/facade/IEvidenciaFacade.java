package mx.unam.buzz.rutinas.facade;

import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.ArchivoVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.EvidenciaVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.SubirEvidenciaRequestVO;

import java.util.List;

public interface IEvidenciaFacade {

    SingleResponseVO<EvidenciaVO> adjuntar(RequestVO<SubirEvidenciaRequestVO> request);

    SingleResponseVO<List<EvidenciaVO>> findByEjecucion(RequestVO<IdRequestVO> request);

    ArchivoVO archivo(RequestVO<IdRequestVO> request);
}
