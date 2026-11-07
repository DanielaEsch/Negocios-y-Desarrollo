package mx.unam.buzz.rutinas.facade;

import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.tablero.TableroVO;

import java.time.LocalDate;

/**
 * Fachada del tablero de cumplimiento.
 */
public interface ITableroFacade {

    SingleResponseVO<TableroVO> resumen(RequestVO<LocalDate> request);
}
