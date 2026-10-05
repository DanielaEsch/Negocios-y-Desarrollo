package mx.unam.buzz.rutinas.dominio.usuario;

/**
 * Puerto para el hash de contrasenas. El dominio solo conoce esta interfaz;
 * la implementacion (BCrypt) vive en rutinas-service.
 */
public interface CifradorContrasena {

    /** Devuelve el hash de la contrasena en texto plano. */
    String cifrar(String contrasenaPlana);

    /** Indica si la contrasena en texto plano corresponde al hash. */
    boolean coincide(String contrasenaPlana, String hash);
}
