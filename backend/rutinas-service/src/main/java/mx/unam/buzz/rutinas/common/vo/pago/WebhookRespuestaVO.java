package mx.unam.buzz.rutinas.common.vo.pago;

/**
 * Acuse para la pasarela. Cualquier 2xx detiene sus reintentos.
 */
public record WebhookRespuestaVO(boolean recibido, String resultado) {
}
