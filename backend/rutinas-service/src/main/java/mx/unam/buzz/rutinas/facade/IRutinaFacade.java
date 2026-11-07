package mx.unam.buzz.rutinas.facade;

import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.rutina.FindListRutinaRequestVO;
import mx.unam.buzz.rutinas.common.vo.rutina.RutinaResponseVO;
import mx.unam.buzz.rutinas.common.vo.rutina.SaveRutinaRequestVO;

/**
 * Fachada del modulo de rutinas. Orquesta rutina y programacion: una rutina nueva
 * aparece en las rondas de hoy sin esperar a la medianoche.
 */
public interface IRutinaFacade {

    ResponseVO<RutinaResponseVO> findList(RequestVO<FindListRutinaRequestVO> request);

    SingleResponseVO<RutinaResponseVO> findDetail(RequestVO<IdRequestVO> request);

    SingleResponseVO<RutinaResponseVO> create(RequestVO<SaveRutinaRequestVO> request);

    SingleResponseVO<RutinaResponseVO> update(RequestVO<SaveRutinaRequestVO> request);

    SingleResponseVO<Boolean> delete(RequestVO<IdRequestVO> request);
}
