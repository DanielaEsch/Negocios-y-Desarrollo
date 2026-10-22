package mx.unam.buzz.rutinas.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.CommonConstant;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.area.AreaResponseVO;
import mx.unam.buzz.rutinas.common.vo.area.CreateAreaRequestVO;
import mx.unam.buzz.rutinas.common.vo.area.FindListAreaRequestVO;
import mx.unam.buzz.rutinas.common.vo.area.UpdateAreaRequestVO;
import mx.unam.buzz.rutinas.facade.IAreaFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints del modulo de areas. Traduce HTTP a RequestVO y no decide nada.
 * La forma de la entrada (@Valid y restricciones) se valida aqui; las reglas de negocio, en el servicio.
 */
@RestController
@RequestMapping(path = "/areas", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Areas", description = "Alta, consulta y baja de las areas del negocio")
@ApiResponse(responseCode = "400", description = "La peticion no cumple el contrato (RFC 7807)",
        content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class)))
public class AreaController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final IAreaFacade areaFacade;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Crear un area")
    @ApiResponse(responseCode = "201", description = "Area creada")
    @ApiResponse(responseCode = "409", description = "Ya existe un area activa con ese nombre",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<SingleResponseVO<AreaResponseVO>> create(@Valid @RequestBody CreateAreaRequestVO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(areaFacade.create(new RequestVO<>(body)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Actualizar un area")
    @ApiResponse(responseCode = "200", description = "Area actualizada")
    @ApiResponse(responseCode = "404", description = "El area no existe o fue eliminada",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Ya existe un area activa con ese nombre",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<SingleResponseVO<Boolean>> update(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateAreaRequestVO body) {
        body.setId(id);
        return ResponseEntity.ok(areaFacade.update(new RequestVO<>(body)));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Eliminar un area (borrado logico)")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "El area no existe o fue eliminada",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<SingleResponseVO<Boolean>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(areaFacade.delete(new RequestVO<>(new IdRequestVO(id))));
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Consultar un area por id")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "El area no existe o fue eliminada",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<SingleResponseVO<AreaResponseVO>> findDetail(@PathVariable Long id) {
        return ResponseEntity.ok(areaFacade.findDetail(new RequestVO<>(new IdRequestVO(id))));
    }

    @GetMapping
    @Operation(summary = "Listar areas", description = "Paginado, page empieza en 1. Ejemplo: ?nombre=cocina&page=1&size=20")
    public ResponseEntity<ResponseVO<AreaResponseVO>> findList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "{validacion.page.minimo}") int page,
            @RequestParam(defaultValue = "50")
            @Min(value = 1, message = "{validacion.size.minimo}")
            @Max(value = CommonConstant.MAX_PAGE_SIZE, message = "{validacion.size.maximo}") int size,
            @RequestParam(defaultValue = "id") String orderBy,
            @RequestParam(defaultValue = "ASC") String orderType,
            @RequestParam(required = false) String nombre) {

        RequestVO<FindListAreaRequestVO> request = new RequestVO<>(new FindListAreaRequestVO(nombre));
        request.setPage(Math.max(page - 1, 0));
        request.setSize(size);
        request.setOrderBy(orderBy);
        request.setOrderType(orderType);
        return ResponseEntity.ok(areaFacade.findList(request));
    }
}
