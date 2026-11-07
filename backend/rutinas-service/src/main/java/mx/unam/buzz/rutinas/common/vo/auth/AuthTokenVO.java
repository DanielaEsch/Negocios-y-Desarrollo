package mx.unam.buzz.rutinas.common.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.unam.buzz.rutinas.common.vo.usuario.UsuarioResponseVO;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Respuesta del login: el token Bearer y quien es el usuario.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Token para el header Authorization: Bearer <token>")
    private String token;

    @Schema(description = "Fecha y hora en que expira el token", example = "2026-11-05T22:00:00")
    private LocalDateTime expiraEn;

    @Schema(description = "Usuario autenticado")
    private UsuarioResponseVO usuario;
}
