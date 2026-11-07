package mx.unam.buzz.rutinas.service;

import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.rutina.FindListRutinaRequestVO;
import mx.unam.buzz.rutinas.common.vo.rutina.RutinaResponseVO;
import mx.unam.buzz.rutinas.common.vo.rutina.SaveRutinaRequestVO;

public interface IRutinaService {

    ResponseVO<RutinaResponseVO> findList(RequestVO<FindListRutinaRequestVO> request);

    SingleResponseVO<RutinaResponseVO> findDetail(RequestVO<IdRequestVO> request);

    SingleResponseVO<RutinaResponseVO> create(RequestVO<SaveRutinaRequestVO> request);

    SingleResponseVO<RutinaResponseVO> update(RequestVO<SaveRutinaRequestVO> request);

    SingleResponseVO<Boolean> delete(RequestVO<IdRequestVO> request);
}
