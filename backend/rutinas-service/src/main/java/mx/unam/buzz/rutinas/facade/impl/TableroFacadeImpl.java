package mx.unam.buzz.rutinas.facade.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.tablero.TableroVO;
import mx.unam.buzz.rutinas.facade.ITableroFacade;
import mx.unam.buzz.rutinas.service.ITableroService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static mx.unam.buzz.rutinas.facade.impl.FacadeSupport.conSesion;

/**
 * Agrega la sesion y delega al servicio.
 */
@Component
@RequiredArgsConstructor
public class TableroFacadeImpl implements ITableroFacade {

    private final ITableroService tableroService;

    @Override
    public SingleResponseVO<TableroVO> resumen(RequestVO<LocalDate> request) {
        return tableroService.resumen(conSesion(request));
    }
}
