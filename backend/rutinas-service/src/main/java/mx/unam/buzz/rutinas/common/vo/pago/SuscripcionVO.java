package mx.unam.buzz.rutinas.common.vo.pago;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Estado de la suscripcion del negocio.
 */
public record SuscripcionVO(
        PlanVO plan,
        @Schema(description = "PRUEBA, ACTIVA o VENCIDA", example = "PRUEBA") String estado,
        LocalDate fechaInicio,
        LocalDate vigenciaFin,
        @Schema(example = "14") long diasRestantes,
        @Schema(description = "Solo para el administrador") DatosFiscalesVO datosFiscales) {
}
