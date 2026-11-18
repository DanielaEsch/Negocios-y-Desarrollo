package mx.unam.buzz.pasarela;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Cuerpos de entrada y salida del simulador, con nombres en espanol como el resto del proyecto.
 */
public final class PasarelaSandboxDtos {

    private PasarelaSandboxDtos() {
    }

    /** Lo que manda el navegador. El numero solo vive en esta llamada. */
    public record SolicitudToken(String numero, Integer mesExpiracion, Integer anioExpiracion, String cvc,
                                 String titular) {
        @Override
        public String toString() {
            return "SolicitudToken[datos de tarjeta ocultos]";
        }
    }

    public record TarjetaResumen(String marca, String ultimos4, Integer mesExpiracion, Integer anioExpiracion) {
    }

    public record TokenRespuesta(String id, String objeto, TarjetaResumen tarjeta, Instant expira) {
    }

    /** Lo que manda el backend del comercio con su llave secreta. */
    public record SolicitudCargo(String token, BigDecimal monto, String moneda, String referencia,
                                 String descripcion) {
    }

    public record CargoRespuesta(String id, String objeto, String estado, BigDecimal monto, String moneda,
                                 String referencia, TarjetaResumen tarjeta, String motivo, Instant creado) {
    }
}
