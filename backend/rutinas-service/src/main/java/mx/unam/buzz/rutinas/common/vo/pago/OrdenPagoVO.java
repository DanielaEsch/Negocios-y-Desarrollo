package mx.unam.buzz.rutinas.common.vo.pago;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Orden de pago con su desglose, su estado y, si aplica, la factura.
 */
public record OrdenPagoVO(
        @Schema(example = "1") Long id,
        @Schema(example = "TK-261115-7QX2") String folio,
        PlanVO plan,
        @Schema(example = "MENSUAL") String periodo,
        @Schema(example = "TARJETA") String metodo,
        @Schema(description = "PENDIENTE_PAGO, PROCESANDO, PAGADO, RECHAZADO o VENCIDO", example = "PAGADO")
        String estado,
        DesgloseVO desglose,
        @Schema(example = "BUZZ20") String cupon,
        LocalDateTime fechaOrden,
        @Schema(description = "Despues de esta fecha la orden vence si no se pago") LocalDateTime fechaExpiracion,
        LocalDateTime fechaPago,
        @Schema(description = "ID del cargo en la pasarela o clave de rastreo SPEI") String referenciaPago,
        @Schema(description = "Solo marca y ultimos 4: TaskIn nunca ve el numero") TarjetaVO tarjeta,
        String motivoRechazo,
        DatosFiscalesVO datosFiscales,
        @Schema(description = "Datos para depositar (solo TRANSFERENCIA)") DatosTransferenciaVO transferencia,
        @Schema(description = "CFDI emitido (solo PAGADO)") FacturaVO factura,
        @Schema(example = "Laura Mendez") String solicitante,
        @Schema(example = "Sofia Vargas") String validador) {

    public record TarjetaVO(@Schema(example = "VISA") String marca, @Schema(example = "4242") String ultimos4) {
    }

    public record DatosTransferenciaVO(String banco, String clabe, String beneficiario, String referencia) {
    }

    public record FacturaVO(Long id, String folioFiscal, String serieFolio, LocalDateTime fechaTimbrado,
                            String rfcEmisor, String razonSocialEmisor, String formaPago) {
    }
}
