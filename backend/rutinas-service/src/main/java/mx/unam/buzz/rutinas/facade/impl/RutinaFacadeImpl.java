package mx.unam.buzz.rutinas.facade.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.EjecucionConstant;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.rutina.FindListRutinaRequestVO;
import mx.unam.buzz.rutinas.common.vo.rutina.RutinaResponseVO;
import mx.unam.buzz.rutinas.common.vo.rutina.SaveRutinaRequestVO;
import mx.unam.buzz.rutinas.facade.IRutinaFacade;
import mx.unam.buzz.rutinas.service.IEjecucionService;
import mx.unam.buzz.rutinas.service.IRutinaService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static mx.unam.buzz.rutinas.facade.impl.FacadeSupport.conSesion;

/**
 * Rutina mas programacion del dia.
 */
@Component
@RequiredArgsConstructor
public class RutinaFacadeImpl implements IRutinaFacade {

    private final IRutinaService rutinaService;
    private final IEjecucionService ejecucionService;

    @Override
    public ResponseVO<RutinaResponseVO> findList(RequestVO<FindListRutinaRequestVO> request) {
        return rutinaService.findList(conSesion(request));
    }

    @Override
    public SingleResponseVO<RutinaResponseVO> findDetail(RequestVO<IdRequestVO> request) {
        return rutinaService.findDetail(conSesion(request));
    }

    @Override
    public SingleResponseVO<RutinaResponseVO> create(RequestVO<SaveRutinaRequestVO> request) {
        SingleResponseVO<RutinaResponseVO> response = rutinaService.create(conSesion(request));
        ejecucionService.programarRutina(response.getData().getId(), LocalDate.now(), desde());
        return response;
    }

    @Override
    public SingleResponseVO<RutinaResponseVO> update(RequestVO<SaveRutinaRequestVO> request) {
        SingleResponseVO<RutinaResponseVO> response = rutinaService.update(conSesion(request));
        // Las rondas pendientes de hoy se rehacen con el horario y las tareas nuevas
        ejecucionService.cancelarProgramadas(response.getData().getId(), LocalDateTime.now());
        ejecucionService.programarRutina(response.getData().getId(), LocalDate.now(), LocalDateTime.now());
        return response;
    }

    @Override
    public SingleResponseVO<Boolean> delete(RequestVO<IdRequestVO> request) {
        ejecucionService.cancelarProgramadas(request.getParameters().getId(), LocalDateTime.now());
        return rutinaService.delete(conSesion(request));
    }

    /** Desde cuando programar hoy: lo que ya vencio no se crea. */
    private static LocalDateTime desde() {
        return LocalDateTime.now().minusMinutes(EjecucionConstant.TOLERANCIA_MINUTOS);
    }
}
