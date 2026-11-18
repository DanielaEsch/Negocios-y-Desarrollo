package mx.unam.buzz.rutinas.common.vo.pago;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.unam.buzz.rutinas.common.constant.PagoConstant;

import java.io.Serial;
import java.io.Serializable;

/**
 * Que se quiere comprar. No lleva precios: el servidor los calcula.
 */
@Getter
@Setter
@NoArgsConstructor
public class CotizacionRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Clave del plan", example = "NEGOCIO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.pago.plan}")
    private String plan;

    @Schema(description = "MENSUAL o ANUAL (2 meses de regalo)", example = "MENSUAL",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.pago.periodo}")
    private String periodo;

    @Schema(example = "BUZZ20")
    @Size(max = PagoConstant.MAX_SIZE_CUPON, message = "{validacion.pago.cupon}")
    private String cupon;
}
