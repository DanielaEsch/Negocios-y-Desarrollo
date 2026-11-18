package mx.unam.buzz.rutinas.integration.pasarela;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.HexFormat;

/**
 * Comprueba que un webhook viene de la pasarela: HMAC-SHA256 de "t.cuerpo" con el
 * secreto compartido. Rechaza firmas viejas (replay) y compara en tiempo constante.
 */
@Component
public class VerificadorFirmaWebhook {

    private final String secreto;
    private final long toleranciaSegundos;
    private final Clock reloj;

    @Autowired
    public VerificadorFirmaWebhook(PagosProperties propiedades) {
        this(propiedades.pasarela().secretoWebhook(), propiedades.pasarela().toleranciaSegundos(), Clock.systemUTC());
    }

    VerificadorFirmaWebhook(String secreto, long toleranciaSegundos, Clock reloj) {
        this.secreto = secreto;
        this.toleranciaSegundos = toleranciaSegundos;
        this.reloj = reloj;
    }

    /** Cabecera con la forma {@code t=1731700000,v1=<hex>}. */
    public boolean esValida(String cuerpo, String cabecera) {
        if (cuerpo == null || cabecera == null) {
            return false;
        }
        Long timestamp = null;
        String firma = null;
        for (String parte : cabecera.split(",")) {
            String[] llaveValor = parte.trim().split("=", 2);
            if (llaveValor.length != 2) {
                continue;
            }
            if ("t".equals(llaveValor[0])) {
                try {
                    timestamp = Long.parseLong(llaveValor[1]);
                } catch (NumberFormatException ex) {
                    return false;
                }
            } else if ("v1".equals(llaveValor[0])) {
                firma = llaveValor[1];
            }
        }
        if (timestamp == null || firma == null
                || Math.abs(reloj.instant().getEpochSecond() - timestamp) > toleranciaSegundos) {
            return false;
        }
        byte[] esperada = hmac(timestamp + "." + cuerpo).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(esperada, firma.getBytes(StandardCharsets.UTF_8));
    }

    String hmac(String mensaje) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(mensaje.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
