package mx.unam.buzz.pasarela;

import mx.unam.buzz.pasarela.PasarelaSandboxDtos.CargoRespuesta;
import mx.unam.buzz.pasarela.PasarelaSandboxDtos.SolicitudCargo;
import mx.unam.buzz.pasarela.PasarelaSandboxDtos.SolicitudToken;
import mx.unam.buzz.pasarela.PasarelaSandboxDtos.TokenRespuesta;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API publica del simulador. En un proveedor real viviria en otro dominio
 * (api.pasarela.com); aqui comparte el servidor solo para que la demo arranque con un comando.
 */
@RestController
@RequestMapping("/sandbox/pasarela")
public class PasarelaSandboxController {

    private final PasarelaSandboxService servicio;

    public PasarelaSandboxController(PasarelaSandboxService servicio) {
        this.servicio = servicio;
    }

    /** Lo llama el navegador con la llave publica: la tarjeta viaja aqui y nunca al comercio. */
    @PostMapping("/tokens")
    public ResponseEntity<TokenRespuesta> tokenizar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacion,
            @RequestBody SolicitudToken solicitud) {
        servicio.exigirLlavePublica(autorizacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.tokenizar(solicitud));
    }

    /** Lo llama el backend del comercio con la llave secreta. 202: el resultado llega por webhook. */
    @PostMapping("/cargos")
    public ResponseEntity<CargoRespuesta> cobrar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacion,
            @RequestBody SolicitudCargo solicitud) {
        servicio.exigirLlaveSecreta(autorizacion);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(servicio.cobrar(solicitud));
    }

    @GetMapping("/cargos/{id}")
    public CargoRespuesta consultar(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String autorizacion,
            @PathVariable String id) {
        servicio.exigirLlaveSecreta(autorizacion);
        return servicio.consultar(id);
    }
}
