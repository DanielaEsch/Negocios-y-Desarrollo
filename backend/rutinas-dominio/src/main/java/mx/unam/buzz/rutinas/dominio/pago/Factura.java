package mx.unam.buzz.rutinas.dominio.pago;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * El "acta de nacimiento" de un ingreso: el CFDI. Solo nace de una orden PAGADA y,
 * una vez emitido, es inmutable (no tiene setters). El timbre es simulado: en
 * produccion lo pone un PAC autorizado por el SAT.
 */
public final class Factura {

    /** RFC de persona moral (12) o fisica (13). */
    private static final Pattern RFC = Pattern.compile("^[A-Z&Ñ]{3,4}\\d{6}[A-Z0-9]{3}$");
    private static final Pattern CODIGO_POSTAL = Pattern.compile("^\\d{5}$");

    private final String folioFiscal;
    private final String rfcEmisor;
    private final String rfcReceptor;
    private final String razonSocialReceptor;
    private final String codigoPostalReceptor;
    private final String regimenFiscalReceptor;
    private final String usoCfdi;
    private final Desglose desglose;
    private final LocalDateTime fechaTimbrado;

    private Factura(String folioFiscal, String rfcEmisor, String rfcReceptor, String razonSocialReceptor,
                    String codigoPostalReceptor, String regimenFiscalReceptor, String usoCfdi, Desglose desglose,
                    LocalDateTime fechaTimbrado) {
        this.folioFiscal = folioFiscal;
        this.rfcEmisor = rfcEmisor;
        this.rfcReceptor = rfcReceptor;
        this.razonSocialReceptor = razonSocialReceptor;
        this.codigoPostalReceptor = codigoPostalReceptor;
        this.regimenFiscalReceptor = regimenFiscalReceptor;
        this.usoCfdi = usoCfdi;
        this.desglose = desglose;
        this.fechaTimbrado = fechaTimbrado;
    }

    public static Factura emitir(OrdenPago orden, String rfcEmisor, DatosFiscales receptor, LocalDateTime ahora) {
        if (orden == null || orden.getEstado() != EstadoOrden.PAGADO) {
            throw new ReglaNegocioException("La factura solo se emite cuando la orden esta PAGADA");
        }
        validar(esRfcValido(rfcEmisor), "El RFC del emisor no es valido");
        validar(receptor != null, "Faltan los datos fiscales del receptor");
        receptor.validar();
        return new Factura(UUID.randomUUID().toString().toUpperCase(), rfcEmisor, receptor.rfc(),
                receptor.razonSocial(), receptor.codigoPostal(), receptor.regimenFiscal(), receptor.usoCfdi(),
                orden.getDesglose(), ahora);
    }

    public static boolean esRfcValido(String rfc) {
        return rfc != null && RFC.matcher(rfc).matches();
    }

    /** Datos del cliente para el CFDI 4.0: RFC, razon social, CP, regimen y uso. */
    public record DatosFiscales(String rfc, String razonSocial, String codigoPostal, String regimenFiscal,
                                String usoCfdi) {

        public void validar() {
            Factura.validar(esRfcValido(rfc), "El RFC no tiene un formato valido");
            Factura.validar(razonSocial != null && !razonSocial.isBlank(), "La razon social es obligatoria");
            Factura.validar(codigoPostal != null && CODIGO_POSTAL.matcher(codigoPostal).matches(),
                    "El codigo postal debe tener 5 digitos");
            Factura.validar(regimenFiscal != null && regimenFiscal.matches("\\d{3}"),
                    "El regimen fiscal es la clave de 3 digitos del SAT");
            Factura.validar(usoCfdi != null && usoCfdi.matches("[A-Z]\\d{2}"),
                    "El uso del CFDI es la clave del SAT (por ejemplo G03)");
        }
    }

    public String getFolioFiscal() {
        return folioFiscal;
    }

    public String getRfcEmisor() {
        return rfcEmisor;
    }

    public String getRfcReceptor() {
        return rfcReceptor;
    }

    public String getRazonSocialReceptor() {
        return razonSocialReceptor;
    }

    public String getCodigoPostalReceptor() {
        return codigoPostalReceptor;
    }

    public String getRegimenFiscalReceptor() {
        return regimenFiscalReceptor;
    }

    public String getUsoCfdi() {
        return usoCfdi;
    }

    public Desglose getDesglose() {
        return desglose;
    }

    public LocalDateTime getFechaTimbrado() {
        return fechaTimbrado;
    }

    private static void validar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ReglaNegocioException(mensaje);
        }
    }
}
