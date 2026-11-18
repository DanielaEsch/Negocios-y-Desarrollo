package mx.unam.buzz.rutinas.dominio.pago;

import java.time.Duration;

/**
 * Como paga el negocio. Cada metodo tiene su propia fuente de verdad:
 * <ul>
 *   <li>TARJETA (modelo B2C): la pasarela cobra y avisa por webhook firmado.</li>
 *   <li>TRANSFERENCIA (modelo B2B): el cliente transfiere con la orden de compra y
 *       Cobranza valida el deposito a mano.</li>
 * </ul>
 */
public enum MetodoPago {

    TARJETA(Duration.ofMinutes(30)),
    TRANSFERENCIA(Duration.ofHours(72));

    /** Tiempo para pagar antes de que la orden venza. */
    private final Duration vigencia;

    MetodoPago(Duration vigencia) {
        this.vigencia = vigencia;
    }

    public Duration getVigencia() {
        return vigencia;
    }
}
