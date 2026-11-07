package mx.unam.buzz.rutinas.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.security.RequierePerfil;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.tablero.TableroVO;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import mx.unam.buzz.rutinas.facade.ITableroFacade;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Tablero de cumplimiento para quien supervisa.
 */
@RestController
@RequestMapping(path = "/tablero", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Tablero", description = "Cumplimiento del dia, por area y de la ultima semana")
public class TableroController {

    private final ITableroFacade tableroFacade;

    @GetMapping
    @RequierePerfil({Perfil.SUPERVISOR, Perfil.ADMINISTRADOR})
    @Operation(summary = "Resumen del dia", description = "Cumplimiento = completadas / (completadas + vencidas).")
    public ResponseEntity<SingleResponseVO<TableroVO>> resumen(
            @Parameter(example = "2026-11-05") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(tableroFacade.resumen(new RequestVO<>(fecha)));
    }
}
