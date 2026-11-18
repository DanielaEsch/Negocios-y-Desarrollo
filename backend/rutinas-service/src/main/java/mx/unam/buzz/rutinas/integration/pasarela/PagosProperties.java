package mx.unam.buzz.rutinas.integration.pasarela;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion de cobro de TaskIn ({@code taskin.pagos}).
 *
 * @param pasarela      como hablar con la pasarela (URL, llave secreta, secreto del webhook)
 * @param emisor        datos fiscales de TaskIn para el CFDI
 * @param transferencia cuenta a la que se deposita en el pago B2B
 * @param diasPrueba    dias de prueba gratis al instalar
 */
@ConfigurationProperties(prefix = "taskin.pagos")
public record PagosProperties(Pasarela pasarela, Emisor emisor, Transferencia transferencia, int diasPrueba) {

    public record Pasarela(String url, String llaveSecreta, String secretoWebhook, long toleranciaSegundos) {
    }

    public record Emisor(String rfc, String razonSocial, String regimenFiscal, String codigoPostal) {
    }

    public record Transferencia(String banco, String clabe, String beneficiario) {
    }
}
