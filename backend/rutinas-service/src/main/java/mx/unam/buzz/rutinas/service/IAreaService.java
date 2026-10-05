package mx.unam.buzz.rutinas.service;

import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.area.AreaResponseVO;
import mx.unam.buzz.rutinas.common.vo.area.CreateAreaRequestVO;
import mx.unam.buzz.rutinas.common.vo.area.FindListAreaRequestVO;
import mx.unam.buzz.rutinas.common.vo.area.UpdateAreaRequestVO;

public interface IAreaService {

    SingleResponseVO<AreaResponseVO> create(RequestVO<CreateAreaRequestVO> request);

    SingleResponseVO<Boolean> update(RequestVO<UpdateAreaRequestVO> request);

    SingleResponseVO<Boolean> delete(RequestVO<IdRequestVO> request);

    SingleResponseVO<AreaResponseVO> findDetail(RequestVO<IdRequestVO> request);

    ResponseVO<AreaResponseVO> findList(RequestVO<FindListAreaRequestVO> request);
}
