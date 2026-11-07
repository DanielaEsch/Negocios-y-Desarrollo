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
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.CambioEstadoEjecucionRequestVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.EjecucionResponseVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.EjecucionResumenVO;
import mx.unam.buzz.rutinas.common.vo.ejecucion.FindListEjecucionRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.CreateIncidenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.incidencia.IncidenciaResponseVO;
import mx.unam.buzz.rutinas.facade.IEjecucionFacade;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Rondas del dia. Los cambios de estado son PATCH sobre el recurso con la version
 * leida: el servidor aplica la maquina de estados del dominio y responde 409 si la
 * transicion no es valida o si alguien mas modifico la ronda.
 */
@RestController
@RequestMapping(path = "/ejecuciones", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Rondas", description = "Ejecucion de rutinas: tomar, avanzar tareas, terminar y reportar incidencias")
public class EjecucionController {

    private final IEjecucionFacade ejecucionFacade;

    @GetMapping
    @Operation(summary = "Rondas de un dia", description = "Sin fecha, hoy. Solo las de las areas de quien pregunta.")
    public ResponseEntity<ResponseVO<EjecucionResumenVO>> findList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "{validacion.page.minimo}") int page,
            @RequestParam(defaultValue = "100") @Min(value = 1, message = "{validacion.size.minimo}")
            @Max(value = CommonConstant.MAX_PAGE_SIZE, message = "{validacion.size.maximo}") int size,
            @RequestParam(defaultValue = "fechaProgramada") String orderBy,
            @RequestParam(defaultValue = "ASC") String orderType,
            @Parameter(example = "2026-11-05") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @Parameter(example = "PROGRAMADA") @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long idArea,
            @Parameter(description = "Solo las que tomo quien pregunta") @RequestParam(defaultValue = "false")
            boolean soloMias) {
        return ResponseEntity.ok(ejecucionFacade.findList(Paginacion.de(
                new FindListEjecucionRequestVO(fecha, estado, idArea, soloMias), page, size, orderBy, orderType)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una ronda", description = "Con sus tareas e incidencias.")
    @ApiResponse(responseCode = "404", description = "La ronda no existe")
    @ApiResponse(responseCode = "403", description = "La ronda es de un area que no tienes asignada")
    public ResponseEntity<SingleResponseVO<EjecucionResponseVO>> findDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ejecucionFacade.findDetail(new RequestVO<>(new IdRequestVO(id))));
    }

    @PatchMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cambiar el estado de la ronda", description = "EN_CURSO la toma quien pide; COMPLETADA "
            + "exige todas las tareas hechas; CANCELADA solo supervisor o administrador.")
    @ApiResponse(responseCode = "409", description = "Transicion invalida u otra persona la modifico primero")
    public ResponseEntity<SingleResponseVO<EjecucionResponseVO>> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody CambioEstadoEjecucionRequestVO body) {
        body.setId(id);
        return ResponseEntity.ok(ejecucionFacade.cambiarEstado(new RequestVO<>(body)));
    }

    @PatchMapping(path = "/{id}/tareas/{idTarea}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Avanzar una tarea", description = "EN_PROCESO o COMPLETADA. Solo quien tomo la ronda.")
    @ApiResponse(responseCode = "409", description = "Transicion invalida u otra persona la modifico primero")
    public ResponseEntity<SingleResponseVO<EjecucionResponseVO>> cambiarEstadoTarea(
            @PathVariable Long id, @PathVariable Long idTarea,
            @Valid @RequestBody CambioEstadoEjecucionRequestVO body) {
        body.setId(id);
        body.setIdTarea(idTarea);
        return ResponseEntity.ok(ejecucionFacade.cambiarEstadoTarea(new RequestVO<>(body)));
    }

    @PostMapping(path = "/{id}/incidencias", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Reportar una incidencia en la ronda")
    @ApiResponse(responseCode = "201", description = "Incidencia reportada en PENDIENTE")
    public ResponseEntity<SingleResponseVO<IncidenciaResponseVO>> reportar(
            @PathVariable Long id, @Valid @RequestBody CreateIncidenciaRequestVO body) {
        body.setIdEjecucion(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ejecucionFacade.reportar(new RequestVO<>(body)));
    }
}
