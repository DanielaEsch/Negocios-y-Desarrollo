package mx.unam.buzz.rutinas.common.vo.pago;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Datos del receptor del CFDI 4.0. Se validan aqui (formato) y en el dominio (regla).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatosFiscalesVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(example = "CLE210514AB1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.pago.rfc}")
    @Pattern(regexp = "^[A-Z&Ñ]{3,4}\\d{6}[A-Z0-9]{3}$", message = "{validacion.pago.rfc}")
    private String rfc;

    @Schema(example = "CAFETERIA LA ESQUINA SA DE CV", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.pago.razonSocial}")
    @Size(max = 300, message = "{validacion.pago.razonSocial}")
    private String razonSocial;

    @Schema(example = "04510", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.pago.codigoPostal}")
    @Pattern(regexp = "^\\d{5}$", message = "{validacion.pago.codigoPostal}")
    private String codigoPostal;

    @Schema(description = "Clave del SAT: 601 General de Ley PM, 612 PF con actividad empresarial, 626 RESICO",
            example = "601", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.pago.regimen}")
    @Pattern(regexp = "^\\d{3}$", message = "{validacion.pago.regimen}")
    private String regimenFiscal;

    @Schema(description = "Uso del CFDI: G03 gastos en general", example = "G03",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{validacion.pago.usoCfdi}")
    @Pattern(regexp = "^[A-Z]\\d{2}$", message = "{validacion.pago.usoCfdi}")
    private String usoCfdi;
}
