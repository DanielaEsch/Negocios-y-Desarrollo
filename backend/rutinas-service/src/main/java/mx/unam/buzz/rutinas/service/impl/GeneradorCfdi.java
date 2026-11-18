package mx.unam.buzz.rutinas.service.impl;

import mx.unam.buzz.rutinas.dominio.pago.Desglose;
import mx.unam.buzz.rutinas.dominio.pago.Factura;
import mx.unam.buzz.rutinas.integration.pasarela.PagosProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Arma un CFDI 4.0 de ingreso con la estructura del SAT. Es un simulacro: el sello y
 * el timbre son de sandbox, no hay PAC ni validez fiscal.
 */
@Component
class GeneradorCfdi {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    /** Servicios de software en la nube (catalogo c_ClaveProdServ). */
    private static final String CLAVE_PROD_SERV = "81112106";
    /** RFC generico de pruebas del SAT, en lugar del de un PAC real. */
    private static final String RFC_PAC_SANDBOX = "AAA010101AAA";

    String generar(Factura factura, PagosProperties.Emisor emisor, String serie, String folio, String formaPago,
                   String concepto) {
        Desglose d = factura.getDesglose();
        StringBuilder xml = new StringBuilder(2048);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<!-- TaskIn sandbox: CFDI de demostracion, sin validez fiscal -->\n")
                .append("<cfdi:Comprobante xmlns:cfdi=\"http://www.sat.gob.mx/cfd/4\"")
                .append(" xmlns:tfd=\"http://www.sat.gob.mx/TimbreFiscalDigital\"")
                .append(attr("Version", "4.0")).append(attr("Serie", serie)).append(attr("Folio", folio))
                .append(attr("Fecha", factura.getFechaTimbrado().format(FECHA)))
                .append(attr("FormaPago", formaPago)).append(attr("MetodoPago", "PUE"))
                .append(attr("SubTotal", monto(d.subtotal())));
        if (d.descuento().signum() > 0) {
            xml.append(attr("Descuento", monto(d.descuento())));
        }
        xml.append(attr("Moneda", Desglose.MONEDA)).append(attr("Total", monto(d.total())))
                .append(attr("TipoDeComprobante", "I")).append(attr("Exportacion", "01"))
                .append(attr("LugarExpedicion", emisor.codigoPostal())).append(">\n")
                .append("  <cfdi:Emisor").append(attr("Rfc", factura.getRfcEmisor()))
                .append(attr("Nombre", emisor.razonSocial())).append(attr("RegimenFiscal", emisor.regimenFiscal()))
                .append("/>\n")
                .append("  <cfdi:Receptor").append(attr("Rfc", factura.getRfcReceptor()))
                .append(attr("Nombre", factura.getRazonSocialReceptor()))
                .append(attr("DomicilioFiscalReceptor", factura.getCodigoPostalReceptor()))
                .append(attr("RegimenFiscalReceptor", factura.getRegimenFiscalReceptor()))
                .append(attr("UsoCFDI", factura.getUsoCfdi())).append("/>\n")
                .append("  <cfdi:Conceptos>\n")
                .append("    <cfdi:Concepto").append(attr("ClaveProdServ", CLAVE_PROD_SERV))
                .append(attr("Cantidad", "1")).append(attr("ClaveUnidad", "E48")).append(attr("Unidad", "Servicio"))
                .append(attr("Descripcion", concepto)).append(attr("ValorUnitario", monto(d.subtotal())))
                .append(attr("Importe", monto(d.subtotal())));
        if (d.descuento().signum() > 0) {
            xml.append(attr("Descuento", monto(d.descuento())));
        }
        xml.append(attr("ObjetoImp", "02")).append(">\n")
                .append("      <cfdi:Impuestos><cfdi:Traslados>").append(traslado(d)).append("</cfdi:Traslados>")
                .append("</cfdi:Impuestos>\n")
                .append("    </cfdi:Concepto>\n")
                .append("  </cfdi:Conceptos>\n")
                .append("  <cfdi:Impuestos").append(attr("TotalImpuestosTrasladados", monto(d.iva()))).append(">")
                .append("<cfdi:Traslados>").append(traslado(d)).append("</cfdi:Traslados></cfdi:Impuestos>\n")
                .append("  <cfdi:Complemento>\n")
                .append("    <tfd:TimbreFiscalDigital").append(attr("Version", "1.1"))
                .append(attr("UUID", factura.getFolioFiscal()))
                .append(attr("FechaTimbrado", factura.getFechaTimbrado().format(FECHA)))
                .append(attr("RfcProvCertif", RFC_PAC_SANDBOX)).append(attr("SelloCFD", "SANDBOX"))
                .append(attr("NoCertificadoSAT", "00000000000000000000")).append(attr("SelloSAT", "SANDBOX"))
                .append("/>\n")
                .append("  </cfdi:Complemento>\n")
                .append("</cfdi:Comprobante>\n");
        return xml.toString();
    }

    private static String traslado(Desglose d) {
        return "<cfdi:Traslado" + attr("Base", monto(d.base())) + attr("Impuesto", "002")
                + attr("TipoFactor", "Tasa") + attr("TasaOCuota", "0.160000") + attr("Importe", monto(d.iva()))
                + "/>";
    }

    private static String monto(BigDecimal valor) {
        return Desglose.centavos(valor).toPlainString();
    }

    private static String attr(String nombre, String valor) {
        return " " + nombre + "=\"" + escapar(valor) + "\"";
    }

    static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
