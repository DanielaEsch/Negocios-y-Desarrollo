package mx.unam.buzz.rutinas.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.unam.buzz.rutinas.common.constant.PagoConstant;
import mx.unam.buzz.rutinas.common.constant.PagoErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import mx.unam.buzz.rutinas.dominio.pago.EstadoSuscripcion;
import mx.unam.buzz.rutinas.dominio.pago.Factura;
import mx.unam.buzz.rutinas.dominio.pago.MetodoPago;
import mx.unam.buzz.rutinas.dominio.pago.OrdenPago;
import mx.unam.buzz.rutinas.dominio.pago.Suscripcion;
import mx.unam.buzz.rutinas.integration.pasarela.PagosProperties;
import mx.unam.buzz.rutinas.modelo.entity.FacturaDO;
import mx.unam.buzz.rutinas.modelo.entity.OrdenPagoDO;
import mx.unam.buzz.rutinas.modelo.entity.PlanSuscripcionDO;
import mx.unam.buzz.rutinas.modelo.entity.SuscripcionDO;
import mx.unam.buzz.rutinas.persistence.IFacturaRepository;
import mx.unam.buzz.rutinas.persistence.IPlanSuscripcionRepository;
import mx.unam.buzz.rutinas.persistence.ISuscripcionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Lo que pasa cuando una orden queda PAGADA, venga del webhook (tarjeta) o de
 * Cobranza (transferencia): se activa o extiende la suscripcion y se emite el CFDI.
 * Corre dentro de la transaccion de quien lo llama: o se hace todo o nada.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class CierrePago {

    private final ISuscripcionRepository suscripcionRepository;
    private final IPlanSuscripcionRepository planRepository;
    private final IFacturaRepository facturaRepository;
    private final GeneradorCfdi generadorCfdi;
    private final PagosProperties propiedades;

    void cerrar(OrdenPagoDO entity, OrdenPago orden, LocalDateTime ahora) {
        if (facturaRepository.existsByIdOrden(entity.getId())) {
            return;
        }
        SuscripcionDO fila = suscripcionRepository.findById(entity.getIdSuscripcion())
                .orElseThrow(() -> new BusinessException(PagoErrorCode.SUSCRIPCION_NOT_FOUND.getCode(),
                        HttpStatus.NOT_FOUND));
        Suscripcion suscripcion = Suscripcion.reconstituir(fila.getIdPlan(),
                EstadoSuscripcion.valueOf(fila.getEstado()), fila.getFechaInicio(), fila.getVigenciaFin());
        suscripcion.activar(orden.getIdPlan(), orden.getPeriodo().getMeses(), ahora.toLocalDate());
        fila.setIdPlan(suscripcion.getIdPlan());
        fila.setEstado(suscripcion.getEstadoGuardado().name());
        fila.setFechaInicio(suscripcion.getFechaInicio());
        fila.setVigenciaFin(suscripcion.getVigenciaFin());
        // Los datos fiscales de la ultima compra quedan como sugerencia para la siguiente
        fila.setRfc(entity.getRfc());
        fila.setRazonSocial(entity.getRazonSocial());
        fila.setCodigoPostal(entity.getCodigoPostal());
        fila.setRegimenFiscal(entity.getRegimenFiscal());
        fila.setUsoCfdi(entity.getUsoCfdi());
        suscripcionRepository.save(fila);

        PagosProperties.Emisor emisor = propiedades.emisor();
        Factura factura = Factura.emitir(orden, emisor.rfc(), OrdenPagoMapper.datosFiscales(entity), ahora);
        String formaPago = orden.getMetodo() == MetodoPago.TARJETA
                ? PagoConstant.FORMA_PAGO_TARJETA : PagoConstant.FORMA_PAGO_TRANSFERENCIA;
        String plan = planRepository.findById(orden.getIdPlan()).map(PlanSuscripcionDO::getNombre).orElse("");
        String concepto = "Suscripcion TaskIn plan " + plan + ", " + orden.getPeriodo().name().toLowerCase()
                + " (" + orden.getPeriodo().getMeses() + (orden.getPeriodo().getMeses() == 1 ? " mes)" : " meses)");

        FacturaDO cfdi = new FacturaDO();
        cfdi.setIdOrden(entity.getId());
        cfdi.setFolioFiscal(factura.getFolioFiscal());
        cfdi.setSerie(PagoConstant.SERIE_FACTURA);
        cfdi.setRfcEmisor(factura.getRfcEmisor());
        cfdi.setRazonSocialEmisor(emisor.razonSocial());
        cfdi.setRfcReceptor(factura.getRfcReceptor());
        cfdi.setRazonSocialReceptor(factura.getRazonSocialReceptor());
        cfdi.setCodigoPostalReceptor(factura.getCodigoPostalReceptor());
        cfdi.setRegimenFiscalReceptor(factura.getRegimenFiscalReceptor());
        cfdi.setUsoCfdi(factura.getUsoCfdi());
        cfdi.setFormaPago(formaPago);
        cfdi.setSubtotal(factura.getDesglose().subtotal());
        cfdi.setDescuento(factura.getDesglose().descuento());
        cfdi.setIva(factura.getDesglose().iva());
        cfdi.setTotal(factura.getDesglose().total());
        cfdi.setFechaTimbrado(factura.getFechaTimbrado());
        cfdi.setXml(generadorCfdi.generar(factura, emisor, PagoConstant.SERIE_FACTURA, entity.getFolio(), formaPago,
                concepto));
        facturaRepository.save(cfdi);
        log.info("Orden {} pagada: suscripcion {} hasta {}, CFDI {}", entity.getFolio(), fila.getEstado(),
                fila.getVigenciaFin(), factura.getFolioFiscal());
    }
}
