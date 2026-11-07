package mx.unam.buzz.rutinas.facade.impl;

import mx.unam.buzz.rutinas.common.constant.AuthErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import mx.unam.buzz.rutinas.common.security.SesionContexto;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import org.springframework.http.HttpStatus;

/**
 * La fachada es la que sabe quien pide: toma la sesion de la peticion en curso y la
 * pone en RequestVO para que el servicio no dependa del contexto HTTP.
 */
final class FacadeSupport {

    private FacadeSupport() {
    }

    static <T> RequestVO<T> conSesion(RequestVO<T> request) {
        request.setSesion(SesionContexto.actual().orElseThrow(
                () -> new BusinessException(AuthErrorCode.TOKEN_REQUIRED.getCode(), HttpStatus.UNAUTHORIZED)));
        return request;
    }
}
