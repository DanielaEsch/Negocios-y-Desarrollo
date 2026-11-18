package mx.unam.buzz.rutinas.common.constant;

import java.util.Map;

/**
 * Constantes del modulo de pagos.
 */
public final class PagoConstant {

    /** Cupones vigentes y su porcentaje. Viven en el servidor: el cliente solo manda la clave. */
    public static final Map<String, Integer> CUPONES = Map.of("BUZZ20", 20);

    public static final int MAX_SIZE_CUPON = 20;
    public static final int MAX_SIZE_MOTIVO = 300;
    public static final String SERIE_FACTURA = "TK";

    /** Forma de pago del SAT: 04 tarjeta de credito, 03 transferencia electronica. */
    public static final String FORMA_PAGO_TARJETA = "04";
    public static final String FORMA_PAGO_TRANSFERENCIA = "03";

    /** Resultado de procesar un webhook, para la bitacora y la respuesta a la pasarela. */
    public static final String WEBHOOK_PROCESADO = "PROCESADO";
    public static final String WEBHOOK_DUPLICADO = "DUPLICADO";
    public static final String WEBHOOK_IGNORADO = "IGNORADO";

    private PagoConstant() {
    }
}
