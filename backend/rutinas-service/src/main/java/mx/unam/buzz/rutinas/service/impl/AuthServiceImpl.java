package mx.unam.buzz.rutinas.service.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.AuthErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import mx.unam.buzz.rutinas.common.security.TokenService;
import mx.unam.buzz.rutinas.common.security.UsuarioSesionVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.auth.AuthTokenVO;
import mx.unam.buzz.rutinas.common.vo.auth.LoginRequestVO;
import mx.unam.buzz.rutinas.dominio.usuario.CifradorContrasena;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import mx.unam.buzz.rutinas.modelo.entity.UsuarioAreaDO;
import mx.unam.buzz.rutinas.modelo.entity.UsuarioDO;
import mx.unam.buzz.rutinas.persistence.IUsuarioAreaRepository;
import mx.unam.buzz.rutinas.persistence.IUsuarioRepository;
import mx.unam.buzz.rutinas.service.IAuthService;
import mx.unam.buzz.rutinas.service.IUsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Login contra el hash BCrypt guardado y validacion del token en cada peticion.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUsuarioRepository usuarioRepository;
    private final IUsuarioAreaRepository usuarioAreaRepository;
    private final IUsuarioService usuarioService;
    private final CifradorContrasena cifrador;
    private final TokenService tokenService;

    @Override
    @Transactional(readOnly = true)
    public SingleResponseVO<AuthTokenVO> login(RequestVO<LoginRequestVO> request) {
        LoginRequestVO parameters = request.getParameters();
        // El mismo error para correo inexistente y contrasena incorrecta: no se revela cual fallo
        UsuarioDO usuario = usuarioRepository.findByCorreoAndActivoTrue(parameters.getCorreo().trim().toLowerCase())
                .filter(found -> cifrador.coincide(parameters.getContrasena(), found.getHashContrasena()))
                .orElseThrow(() -> new BusinessException(AuthErrorCode.CREDENTIALS_INVALID.getCode(),
                        HttpStatus.UNAUTHORIZED));

        TokenService.Token token = tokenService.emitir(usuario.getId(), usuario.getPerfil());
        return SingleResponseVO.ok(new AuthTokenVO(token.valor(),
                LocalDateTime.ofInstant(token.expiraEn(), ZoneId.systemDefault()),
                usuarioService.toResponse(usuario)));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioSesionVO sesionDesdeToken(String token) {
        Long idUsuario = tokenService.validar(token);
        // Se lee de la base en cada peticion: una baja o un cambio de areas aplica de inmediato
        UsuarioDO usuario = usuarioRepository.findByIdAndActivoTrue(idUsuario)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.TOKEN_INVALID.getCode(),
                        HttpStatus.UNAUTHORIZED));
        Set<Long> areas = usuarioAreaRepository.findByIdUsuarioAndActivoTrue(idUsuario).stream()
                .map(UsuarioAreaDO::getIdArea)
                .collect(Collectors.toUnmodifiableSet());
        return new UsuarioSesionVO(usuario.getId(), usuario.getNombre(), usuario.getCorreo(),
                Perfil.valueOf(usuario.getPerfil()), areas);
    }
}
