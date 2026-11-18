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
import mx.unam.buzz.rutinas.common.security.Publico;
import mx.unam.buzz.rutinas.common.security.RequierePerfil;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.pago.CobroTarjetaRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.CotizacionRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.CotizacionVO;
import mx.unam.buzz.rutinas.common.vo.pago.FacturaXmlVO;
import mx.unam.buzz.rutinas.common.vo.pago.FindListOrdenRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.OrdenPagoRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.OrdenPagoVO;
import mx.unam.buzz.rutinas.common.vo.pago.PlanVO;
import mx.unam.buzz.rutinas.common.vo.pago.SuscripcionVO;
import mx.unam.buzz.rutinas.common.vo.pago.TransferenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.ValidacionPagoRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.WebhookRespuestaVO;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import mx.unam.buzz.rutinas.facade.IPagoFacade;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Suscripcion y pagos de TaskIn. El administrador del negocio compra; Cobranza (personal
 * de TaskIn) valida transferencias; la pasarela avisa por webhook.
 */
@RestController
@RequestMapping(path = "/pagos", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Planes, ordenes de pago (tarjeta o transferencia), webhook y CFDI")
public class PagoController {

    private final IPagoFacade pagoFacade;

    @GetMapping("/planes")
    @RequierePerfil(Perfil.ADMINISTRADOR)
    @Operation(summary = "Planes a la venta", description = "Precios en MXN sin IVA; el anual trae 2 meses de regalo.")
    public ResponseEntity<SingleResponseVO<List<PlanVO>>> planes() {
        return ResponseEntity.ok(pagoFacade.planes());
    }

    @GetMapping("/suscripcion")
    @Operation(summary = "Suscripcion del negocio", description = "Cualquier usuario la ve (para el aviso de "
            + "prueba o vencida); los datos fiscales solo los recibe el administrador.")
    public ResponseEntity<SingleResponseVO<SuscripcionVO>> suscripcion() {
        return ResponseEntity.ok(pagoFacade.suscripcion());
    }

    @PostMapping(path = "/cotizaciones", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequierePerfil(Perfil.ADMINISTRADOR)
    @Operation(summary = "Cotizar", description = "Calcula subtotal, descuentos, IVA y total sin crear la orden.")
    @ApiResponse(responseCode = "400", description = "Cupon inexistente o periodo invalido")
    public ResponseEntity<SingleResponseVO<CotizacionVO>> cotizar(@Valid @RequestBody CotizacionRequestVO body) {
        return ResponseEntity.ok(pagoFacade.cotizar(new RequestVO<>(body)));
    }

    @PostMapping(path = "/ordenes", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequierePerfil(Perfil.ADMINISTRADOR)
    @Operation(summary = "Crear orden de pago", description = "Queda en PENDIENTE_PAGO. Tarjeta: 30 minutos para "
            + "pagar. Transferencia: 72 horas, con CLABE y referencia. Mandar un total o precio es un 400.")
    @ApiResponse(responseCode = "409", description = "Ya hay un cobro en proceso")
    public ResponseEntity<SingleResponseVO<OrdenPagoVO>> crearOrden(@Valid @RequestBody OrdenPagoRequestVO body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoFacade.crearOrden(new RequestVO<>(body)));
    }

    @GetMapping("/ordenes")
    @RequierePerfil({Perfil.ADMINISTRADOR, Perfil.COBRANZA})
    @Operation(summary = "Listar ordenes", description = "Historial de pagos; Cobranza filtra "
            + "metodo=TRANSFERENCIA&estado=PROCESANDO para conciliar.")
    public ResponseEntity<ResponseVO<OrdenPagoVO>> findList(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "{validacion.page.minimo}") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "{validacion.size.minimo}")
            @Max(value = CommonConstant.MAX_PAGE_SIZE, message = "{validacion.size.maximo}") int size,
            @RequestParam(defaultValue = "fechaOrden") String orderBy,
            @RequestParam(defaultValue = "DESC") String orderType,
            @Parameter(example = "PROCESANDO") @RequestParam(required = false) String estado,
            @Parameter(example = "TRANSFERENCIA") @RequestParam(required = false) String metodo) {
        return ResponseEntity.ok(pagoFacade.findList(
                Paginacion.de(new FindListOrdenRequestVO(estado, metodo), page, size, orderBy, orderType)));
    }

    @GetMapping("/ordenes/{id}")
    @RequierePerfil({Perfil.ADMINISTRADOR, Perfil.COBRANZA})
    @Operation(summary = "Detalle de una orden", description = "Lo consulta la pantalla de pago mientras espera "
            + "el webhook.")
    public ResponseEntity<SingleResponseVO<OrdenPagoVO>> findDetail(@PathVariable Long id) {
        return ResponseEntity.ok(pagoFacade.findDetail(new RequestVO<>(new IdRequestVO(id))));
    }

    @PostMapping(path = "/ordenes/{id}/cobros", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequierePerfil(Perfil.ADMINISTRADOR)
    @Operation(summary = "Cobrar con tarjeta", description = "Recibe solo el token de la pasarela (PCI-DSS). "
            + "Responde 202 con la orden en PROCESANDO; el resultado llega por webhook.")
    @ApiResponse(responseCode = "502", description = "La pasarela no respondio")
    public ResponseEntity<SingleResponseVO<OrdenPagoVO>> cobrar(@PathVariable Long id,
                                                               @Valid @RequestBody CobroTarjetaRequestVO body) {
        body.setId(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(pagoFacade.cobrarConTarjeta(new RequestVO<>(body)));
    }

    @PostMapping(path = "/ordenes/{id}/transferencias", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequierePerfil(Perfil.ADMINISTRADOR)
    @Operation(summary = "Reportar transferencia", description = "El cliente B2B da la clave de rastreo SPEI; la "
            + "orden pasa a PROCESANDO hasta que Cobranza la valide.")
    public ResponseEntity<SingleResponseVO<OrdenPagoVO>> reportarTransferencia(
            @PathVariable Long id, @Valid @RequestBody TransferenciaRequestVO body) {
        body.setId(id);
        return ResponseEntity.ok(pagoFacade.reportarTransferencia(new RequestVO<>(body)));
    }

    @PatchMapping(path = "/ordenes/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequierePerfil(Perfil.COBRANZA)
    @Operation(summary = "Validar pago", description = "Cobranza marca una transferencia como PAGADO (activa la "
            + "suscripcion y emite el CFDI) o RECHAZADO (con motivo). Las de tarjeta solo las resuelve el webhook.")
    @ApiResponse(responseCode = "409", description = "La orden es de tarjeta o no esta en PROCESANDO")
    public ResponseEntity<SingleResponseVO<OrdenPagoVO>> validar(@PathVariable Long id,
                                                                @Valid @RequestBody ValidacionPagoRequestVO body) {
        body.setId(id);
        return ResponseEntity.ok(pagoFacade.validarTransferencia(new RequestVO<>(body)));
    }

    @PostMapping(path = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Publico
    @Operation(summary = "Webhook de la pasarela", description = "Fuente de verdad del cobro con tarjeta. Exige "
            + "la cabecera Pasarela-Firma (HMAC-SHA256) y es idempotente por ID de evento.")
    @ApiResponse(responseCode = "400", description = "Firma ausente, alterada o vieja")
    public ResponseEntity<WebhookRespuestaVO> webhook(
            @RequestHeader(value = "Pasarela-Firma", required = false) String firma, @RequestBody String cuerpo) {
        return ResponseEntity.ok(pagoFacade.procesarWebhook(cuerpo, firma));
    }

    @GetMapping(path = "/facturas/{id}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    @RequierePerfil({Perfil.ADMINISTRADOR, Perfil.COBRANZA})
    @Operation(summary = "Descargar el XML del CFDI")
    public ResponseEntity<String> facturaXml(@PathVariable Long id) {
        FacturaXmlVO factura = pagoFacade.facturaXml(new RequestVO<>(new IdRequestVO(id)));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(factura.nombreArchivo(), StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType(MediaType.APPLICATION_XML, StandardCharsets.UTF_8))
                .body(factura.xml());
    }
}
