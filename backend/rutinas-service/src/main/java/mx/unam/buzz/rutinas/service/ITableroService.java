package mx.unam.buzz.rutinas.service;

import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.tablero.TableroVO;

import java.time.LocalDate;

public interface ITableroService {

    /** Cumplimiento del dia, por area y de los ultimos dias, en las areas de quien pregunta. */
    SingleResponseVO<TableroVO> resumen(RequestVO<LocalDate> request);
}
