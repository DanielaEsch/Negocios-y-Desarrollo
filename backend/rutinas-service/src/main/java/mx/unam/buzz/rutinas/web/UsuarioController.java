package mx.unam.buzz.rutinas.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.CommonConstant;
import mx.unam.buzz.rutinas.common.security.RequierePerfil;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.usuario.CreateUsuarioRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.FindListUsuarioRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UpdateAreasRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UpdatePerfilRequestVO;
import mx.unam.buzz.rutinas.common.vo.usuario.UsuarioResponseVO;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import mx.unam.buzz.rutinas.facade.IUsuarioFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Usuarios del negocio. Cualquiera consulta su propio perfil; el supervisor lista a
 * la gente de sus areas y el administrador da de alta, promueve y asigna areas.
 */
@RestController
@RequestMapping(path = "/usuarios", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Equipo, perfiles y areas a cargo")
public class UsuarioController {

    private final IUsuarioFacade usuarioFacade;

    @GetMapping("/me")
    @Operation(summary = "Mi perfil")
    public ResponseEntity<SingleResponseVO<UsuarioResponseVO>> findMe() {
        return ResponseEntity.ok(usuarioFacade.findMe(new RequestVO<>()));
    }

    @GetMapping
    @RequierePerfil({Perfil.SUPERVISOR, Perfil.ADMINISTRADOR})
    @Operation(summary = "Listar usuarios", description = "El administrador recibe UsuarioAdminVO (con datos de "
            + "consentimiento); el supervisor, solo a la gente de sus areas.")
    @ApiResponse(responseCode = "403", description = "Un operador no puede listar usuarios")
    public ResponseEntity<ResponseVO<UsuarioResponseVO>> findList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "{validacion.page.minimo}") int page,
            @RequestParam(defaultValue = "50") @Min(value = 1, message = "{validacion.size.minimo}")
            @Max(value = CommonConstant.MAX_PAGE_SIZE, message = "{validacion.size.maximo}") int size,
            @RequestParam(defaultValue = "nombre") String orderBy,
            @RequestParam(defaultValue = "ASC") String orderType,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String perfil) {
        return ResponseEntity.ok(usuarioFacade.findList(
                Paginacion.de(new FindListUsuarioRequestVO(nombre, perfil), page, size, orderBy, orderType)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequierePerfil(Perfil.ADMINISTRADOR)
    @Operation(summary = "Dar de alta un usuario", description = "Entra como OPERADOR. Sin aceptar el aviso de "
            + "privacidad no se registra (LFPDPPP).")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "409", description = "Ya existe un usuario activo con ese correo")
    public ResponseEntity<SingleResponseVO<UsuarioResponseVO>> create(@Valid @RequestBody CreateUsuarioRequestVO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioFacade.create(new RequestVO<>(body)));
    }

    @PatchMapping(path = "/{id}/perfil", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequierePerfil(Perfil.ADMINISTRADOR)
    @Operation(summary = "Promover a supervisor", description = "Regla del dominio: activo, con consentimiento y con "
            + "al menos un area.")
    @ApiResponse(responseCode = "400", description = "No cumple la regla de promocion")
    public ResponseEntity<SingleResponseVO<UsuarioResponseVO>> updatePerfil(@PathVariable Long id,
                                                                           @Valid @RequestBody UpdatePerfilRequestVO body) {
        body.setId(id);
        return ResponseEntity.ok(usuarioFacade.updatePerfil(new RequestVO<>(body)));
    }

    @PutMapping(path = "/{id}/areas", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequierePerfil(Perfil.ADMINISTRADOR)
    @Operation(summary = "Reemplazar las areas a cargo")
    public ResponseEntity<SingleResponseVO<UsuarioResponseVO>> updateAreas(@PathVariable Long id,
                                                                          @Valid @RequestBody UpdateAreasRequestVO body) {
        body.setId(id);
        return ResponseEntity.ok(usuarioFacade.updateAreas(new RequestVO<>(body)));
    }

    @DeleteMapping("/{id}")
    @RequierePerfil(Perfil.ADMINISTRADOR)
    @Operation(summary = "Dar de baja un usuario (borrado logico)")
    public ResponseEntity<SingleResponseVO<Boolean>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioFacade.delete(new RequestVO<>(new IdRequestVO(id))));
    }
}
