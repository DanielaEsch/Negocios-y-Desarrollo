package mx.unam.buzz.rutinas.dominio.pago;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.comun.TransicionInvalidaException;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Maquina de estados de la orden: PENDIENTE_PAGO -> PROCESANDO -> PAGADO | RECHAZADO,
 * con salida a VENCIDO. Cada salto prohibido tiene su prueba.
 */
class OrdenPagoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 15, 10, 0);
    private static final Desglose DESGLOSE =
            Desglose.calcular(new BigDecimal("1490"), Periodo.MENSUAL, 0, null, null);

    private static OrdenPago tarjeta() {
        return OrdenPago.crear(2L, Periodo.MENSUAL, MetodoPago.TARJETA, DESGLOSE, null, AHORA);
    }

    private static OrdenPago transferencia() {
        return OrdenPago.crear(2L, Periodo.MENSUAL, MetodoPago.TRANSFERENCIA, DESGLOSE, null, AHORA);
    }

    @Test
    void nuevaOrdenQuedaPendienteYVenceSegunElMetodo() {
        assertEquals(EstadoOrden.PENDIENTE_PAGO, tarjeta().getEstado());
        assertEquals(AHORA.plusMinutes(30), tarjeta().getFechaExpiracion());
        assertEquals(AHORA.plusHours(72), transferencia().getFechaExpiracion());
    }

    @Test
    void flujoConTarjetaConfirmadoPorWebhook() {
        OrdenPago orden = tarjeta();
        orden.iniciarCobroConTarjeta(AHORA.plusMinutes(1));
        orden.registrarCargo("ch_123", "VISA", "4242");
        orden.confirmarPorPasarela(new BigDecimal("1728.40"), AHORA.plusMinutes(2));

        assertEquals(EstadoOrden.PAGADO, orden.getEstado());
        assertEquals("ch_123", orden.getReferenciaPago());
        assertEquals("4242", orden.getUltimos4());
        assertNotNull(orden.getFechaPago());
    }

    @Test
    void prohibidoSaltarDePendienteAPagado() {
        OrdenPago orden = tarjeta();

        assertThrows(TransicionInvalidaException.class,
                () -> orden.confirmarPorPasarela(new BigDecimal("1728.40"), AHORA));
        assertEquals(EstadoOrden.PENDIENTE_PAGO, orden.getEstado());
    }

    @Test
    void elWebhookConOtroMontoNoConfirmaLaOrden() {
        OrdenPago orden = tarjeta();
        orden.iniciarCobroConTarjeta(AHORA);

        assertThrows(ReglaNegocioException.class, () -> orden.confirmarPorPasarela(new BigDecimal("5.00"), AHORA));
        assertEquals(EstadoOrden.PROCESANDO, orden.getEstado());
    }

    @Test
    void deLaTarjetaSoloSeGuardanLosUltimosCuatroDigitos() {
        OrdenPago orden = tarjeta();
        orden.iniciarCobroConTarjeta(AHORA);

        assertThrows(ReglaNegocioException.class,
                () -> orden.registrarCargo("ch_1", "VISA", "4242424242424242"));
    }

    @Test
    void rechazoEsFinalYPideMotivo() {
        OrdenPago orden = tarjeta();
        orden.iniciarCobroConTarjeta(AHORA);

        assertThrows(ReglaNegocioException.class, () -> orden.rechazar(" "));
        orden.rechazar("Tarjeta declinada");

        assertEquals(EstadoOrden.RECHAZADO, orden.getEstado());
        assertThrows(TransicionInvalidaException.class,
                () -> orden.confirmarPorPasarela(new BigDecimal("1728.40"), AHORA));
    }

    @Test
    void unaOrdenPagadaNoSeRechazaDespues() {
        OrdenPago orden = tarjeta();
        orden.iniciarCobroConTarjeta(AHORA);
        orden.confirmarPorPasarela(new BigDecimal("1728.40"), AHORA);

        assertThrows(TransicionInvalidaException.class, () -> orden.rechazar("Se arrepintio"));
    }

    @Test
    void noSeCobraUnaOrdenVencida() {
        OrdenPago orden = tarjeta();

        assertThrows(ReglaNegocioException.class, () -> orden.iniciarCobroConTarjeta(AHORA.plusMinutes(31)));
        orden.vencer(AHORA.plusMinutes(31));
        assertEquals(EstadoOrden.VENCIDO, orden.getEstado());
    }

    @Test
    void noSeVenceUnaOrdenATiempoNiUnaEnProceso() {
        OrdenPago orden = tarjeta();
        assertThrows(ReglaNegocioException.class, () -> orden.vencer(AHORA.plusMinutes(5)));

        orden.iniciarCobroConTarjeta(AHORA);
        assertThrows(TransicionInvalidaException.class, () -> orden.vencer(AHORA.plusHours(2)));
    }

    @Test
    void flujoB2bConTransferenciaValidadaPorCobranza() {
        OrdenPago orden = transferencia();
        orden.reportarTransferencia(" mbank1234567890 ", AHORA.plusHours(5));
        orden.validarTransferencia(9L, Perfil.COBRANZA, AHORA.plusHours(6));

        assertEquals(EstadoOrden.PAGADO, orden.getEstado());
        assertEquals("MBANK1234567890", orden.getReferenciaPago());
        assertEquals(9L, orden.getIdValidador());
    }

    @Test
    void elAdministradorDelClienteNoValidaSuPropiaTransferencia() {
        OrdenPago orden = transferencia();
        orden.reportarTransferencia("MBANK1", AHORA);

        assertThrows(ReglaNegocioException.class,
                () -> orden.validarTransferencia(1L, Perfil.ADMINISTRADOR, AHORA));
        assertEquals(EstadoOrden.PROCESANDO, orden.getEstado());
    }

    @Test
    void cadaMetodoTieneSuFuenteDeVerdad() {
        OrdenPago porTarjeta = tarjeta();
        porTarjeta.iniciarCobroConTarjeta(AHORA);
        assertThrows(ReglaNegocioException.class,
                () -> porTarjeta.validarTransferencia(9L, Perfil.COBRANZA, AHORA));

        OrdenPago porTransferencia = transferencia();
        assertThrows(ReglaNegocioException.class, () -> porTransferencia.iniciarCobroConTarjeta(AHORA));
        porTransferencia.reportarTransferencia("MBANK1", AHORA);
        assertThrows(ReglaNegocioException.class,
                () -> porTransferencia.confirmarPorPasarela(new BigDecimal("1728.40"), AHORA));
    }

    @Test
    void laFacturaSoloNaceDeUnaOrdenPagada() {
        Factura.DatosFiscales receptor = new Factura.DatosFiscales("CLE210514AB1", "CAFETERIA LA ESQUINA",
                "06700", "601", "G03");
        OrdenPago orden = tarjeta();
        orden.iniciarCobroConTarjeta(AHORA);

        assertThrows(ReglaNegocioException.class, () -> Factura.emitir(orden, "BUZ260917AB1", receptor, AHORA));

        orden.confirmarPorPasarela(new BigDecimal("1728.40"), AHORA);
        Factura factura = Factura.emitir(orden, "BUZ260917AB1", receptor, AHORA);
        assertEquals(36, factura.getFolioFiscal().length());
        assertEquals(new BigDecimal("1728.40"), factura.getDesglose().total());
        assertNull(orden.getMotivoRechazo());
    }

    @Test
    void datosFiscalesInvalidosNoSeFacturan() {
        assertThrows(ReglaNegocioException.class,
                () -> new Factura.DatosFiscales("NOESRFC", "X", "06700", "601", "G03").validar());
        assertThrows(ReglaNegocioException.class,
                () -> new Factura.DatosFiscales("CLE210514AB1", "X", "067", "601", "G03").validar());
    }
}
