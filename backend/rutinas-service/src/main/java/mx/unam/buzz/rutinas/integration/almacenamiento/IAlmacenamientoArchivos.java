package mx.unam.buzz.rutinas.integration.almacenamiento;

/**
 * Donde viven los archivos (fotos de evidencia). La base solo guarda la llave; cambiar
 * de disco local a un almacenamiento de objetos es otra implementacion de esta interfaz.
 */
public interface IAlmacenamientoArchivos {

    /** Guarda el contenido y devuelve la llave con la que se recupera. */
    String guardar(byte[] contenido, String extension);

    byte[] leer(String llave);
}
