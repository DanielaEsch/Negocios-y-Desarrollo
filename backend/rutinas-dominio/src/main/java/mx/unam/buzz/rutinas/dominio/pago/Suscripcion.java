package mx.unam.buzz.rutinas.dominio.pago;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * La suscripcion del negocio a TaskIn (SaaS). Reglas de facturacion recurrente:
 * <ul>
 *   <li>Renovar el mismo plan suma los meses al final de la vigencia actual.</li>
 *   <li>Cambiar de plan arranca hoy, y los dias pagados que no se usaron del plan
 *       anterior se devuelven como credito (prorrateo).</li>
 *   <li>La prueba gratis no genera credito: no se pago nada.</li>
 * </ul>
 */
public class Suscripcion {

    /** Mes comercial para prorratear. */
    public static final int DIAS_POR_MES = 30;

    private Long idPlan;
    private boolean pagada;
    private LocalDate fechaInicio;
    private LocalDate vigenciaFin;

    private Suscripcion() {
    }

    public static Suscripcion prueba(Long idPlan, LocalDate hoy, int dias) {
        validar(idPlan != null && idPlan > 0, "El plan no es valido");
        validar(hoy != null && dias > 0, "La prueba necesita una fecha y una duracion");
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.idPlan = idPlan;
        suscripcion.fechaInicio = hoy;
        suscripcion.vigenciaFin = hoy.plusDays(dias);
        return suscripcion;
    }

    public static Suscripcion reconstituir(Long idPlan, EstadoSuscripcion estadoGuardado, LocalDate fechaInicio,
                                           LocalDate vigenciaFin) {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.idPlan = idPlan;
        suscripcion.pagada = estadoGuardado == EstadoSuscripcion.ACTIVA;
        suscripcion.fechaInicio = fechaInicio;
        suscripcion.vigenciaFin = vigenciaFin;
        return suscripcion;
    }

    public EstadoSuscripcion estadoAl(LocalDate hoy) {
        if (hoy.isAfter(vigenciaFin)) {
            return EstadoSuscripcion.VENCIDA;
        }
        return pagada ? EstadoSuscripcion.ACTIVA : EstadoSuscripcion.PRUEBA;
    }

    /** Dias que quedan de vigencia (0 si ya vencio). */
    public long diasRestantes(LocalDate hoy) {
        return Math.max(0, ChronoUnit.DAYS.between(hoy, vigenciaFin));
    }

    /**
     * Credito al cambiar a otro plan estando pagado y vigente: el precio mensual del plan
     * actual por los dias que no se usaron, entre 30.
     */
    public BigDecimal creditoPorCambio(Long idPlanNuevo, BigDecimal precioMensualActual, LocalDate hoy) {
        if (!pagada || idPlanNuevo.equals(idPlan) || estadoAl(hoy) == EstadoSuscripcion.VENCIDA) {
            return BigDecimal.ZERO;
        }
        return precioMensualActual.multiply(BigDecimal.valueOf(diasRestantes(hoy)))
                .divide(BigDecimal.valueOf(DIAS_POR_MES), 2, RoundingMode.HALF_UP);
    }

    /** Se pago una orden: el plan queda activo por los meses comprados. */
    public void activar(Long idPlanPagado, int meses, LocalDate hoy) {
        validar(idPlanPagado != null && idPlanPagado > 0, "El plan no es valido");
        validar(meses > 0, "Hay que activar al menos un mes");
        boolean renovacion = pagada && idPlanPagado.equals(idPlan) && estadoAl(hoy) != EstadoSuscripcion.VENCIDA;
        if (renovacion) {
            vigenciaFin = vigenciaFin.plusMonths(meses);
        } else {
            fechaInicio = hoy;
            vigenciaFin = hoy.plusMonths(meses);
        }
        idPlan = idPlanPagado;
        pagada = true;
    }

    /** Lo que se guarda en la base: PRUEBA o ACTIVA (VENCIDA se deduce). */
    public EstadoSuscripcion getEstadoGuardado() {
        return pagada ? EstadoSuscripcion.ACTIVA : EstadoSuscripcion.PRUEBA;
    }

    public Long getIdPlan() {
        return idPlan;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getVigenciaFin() {
        return vigenciaFin;
    }

    private static void validar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ReglaNegocioException(mensaje);
        }
    }
}
