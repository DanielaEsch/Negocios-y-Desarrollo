package mx.unam.buzz.rutinas.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.CommonConstant;
import mx.unam.buzz.rutinas.common.security.RequierePerfil;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.CambioEstadoIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.FindListIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.IncidenciaResponseVO;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import mx.unam.buzz.rutinas.facade.IIncidenciaFacade;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Incidencias. El operador ve las que reporto; el supervisor, las de sus areas, y es
 * quien las revisa y resuelve.
 */
@RestController
@RequestMapping(path = "/incidencias", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Incidencias", description = "Seguimiento de lo que salio mal: PENDIENTE -> REVISION -> RESUELTA")
public class IncidenciaController {

    private final IIncidenciaFacade incidenciaFacade;

    @GetMapping
    @Operation(summary = "Listar incidencias", description = "Operador: IncidenciaResponseVO de las que reporto. "
            + "Supervisor y administrador: IncidenciaDetalleVO (con reporta y revisor).")
    public ResponseEntity<ResponseVO<IncidenciaResponseVO>> findList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "{validacion.page.minimo}") int page,
            @RequestParam(defaultValue = "50") @Min(value = 1, message = "{validacion.size.minimo}")
            @Max(value = CommonConstant.MAX_PAGE_SIZE, message = "{validacion.size.maximo}") int size,
            @RequestParam(defaultValue = "fechaReporte") String orderBy,
            @RequestParam(defaultValue = "DESC") String orderType,
            @Parameter(example = "PENDIENTE") @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long idArea) {
        return ResponseEntity.ok(incidenciaFacade.findList(
                Paginacion.de(new FindListIncidenciaRequestVO(estado, idArea), page, size, orderBy, orderType)));
    }

    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequierePerfil({Perfil.SUPERVISOR, Perfil.ADMINISTRADOR})
    @Operation(summary = "Revisar o resolver", description = "REVISION la toma quien pide; RESUELTA exige "
            + "comentarioResolucion.")
    @ApiResponse(responseCode = "403", description = "Un operador no puede revisar incidencias")
    @ApiResponse(responseCode = "409", description = "Transicion invalida (por ejemplo, resolver sin revisar)")
    public ResponseEntity<SingleResponseVO<IncidenciaResponseVO>> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody CambioEstadoIncidenciaRequestVO body) {
        body.setId(id);
        return ResponseEntity.ok(incidenciaFacade.cambiarEstado(new RequestVO<>(body)));
    }
}
