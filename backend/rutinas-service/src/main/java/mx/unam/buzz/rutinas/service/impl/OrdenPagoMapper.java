package mx.unam.buzz.rutinas.service.impl;

import mx.unam.buzz.rutinas.dominio.pago.Desglose;
import mx.unam.buzz.rutinas.dominio.pago.EstadoOrden;
import mx.unam.buzz.rutinas.dominio.pago.Factura;
import mx.unam.buzz.rutinas.dominio.pago.MetodoPago;
import mx.unam.buzz.rutinas.dominio.pago.OrdenPago;
import mx.unam.buzz.rutinas.dominio.pago.Periodo;
import mx.unam.buzz.rutinas.modelo.entity.OrdenPagoDO;

/**
 * Traduce entre la fila ORDEN_PAGO y el agregado OrdenPago del dominio. El desglose
 * no se lee de la fila: se recalcula con las mismas entradas, asi la regla vive en un
 * solo lugar.
 */
final class OrdenPagoMapper {

    private OrdenPagoMapper() {
    }

    static Desglose desglose(OrdenPagoDO entity) {
        return Desglose.calcular(entity.getPrecioMensual(), Periodo.valueOf(entity.getPeriodo()),
                entity.getPorcentajeCupon(), entity.getCupon(), entity.getCredito());
    }

    static OrdenPago aDominio(OrdenPagoDO entity) {
        return OrdenPago.reconstituir(entity.getIdPlan(), Periodo.valueOf(entity.getPeriodo()),
                MetodoPago.valueOf(entity.getMetodo()), desglose(entity), entity.getCupon(), entity.getFechaOrden(),
                entity.getFechaExpiracion(), EstadoOrden.valueOf(entity.getEstado()), entity.getReferenciaPago(),
                entity.getMarcaTarjeta(), entity.getUltimos4(), entity.getMotivoRechazo(), entity.getIdValida(),
                entity.getFechaPago());
    }

    /** Copia a la fila lo que el dominio puede cambiar. */
    static void aplicar(OrdenPago orden, OrdenPagoDO entity) {
        entity.setEstado(orden.getEstado().name());
        entity.setReferenciaPago(orden.getReferenciaPago());
        entity.setMarcaTarjeta(orden.getMarcaTarjeta());
        entity.setUltimos4(orden.getUltimos4());
        entity.setMotivoRechazo(orden.getMotivoRechazo());
        entity.setIdValida(orden.getIdValidador());
        entity.setFechaPago(orden.getFechaPago());
    }

    static Factura.DatosFiscales datosFiscales(OrdenPagoDO entity) {
        return new Factura.DatosFiscales(entity.getRfc(), entity.getRazonSocial(), entity.getCodigoPostal(),
                entity.getRegimenFiscal(), entity.getUsoCfdi());
    }
}
