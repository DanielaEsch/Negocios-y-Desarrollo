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
 * Cobro con tarjeta: solo el token que genero la pasarela en el navegador.
 */
@Getter
@Setter
@NoArgsConstructor
public class CobroTarjetaRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(hidden = true)
    private Long id;

    @Schema(description = "Token de un solo uso de la pasarela", example = "tok_3f9a1c2b7d8e4f60a1b2c3d4",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.pago.token}")
    @Pattern(regexp = "^tok_[A-Za-z0-9]{8,40}$", message = "{validacion.pago.token}")
    private String token;
}
