package mx.unam.buzz.rutinas.facade.impl;

import lombok.RequiredArgsConstructor;
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
import mx.unam.buzz.rutinas.facade.IPagoFacade;
import mx.unam.buzz.rutinas.service.IPagoService;
import org.springframework.stereotype.Component;

import java.util.List;

import static mx.unam.buzz.rutinas.facade.impl.FacadeSupport.conSesion;

/**
 * Agrega la sesion y delega al servicio. El webhook no lleva sesion.
 */
@Component
@RequiredArgsConstructor
public class PagoFacadeImpl implements IPagoFacade {

    private final IPagoService pagoService;

    @Override
    public SingleResponseVO<List<PlanVO>> planes() {
        return pagoService.planes(conSesion(new RequestVO<>()));
    }

    @Override
    public SingleResponseVO<SuscripcionVO> suscripcion() {
        return pagoService.suscripcion(conSesion(new RequestVO<>()));
    }

    @Override
    public SingleResponseVO<CotizacionVO> cotizar(RequestVO<CotizacionRequestVO> request) {
        return pagoService.cotizar(conSesion(request));
    }

    @Override
    public SingleResponseVO<OrdenPagoVO> crearOrden(RequestVO<OrdenPagoRequestVO> request) {
        return pagoService.crearOrden(conSesion(request));
    }

    @Override
    public ResponseVO<OrdenPagoVO> findList(RequestVO<FindListOrdenRequestVO> request) {
        return pagoService.findList(conSesion(request));
    }

    @Override
    public SingleResponseVO<OrdenPagoVO> findDetail(RequestVO<IdRequestVO> request) {
        return pagoService.findDetail(conSesion(request));
    }

    @Override
    public SingleResponseVO<OrdenPagoVO> cobrarConTarjeta(RequestVO<CobroTarjetaRequestVO> request) {
        return pagoService.cobrarConTarjeta(conSesion(request));
    }

    @Override
    public SingleResponseVO<OrdenPagoVO> reportarTransferencia(RequestVO<TransferenciaRequestVO> request) {
        return pagoService.reportarTransferencia(conSesion(request));
    }

    @Override
    public SingleResponseVO<OrdenPagoVO> validarTransferencia(RequestVO<ValidacionPagoRequestVO> request) {
        return pagoService.validarTransferencia(conSesion(request));
    }

    @Override
    public WebhookRespuestaVO procesarWebhook(String cuerpo, String firma) {
        return pagoService.procesarWebhook(cuerpo, firma);
    }

    @Override
    public FacturaXmlVO facturaXml(RequestVO<IdRequestVO> request) {
        return pagoService.facturaXml(conSesion(request));
    }
}
