package mx.unam.buzz.pasarela;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

/**
 * Firma de los avisos: HMAC-SHA256 de "timestamp.cuerpo" con el secreto del webhook.
 * La cabecera queda como {@code Pasarela-Firma: t=1731700000,v1=<hex>}. El timestamp
 * dentro de la firma impide reenviar un aviso viejo (replay).
 */
public final class FirmaWebhook {

    public static final String CABECERA = "Pasarela-Firma";

    private FirmaWebhook() {
    }

    public static String cabecera(String secreto, long timestamp, String cuerpo) {
        return "t=" + timestamp + ",v1=" + hmac(secreto, timestamp + "." + cuerpo);
    }

    public static String hmac(String secreto, String mensaje) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(mensaje.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo firmar el webhook", ex);
        }
    }
}
