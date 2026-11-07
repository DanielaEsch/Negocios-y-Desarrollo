package mx.unam.buzz.rutinas.facade;

import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.auth.AuthTokenVO;
import mx.unam.buzz.rutinas.common.vo.auth.LoginRequestVO;

/**
 * Fachada de sesion. El login es el unico endpoint sin token.
 */
public interface IAuthFacade {

    SingleResponseVO<AuthTokenVO> login(RequestVO<LoginRequestVO> request);
}
