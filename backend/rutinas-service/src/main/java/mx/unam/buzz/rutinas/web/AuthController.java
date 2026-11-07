package mx.unam.buzz.rutinas.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.security.Publico;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.auth.AuthTokenVO;
import mx.unam.buzz.rutinas.common.vo.auth.LoginRequestVO;
import mx.unam.buzz.rutinas.facade.IAuthFacade;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inicio de sesion. Devuelve el token que va en Authorization: Bearer en el resto de la API.
 */
@RestController
@RequestMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Sesion", description = "Login con correo y contrasena")
public class AuthController {

    private final IAuthFacade authFacade;

    @Publico
    @SecurityRequirements
    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Iniciar sesion", description = "Usuarios de la demo: laura@, jorge@, ana@ y luis@taskin.mx, "
            + "contrasena TaskIn2026.")
    @ApiResponse(responseCode = "200", description = "Token y datos del usuario")
    @ApiResponse(responseCode = "401", description = "Correo o contrasena incorrectos (no se dice cual)",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<SingleResponseVO<AuthTokenVO>> login(@Valid @RequestBody LoginRequestVO body) {
        return ResponseEntity.ok(authFacade.login(new RequestVO<>(body)));
    }
}
