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
 * Cobranza valida o rechaza una transferencia (el boton "Validar pago" del B2B).
 */
@Getter
@Setter
@NoArgsConstructor
public class ValidacionPagoRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(hidden = true)
    private Long id;

    @Schema(description = "PAGADO o RECHAZADO", example = "PAGADO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.estado.obligatorio}")
    private String estado;

    @Schema(description = "Obligatorio para RECHAZADO", example = "No llego el deposito")
    @Size(max = PagoConstant.MAX_SIZE_MOTIVO, message = "{validacion.pago.motivo}")
    private String motivo;
}
