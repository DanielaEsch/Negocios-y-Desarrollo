package mx.unam.buzz.pasarela;

import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;

/**
 * Tarjetas de prueba del sandbox, como las que publican los proveedores reales. El
 * numero decide el resultado del cargo; cualquier otro numero valido (Luhn) se aprueba.
 */
public final class TarjetasDePrueba {

    /** Resultado programado para una tarjeta de prueba. */
    public record Rechazo(String codigo, String mensaje) {
    }

    private static final Map<String, Rechazo> RECHAZOS = Map.of(
            "4000000000000002", new Rechazo("tarjeta_declinada", "El banco emisor declino la tarjeta"),
            "4000000000009995", new Rechazo("fondos_insuficientes", "La tarjeta no tiene fondos suficientes"),
            "4000000000000127", new Rechazo("cvc_incorrecto", "El codigo de seguridad no coincide"));

    private TarjetasDePrueba() {
    }

    /** Rechazo programado para la tarjeta, o vacio si se aprueba. */
    public static Optional<Rechazo> rechazoPara(String numero) {
        return Optional.ofNullable(RECHAZOS.get(numero));
    }

    /** Algoritmo de Luhn: el ultimo digito verifica a los demas. */
    public static boolean pasaLuhn(String numero) {
        if (numero == null || !numero.matches("\\d{13,19}")) {
            return false;
        }
        int suma = 0;
        boolean doble = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = numero.charAt(i) - '0';
            if (doble) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
            doble = !doble;
        }
        return suma % 10 == 0;
    }

    public static String marca(String numero) {
        if (numero.startsWith("4")) {
            return "VISA";
        }
        if (numero.startsWith("34") || numero.startsWith("37")) {
            return "AMEX";
        }
        int prefijo = Integer.parseInt(numero.substring(0, 4));
        if ((prefijo >= 5100 && prefijo <= 5599) || (prefijo >= 2221 && prefijo <= 2720)) {
            return "MASTERCARD";
        }
        return "OTRA";
    }

    /** Acepta 2 o 4 digitos de anio. Vence al terminar el mes indicado. */
    public static boolean vigente(int mes, int anio, YearMonth hoy) {
        if (mes < 1 || mes > 12) {
            return false;
        }
        int completo = anio < 100 ? 2000 + anio : anio;
        return !YearMonth.of(completo, mes).isBefore(hoy);
    }
}
