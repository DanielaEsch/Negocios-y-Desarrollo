package mx.unam.buzz.rutinas.service;

import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.pago.CobroTarjetaRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.CotizacionRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.CotizacionVO;
import mx.unam.buzz.rutinas.common.vo.pago.FacturaXmlVO;
import mx.unam.buzz.rutinas.common.vo.pago.FindListOrdenRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.OrdenPagoRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.OrdenPagoVO;
import mx.unam.buzz.rutinas.common.vo.pago.PlanVO;
import mx.unam.buzz.rutinas.common.vo.pago.SuscripcionVO;
import mx.unam.buzz.rutinas.common.vo.pago.TransferenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.ValidacionPagoRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.WebhookRespuestaVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Monetizacion de TaskIn: planes, ordenes, cobro con tarjeta (B2C), transferencia
 * validada por Cobranza (B2B), webhook de la pasarela y CFDI.
 */
public interface IPagoService {

    SingleResponseVO<List<PlanVO>> planes(RequestVO<Void> request);

    SingleResponseVO<SuscripcionVO> suscripcion(RequestVO<Void> request);

    SingleResponseVO<CotizacionVO> cotizar(RequestVO<CotizacionRequestVO> request);

    SingleResponseVO<OrdenPagoVO> crearOrden(RequestVO<OrdenPagoRequestVO> request);

    ResponseVO<OrdenPagoVO> findList(RequestVO<FindListOrdenRequestVO> request);

    SingleResponseVO<OrdenPagoVO> findDetail(RequestVO<IdRequestVO> request);

    SingleResponseVO<OrdenPagoVO> cobrarConTarjeta(RequestVO<CobroTarjetaRequestVO> request);

    SingleResponseVO<OrdenPagoVO> reportarTransferencia(RequestVO<TransferenciaRequestVO> request);

    SingleResponseVO<OrdenPagoVO> validarTransferencia(RequestVO<ValidacionPagoRequestVO> request);

    WebhookRespuestaVO procesarWebhook(String cuerpo, String firma);

    FacturaXmlVO facturaXml(RequestVO<IdRequestVO> request);

    /** Pasa a VENCIDO las ordenes sin pagar cuyo plazo ya paso. */
    int vencerOrdenes(LocalDateTime ahora);
}
