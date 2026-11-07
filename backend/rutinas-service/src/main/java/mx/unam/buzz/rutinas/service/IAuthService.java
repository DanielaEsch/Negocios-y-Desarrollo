package mx.unam.buzz.rutinas.service;

import mx.unam.buzz.rutinas.common.security.UsuarioSesionVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.auth.AuthTokenVO;
import mx.unam.buzz.rutinas.common.vo.auth.LoginRequestVO;

public interface IAuthService {

    SingleResponseVO<AuthTokenVO> login(RequestVO<LoginRequestVO> request);

    /** Valida el token y arma la sesion con el perfil y las areas vigentes del usuario. */
    UsuarioSesionVO sesionDesdeToken(String token);
}
