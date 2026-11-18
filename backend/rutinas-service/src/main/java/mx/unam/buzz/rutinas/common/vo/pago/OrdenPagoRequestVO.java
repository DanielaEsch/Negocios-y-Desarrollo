package mx.unam.buzz.rutinas.common.vo.pago;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

/**
 * Alta de una orden de pago. Si el cliente manda un total o un precio, Jackson lo
 * rechaza con 400 (fail-on-unknown-properties): el precio lo dicta el servidor.
 */
@Getter
@Setter
@NoArgsConstructor
public class OrdenPagoRequestVO extends CotizacionRequestVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "TARJETA (B2C, pasarela) o TRANSFERENCIA (B2B, SPEI)", example = "TARJETA",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.pago.metodo}")
    private String metodo;

    @Valid
    @NotNull(message = "{validacion.pago.datosFiscales}")
    private DatosFiscalesVO datosFiscales;
}
