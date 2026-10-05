package mx.unam.buzz.rutinas.config;

import mx.unam.buzz.rutinas.dominio.usuario.CifradorContrasena;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Implementacion del puerto de hash del dominio con BCrypt (patron Adapter).
 * El hash incluye su propia sal; no se guarda la contrasena en texto plano.
 */
@Component
public class BcryptCifradorContrasena implements CifradorContrasena {

    private static final int COSTO = 12;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(COSTO);

    @Override
    public String cifrar(String contrasenaPlana) {
        return encoder.encode(contrasenaPlana);
    }

    @Override
    public boolean coincide(String contrasenaPlana, String hash) {
        return encoder.matches(contrasenaPlana, hash);
    }
}
