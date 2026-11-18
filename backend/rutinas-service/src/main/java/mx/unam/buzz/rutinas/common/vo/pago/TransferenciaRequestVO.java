package mx.unam.buzz.rutinas.common.vo.pago;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * El cliente avisa que ya transfirio y da la clave de rastreo SPEI.
 */
@Getter
@Setter
@NoArgsConstructor
public class TransferenciaRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(hidden = true)
    private Long id;

    @Schema(example = "BNET01002611150012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.pago.claveRastreo}")
    @Pattern(regexp = "^[A-Za-z0-9]{6,30}$", message = "{validacion.pago.claveRastreo}")
    private String claveRastreo;
}
