package mx.unam.buzz.pasarela;

/**
 * Como llega el aviso al comercio. En la app es un POST HTTP; en las pruebas se
 * sustituye por una implementacion que guarda los avisos en memoria.
 */
@FunctionalInterface
public interface EntregaWebhook {

    /**
     * @return true si el comercio respondio 2xx
     */
    boolean entregar(String url, String cuerpo, String firma);
}
