package mx.unam.buzz.pasarela;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion del simulador. Las llaves imitan las de un proveedor real: la publica
 * (pk_test_) la usa el navegador para tokenizar; la secreta (sk_test_) solo el backend
 * del comercio para cobrar; el secreto del webhook (whsec_) firma cada aviso.
 *
 * @param habilitada        si el simulador se monta (en produccion va apagado)
 * @param llavePublica      llave que acepta POST /tokens
 * @param llaveSecreta      llave que acepta POST /cargos
 * @param secretoWebhook    secreto compartido para firmar los webhooks
 * @param urlWebhook        a donde se avisa el resultado de cada cargo
 * @param retrasoMs         cuanto tarda en "procesar" un cargo antes de avisar
 * @param reintentos        reintentos si el comercio no responde 2xx
 * @param duplicarWebhook   reenvia cada aviso una segunda vez, como un reintento de red
 * @param entregarWebhooks  apagado en pruebas que no levantan servidor
 */
@ConfigurationProperties(prefix = "pasarela.sandbox")
public record PasarelaSandboxProperties(
        boolean habilitada,
        String llavePublica,
        String llaveSecreta,
        String secretoWebhook,
        String urlWebhook,
        long retrasoMs,
        int reintentos,
        boolean duplicarWebhook,
        Boolean entregarWebhooks) {

    public boolean debeEntregar() {
        return entregarWebhooks == null || entregarWebhooks;
    }
}
