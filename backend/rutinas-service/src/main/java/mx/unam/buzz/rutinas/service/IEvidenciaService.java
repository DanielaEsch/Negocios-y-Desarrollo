package mx.unam.buzz.rutinas.service;

import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.ArchivoVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.EvidenciaVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.SubirEvidenciaRequestVO;

import java.util.List;

/**
 * Fotos de evidencia de las rondas.
 */
public interface IEvidenciaService {

    SingleResponseVO<EvidenciaVO> adjuntar(RequestVO<SubirEvidenciaRequestVO> request);

    SingleResponseVO<List<EvidenciaVO>> findByEjecucion(RequestVO<IdRequestVO> request);

    ArchivoVO archivo(RequestVO<IdRequestVO> request);
}
