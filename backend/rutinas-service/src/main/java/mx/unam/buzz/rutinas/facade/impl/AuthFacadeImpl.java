package mx.unam.buzz.rutinas.facade.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.auth.AuthTokenVO;
import mx.unam.buzz.rutinas.common.vo.auth.LoginRequestVO;
import mx.unam.buzz.rutinas.facade.IAuthFacade;
import mx.unam.buzz.rutinas.service.IAuthService;
import org.springframework.stereotype.Component;

/**
 * Delega el login; no necesita sesion.
 */
@Component
@RequiredArgsConstructor
public class AuthFacadeImpl implements IAuthFacade {

    private final IAuthService authService;

    @Override
    public SingleResponseVO<AuthTokenVO> login(RequestVO<LoginRequestVO> request) {
        return authService.login(request);
    }
}
