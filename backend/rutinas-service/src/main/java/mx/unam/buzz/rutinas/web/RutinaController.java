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
import mx.unam.buzz.rutinas.common.vo.ejecucion.EjecucionResponseVO;
import mx.unam.buzz.rutinas.common.vo.rutina.FindListRutinaRequestVO;
import mx.unam.buzz.rutinas.common.vo.rutina.RutinaResponseVO;
import mx.unam.buzz.rutinas.common.vo.rutina.SaveRutinaRequestVO;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import mx.unam.buzz.rutinas.facade.IEjecucionFacade;
import mx.unam.buzz.rutinas.facade.IRutinaFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
 * Parametrizacion de rutinas (el corazon del ADR). Todos leen las de sus areas; solo
 * el administrador las crea, cambia o da de baja.
 */
@RestController
@RequestMapping(path = "/rutinas", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Rutinas", description = "Que hay que hacer, en que area y cada cuando")
public class RutinaController {

    private final IRutinaFacade rutinaFacade;
    private final IEjecucionFacade ejecucionFacade;

    @GetMapping
    @Operation(summary = "Listar rutinas con sus tareas")
    public ResponseEntity<ResponseVO<RutinaResponseVO>> findList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "{validacion.page.minimo}") int page,
            @RequestParam(defaultValue = "50") @Min(value = 1, message = "{validacion.size.minimo}")
            @Max(value = CommonConstant.MAX_PAGE_SIZE, message = "{validacion.size.maximo}") int size,
            @RequestParam(defaultValue = "nombre") String orderBy,
            @RequestParam(defaultValue = "ASC") String orderType,
            @RequestParam(required = false) Long idArea,
            @RequestParam(required = false) String nombre) {
        return ResponseEntity.ok(rutinaFacade.findList(
                Paginacion.de(new FindListRutinaRequestVO(idArea, nombre), page, size, orderBy, orderType)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una rutina")
    @ApiResponse(responseCode = "404", description = "La rutina no existe o fue eliminada")
    public ResponseEntity<SingleResponseVO<RutinaResponseVO>> findDetail(@PathVariable Long id) {
        return ResponseEntity.ok(rutinaFacade.findDetail(new RequestVO<>(new IdRequestVO(id))));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequierePerfil(Perfil.ADMINISTRADOR)
    @Operation(summary = "Crear una rutina", description = "Cada frecuencia pide sus datos: CADA_2_HORAS inicio y fin, "
            + "DIARIA inicio, SEMANAL inicio y diaSemana, POR_EVENTO nada. Las rondas que faltan hoy se programan "
            + "en el acto.")
    @ApiResponse(responseCode = "201", description = "Rutina creada")
    @ApiResponse(responseCode = "409", description = "Ya existe una rutina con ese nombre en el area")
    public ResponseEntity<SingleResponseVO<RutinaResponseVO>> create(@Valid @RequestBody SaveRutinaRequestVO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rutinaFacade.create(new RequestVO<>(body)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequierePerfil(Perfil.ADMINISTRADOR)
    @Operation(summary = "Cambiar una rutina", description = "Las tareas enviadas reemplazan a las anteriores; las "
            + "rondas pendientes de hoy se rehacen.")
    public ResponseEntity<SingleResponseVO<RutinaResponseVO>> update(@PathVariable Long id,
                                                                    @Valid @RequestBody SaveRutinaRequestVO body) {
        body.setId(id);
        return ResponseEntity.ok(rutinaFacade.update(new RequestVO<>(body)));
    }

    @DeleteMapping("/{id}")
    @RequierePerfil(Perfil.ADMINISTRADOR)
    @Operation(summary = "Dar de baja una rutina", description = "Borrado logico; cancela sus rondas pendientes.")
    public ResponseEntity<SingleResponseVO<Boolean>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(rutinaFacade.delete(new RequestVO<>(new IdRequestVO(id))));
    }

    @PostMapping("/{id}/ejecuciones")
    @Operation(summary = "Lanzar una ronda por evento", description = "Solo para rutinas POR_EVENTO (por ejemplo, "
            + "limpieza por derrame): crea la ronda en este momento.")
    @ApiResponse(responseCode = "201", description = "Ronda creada")
    @ApiResponse(responseCode = "400", description = "La rutina no es POR_EVENTO")
    public ResponseEntity<SingleResponseVO<EjecucionResponseVO>> lanzar(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ejecucionFacade.lanzar(new RequestVO<>(new IdRequestVO(id))));
    }
}
