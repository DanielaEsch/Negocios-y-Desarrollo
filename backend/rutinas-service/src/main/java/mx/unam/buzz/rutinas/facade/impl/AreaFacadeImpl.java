package mx.unam.buzz.rutinas.facade.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.area.AreaResponseVO;
import mx.unam.buzz.rutinas.common.vo.area.CreateAreaRequestVO;
import mx.unam.buzz.rutinas.common.vo.area.FindListAreaRequestVO;
import mx.unam.buzz.rutinas.common.vo.area.UpdateAreaRequestVO;
import mx.unam.buzz.rutinas.facade.IAreaFacade;
import mx.unam.buzz.rutinas.service.IAreaService;
import org.springframework.stereotype.Component;

/**
 * Hoy solo delega al servicio. Cuando una operacion necesite orquestar varios
 * servicios (por ejemplo, cerrar una ejecucion y notificar), se hace aqui.
 */
@Component
@RequiredArgsConstructor
public class AreaFacadeImpl implements IAreaFacade {

    private final IAreaService areaService;

    @Override
    public SingleResponseVO<AreaResponseVO> create(RequestVO<CreateAreaRequestVO> request) {
        return areaService.create(request);
    }

    @Override
    public SingleResponseVO<Boolean> update(RequestVO<UpdateAreaRequestVO> request) {
        return areaService.update(request);
    }

    @Override
    public SingleResponseVO<Boolean> delete(RequestVO<IdRequestVO> request) {
        return areaService.delete(request);
    }

    @Override
    public SingleResponseVO<AreaResponseVO> findDetail(RequestVO<IdRequestVO> request) {
        return areaService.findDetail(request);
    }

    @Override
    public ResponseVO<AreaResponseVO> findList(RequestVO<FindListAreaRequestVO> request) {
        return areaService.findList(request);
    }
}
