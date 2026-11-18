package mx.unam.buzz.rutinas.integration.pasarela;

import java.math.BigDecimal;

/**
 * Puerto hacia la pasarela de pago. TaskIn solo conoce esta interfaz; cambiar de
 * proveedor (o pasar del sandbox a uno real) es escribir otra implementacion.
 */
public interface IPasarelaPagos {

    /**
     * Abre un cargo con el token que genero el navegador.
     *
     * @param referencia folio de la orden; regresa en el webhook para encontrarla
     * @throws PasarelaException si la pasarela no abrio el cargo
     */
    CargoPasarela crearCargo(String token, BigDecimal monto, String moneda, String referencia, String descripcion);
}
