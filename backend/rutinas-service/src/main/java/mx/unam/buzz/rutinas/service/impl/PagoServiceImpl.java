package mx.unam.buzz.rutinas.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.unam.buzz.rutinas.common.constant.PagoConstant;
import mx.unam.buzz.rutinas.common.constant.PagoErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import mx.unam.buzz.rutinas.common.security.UsuarioSesionVO;
import mx.unam.buzz.rutinas.common.util.ValidationUtil;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.ResponseVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.pago.CobroTarjetaRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.CotizacionRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.CotizacionVO;
import mx.unam.buzz.rutinas.common.vo.pago.DatosFiscalesVO;
import mx.unam.buzz.rutinas.common.vo.pago.DesgloseVO;
import mx.unam.buzz.rutinas.common.vo.pago.FacturaXmlVO;
import mx.unam.buzz.rutinas.common.vo.pago.FindListOrdenRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.OrdenPagoRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.OrdenPagoVO;
import mx.unam.buzz.rutinas.common.vo.pago.PlanVO;
import mx.unam.buzz.rutinas.common.vo.pago.SuscripcionVO;
import mx.unam.buzz.rutinas.common.vo.pago.TransferenciaRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.ValidacionPagoRequestVO;
import mx.unam.buzz.rutinas.common.vo.pago.WebhookRespuestaVO;
import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.pago.Desglose;
import mx.unam.buzz.rutinas.dominio.pago.EstadoOrden;
import mx.unam.buzz.rutinas.dominio.pago.EstadoSuscripcion;
import mx.unam.buzz.rutinas.dominio.pago.Factura;
import mx.unam.buzz.rutinas.dominio.pago.MetodoPago;
import mx.unam.buzz.rutinas.dominio.pago.OrdenPago;
import mx.unam.buzz.rutinas.dominio.pago.Periodo;
import mx.unam.buzz.rutinas.dominio.pago.Suscripcion;
import mx.unam.buzz.rutinas.integration.pasarela.CargoPasarela;
import mx.unam.buzz.rutinas.integration.pasarela.IPasarelaPagos;
import mx.unam.buzz.rutinas.integration.pasarela.PagosProperties;
import mx.unam.buzz.rutinas.integration.pasarela.PasarelaException;
import mx.unam.buzz.rutinas.integration.pasarela.VerificadorFirmaWebhook;
import mx.unam.buzz.rutinas.modelo.entity.EventoPagoDO;
import mx.unam.buzz.rutinas.modelo.entity.FacturaDO;
import mx.unam.buzz.rutinas.modelo.entity.OrdenPagoDO;
import mx.unam.buzz.rutinas.modelo.entity.PlanSuscripcionDO;
import mx.unam.buzz.rutinas.modelo.entity.SuscripcionDO;
import mx.unam.buzz.rutinas.persistence.IEventoPagoRepository;
import mx.unam.buzz.rutinas.persistence.IFacturaRepository;
import mx.unam.buzz.rutinas.persistence.IOrdenPagoRepository;
import mx.unam.buzz.rutinas.persistence.IPlanSuscripcionRepository;
import mx.unam.buzz.rutinas.persistence.ISuscripcionRepository;
import mx.unam.buzz.rutinas.service.IPagoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Ciclo universal del pago, con las dos variantes de la matriz de monetizacion:
 * <ul>
 *   <li>B2C, tarjeta: el navegador tokeniza en la pasarela, TaskIn cobra con el token
 *       y el webhook firmado decide PAGADO o RECHAZADO.</li>
 *   <li>B2B, transferencia: orden de compra con CLABE y referencia; el cliente reporta
 *       la clave de rastreo y Cobranza valida el deposito.</li>
 * </ul>
 * En ambos casos, al quedar PAGADA, CierrePago activa la suscripcion y emite el CFDI.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements IPagoService {

    private static final Set<String> ORDER_FIELDS = Set.of("id", "fechaOrden", "estado", "total");
    private static final String ALFABETO_FOLIO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter FECHA_FOLIO = DateTimeFormatter.ofPattern("yyMMdd");
    private static final SecureRandom AZAR = new SecureRandom();

    private final IPlanSuscripcionRepository planRepository;
    private final ISuscripcionRepository suscripcionRepository;
    private final IOrdenPagoRepository ordenRepository;
    private final IEventoPagoRepository eventoRepository;
    private final IFacturaRepository facturaRepository;
    private final IPasarelaPagos pasarela;
    private final VerificadorFirmaWebhook verificador;
    private final CierrePago cierre;
    private final PagosProperties propiedades;
    private final ConsultaNombres nombres;
    private final TransactionTemplate transaccion;
    private final ObjectMapper json;

    // ------------------------------------------------------------------ catalogo

    @Override
    @Transactional(readOnly = true)
    public SingleResponseVO<List<PlanVO>> planes(RequestVO<Void> request) {
        Long idPlanActual = suscripcionRepository.findFirstByActivoTrueOrderByIdAsc()
                .map(SuscripcionDO::getIdPlan).orElse(null);
        return SingleResponseVO.ok(planRepository.findByActivoTrueOrderByOrdenAsc().stream()
                .map(plan -> planVO(plan, idPlanActual)).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SingleResponseVO<SuscripcionVO> suscripcion(RequestVO<Void> request) {
        SuscripcionDO fila = suscripcionActual();
        Suscripcion suscripcion = aDominio(fila);
        LocalDate hoy = LocalDate.now();
        PlanSuscripcionDO plan = planRepository.findById(fila.getIdPlan())
                .orElseThrow(() -> error(PagoErrorCode.PLAN_NOT_FOUND, HttpStatus.NOT_FOUND));
        DatosFiscalesVO datosFiscales = request.getSesion().esAdministrador() && fila.getRfc() != null
                ? new DatosFiscalesVO(fila.getRfc(), fila.getRazonSocial(), fila.getCodigoPostal(),
                fila.getRegimenFiscal(), fila.getUsoCfdi())
                : null;
        return SingleResponseVO.ok(new SuscripcionVO(planVO(plan, plan.getId()), suscripcion.estadoAl(hoy).name(),
                fila.getFechaInicio(), fila.getVigenciaFin(), suscripcion.diasRestantes(hoy), datosFiscales));
    }

    // ------------------------------------------------------------------ cotizacion y orden

    /** Lo que se cobra, calculado solo con datos del servidor. */
    private record Calculo(SuscripcionDO suscripcion, PlanSuscripcionDO plan, Periodo periodo, String cupon,
                           int porcentajeCupon, BigDecimal credito, Desglose desglose) {
    }

    private Calculo calcular(CotizacionRequestVO parameters, LocalDate hoy) {
        PlanSuscripcionDO plan = planRepository.findByClaveAndActivoTrue(StringUtils.upperCase(parameters.getPlan()))
                .orElseThrow(() -> error(PagoErrorCode.PLAN_NOT_FOUND, HttpStatus.NOT_FOUND));
        Periodo periodo = enumValor(Periodo.class, parameters.getPeriodo());
        String cupon = ValidationUtil.normalize(parameters.getCupon(), PagoConstant.MAX_SIZE_CUPON);
        int porcentaje = 0;
        if (cupon != null) {
            Integer encontrado = PagoConstant.CUPONES.get(cupon);
            ValidationUtil.validate(encontrado == null, PagoErrorCode.CUPON_INVALIDO.getCode(),
                    HttpStatus.BAD_REQUEST);
            porcentaje = encontrado;
        }
        SuscripcionDO fila = suscripcionActual();
        BigDecimal precioActual = planRepository.findById(fila.getIdPlan())
                .map(PlanSuscripcionDO::getPrecioMensual).orElse(BigDecimal.ZERO);
        BigDecimal credito = aDominio(fila).creditoPorCambio(plan.getId(), precioActual, hoy);
        Desglose desglose = Desglose.calcular(plan.getPrecioMensual(), periodo, porcentaje, cupon, credito);
        return new Calculo(fila, plan, periodo, cupon, porcentaje, credito, desglose);
    }

    @Override
    @Transactional(readOnly = true)
    public SingleResponseVO<CotizacionVO> cotizar(RequestVO<CotizacionRequestVO> request) {
        Calculo calculo = calcular(request.getParameters(), LocalDate.now());
        return SingleResponseVO.ok(new CotizacionVO(planVO(calculo.plan(), calculo.suscripcion().getIdPlan()),
                calculo.periodo().name(), calculo.cupon(), DesgloseVO.de(calculo.desglose())));
    }

    @Override
    @Transactional
    public SingleResponseVO<OrdenPagoVO> crearOrden(RequestVO<OrdenPagoRequestVO> request) {
        OrdenPagoRequestVO parameters = request.getParameters();
        LocalDateTime ahora = LocalDateTime.now();
        Calculo calculo = calcular(parameters, ahora.toLocalDate());
        MetodoPago metodo = enumValor(MetodoPago.class, parameters.getMetodo());
        // Mientras un cobro esta en proceso no se abre otro: evita pagar dos veces
        ValidationUtil.validate(ordenRepository.existsByIdSuscripcionAndEstadoAndActivoTrue(
                        calculo.suscripcion().getId(), EstadoOrden.PROCESANDO.name()),
                PagoErrorCode.COBRO_EN_PROCESO.getCode(), HttpStatus.CONFLICT);

        DatosFiscalesVO fiscales = parameters.getDatosFiscales();
        Factura.DatosFiscales receptor = new Factura.DatosFiscales(StringUtils.upperCase(fiscales.getRfc()),
                StringUtils.upperCase(StringUtils.trim(fiscales.getRazonSocial())), fiscales.getCodigoPostal(),
                fiscales.getRegimenFiscal(), StringUtils.upperCase(fiscales.getUsoCfdi()));
        receptor.validar();

        OrdenPago orden = OrdenPago.crear(calculo.plan().getId(), calculo.periodo(), metodo, calculo.desglose(),
                calculo.cupon(), ahora);
        OrdenPagoDO entity = new OrdenPagoDO();
        entity.setFolio(nuevoFolio(ahora.toLocalDate()));
        entity.setIdSuscripcion(calculo.suscripcion().getId());
        entity.setIdPlan(orden.getIdPlan());
        entity.setIdSolicita(request.getSesion().getId());
        entity.setPeriodo(orden.getPeriodo().name());
        entity.setMetodo(orden.getMetodo().name());
        entity.setPrecioMensual(orden.getDesglose().precioMensual());
        entity.setCupon(orden.getCupon());
        entity.setPorcentajeCupon(calculo.porcentajeCupon());
        entity.setCredito(Desglose.centavos(calculo.credito()));
        entity.setSubtotal(orden.getDesglose().subtotal());
        entity.setDescuento(orden.getDesglose().descuento());
        entity.setIva(orden.getDesglose().iva());
        entity.setTotal(orden.getDesglose().total());
        entity.setMoneda(Desglose.MONEDA);
        entity.setFechaOrden(orden.getFechaCreacion());
        entity.setFechaExpiracion(orden.getFechaExpiracion());
        entity.setRfc(receptor.rfc());
        entity.setRazonSocial(receptor.razonSocial());
        entity.setCodigoPostal(receptor.codigoPostal());
        entity.setRegimenFiscal(receptor.regimenFiscal());
        entity.setUsoCfdi(receptor.usoCfdi());
        OrdenPagoMapper.aplicar(orden, entity);
        ordenRepository.save(entity);
        log.info("Orden {} creada: {} {} por {} MXN", entity.getFolio(), entity.getMetodo(), entity.getPeriodo(),
                entity.getTotal());
        return SingleResponseVO.ok(aVO(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseVO<OrdenPagoVO> findList(RequestVO<FindListOrdenRequestVO> request) {
        FindListOrdenRequestVO filtros = request.getParameters();
        String orderBy = ORDER_FIELDS.contains(request.getOrderBy()) ? request.getOrderBy() : "fechaOrden";
        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getOrderType())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<OrdenPagoDO> page = ordenRepository.findList(StringUtils.defaultString(filtros.getEstado()).toUpperCase(),
                StringUtils.defaultString(filtros.getMetodo()).toUpperCase(),
                PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, orderBy)));
        return ResponseVO.of(page, aVOs(page.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public SingleResponseVO<OrdenPagoVO> findDetail(RequestVO<IdRequestVO> request) {
        return SingleResponseVO.ok(aVO(buscar(request.getParameters().getId())));
    }

    // ------------------------------------------------------------------ B2C: tarjeta

    /**
     * Sin @Transactional a proposito: la llamada a la pasarela va entre dos transacciones
     * cortas, para no retener una conexion a la base mientras responde un tercero.
     */
    @Override
    public SingleResponseVO<OrdenPagoVO> cobrarConTarjeta(RequestVO<CobroTarjetaRequestVO> request) {
        Long id = request.getParameters().getId();
        String token = request.getParameters().getToken();

        // 1) PENDIENTE_PAGO -> PROCESANDO, guardado antes de llamar a nadie
        OrdenPagoDO procesando = transaccion.execute(estado -> {
            OrdenPagoDO entity = buscar(id);
            OrdenPago orden = OrdenPagoMapper.aDominio(entity);
            orden.iniciarCobroConTarjeta(LocalDateTime.now());
            OrdenPagoMapper.aplicar(orden, entity);
            return ordenRepository.saveAndFlush(entity);
        });

        // 2) La pasarela abre el cargo (o lo rechaza de inmediato)
        CargoPasarela cargo = null;
        PasarelaException fallo = null;
        try {
            cargo = pasarela.crearCargo(token, procesando.getTotal(), procesando.getMoneda(), procesando.getFolio(),
                    "Suscripcion TaskIn " + procesando.getPeriodo().toLowerCase());
        } catch (PasarelaException ex) {
            fallo = ex;
        }

        // 3) Se registra la respuesta inmediata; el resultado final lo trae el webhook
        CargoPasarela abierto = cargo;
        PasarelaException rechazo = fallo;
        OrdenPagoDO resultado = transaccion.execute(estado -> {
            OrdenPagoDO entity = buscar(id);
            OrdenPago orden = OrdenPagoMapper.aDominio(entity);
            if (orden.getEstado() != EstadoOrden.PROCESANDO) {
                return entity; // el webhook gano la carrera y ya resolvio la orden
            }
            if (rechazo != null) {
                orden.rechazar(rechazo.getMessage());
            } else if (orden.getReferenciaPago() == null) {
                orden.registrarCargo(abierto.id(), abierto.marca(), abierto.ultimos4());
            }
            OrdenPagoMapper.aplicar(orden, entity);
            return ordenRepository.saveAndFlush(entity);
        });
        if (fallo != null && !fallo.isRechazo()) {
            throw error(PagoErrorCode.PASARELA_NO_DISPONIBLE, HttpStatus.BAD_GATEWAY);
        }
        return SingleResponseVO.ok(transaccion.execute(estado -> aVO(resultado)));
    }

    /**
     * El webhook es la fuente de verdad: firma valida, idempotente por ID de evento y
     * siempre responde 2xx si ya lo proceso (para que la pasarela deje de reintentar).
     */
    @Override
    @Transactional
    public WebhookRespuestaVO procesarWebhook(String cuerpo, String firma) {
        ValidationUtil.validate(!verificador.esValida(cuerpo, firma), PagoErrorCode.FIRMA_INVALIDA.getCode(),
                HttpStatus.BAD_REQUEST);
        JsonNode evento;
        try {
            evento = json.readTree(cuerpo);
        } catch (Exception ex) {
            throw error(PagoErrorCode.FIRMA_INVALIDA, HttpStatus.BAD_REQUEST);
        }
        String idEvento = evento.path("id").asText("");
        String tipo = evento.path("tipo").asText("");
        ValidationUtil.validate(idEvento.isBlank(), PagoErrorCode.FIRMA_INVALIDA.getCode(), HttpStatus.BAD_REQUEST);
        if (eventoRepository.existsByIdEvento(idEvento)) {
            log.info("Webhook {} repetido: se ignora (idempotencia)", idEvento);
            return new WebhookRespuestaVO(true, PagoConstant.WEBHOOK_DUPLICADO);
        }

        JsonNode datos = evento.path("datos");
        LocalDateTime ahora = LocalDateTime.now();
        OrdenPagoDO orden = ordenRepository.findByFolio(datos.path("referencia").asText("")).orElse(null);
        String resultado = orden == null ? PagoConstant.WEBHOOK_IGNORADO : aplicarEvento(orden, tipo, datos, ahora);

        EventoPagoDO bitacora = new EventoPagoDO();
        bitacora.setIdEvento(idEvento);
        bitacora.setTipo(StringUtils.left(tipo, 40));
        bitacora.setIdOrden(orden == null ? null : orden.getId());
        bitacora.setResultado(resultado);
        bitacora.setFechaRecibido(ahora);
        eventoRepository.save(bitacora);
        log.info("Webhook {} ({}) para {}: {}", idEvento, tipo, orden == null ? "orden desconocida" : orden.getFolio(),
                resultado);
        return new WebhookRespuestaVO(true, resultado);
    }

    private String aplicarEvento(OrdenPagoDO entity, String tipo, JsonNode datos, LocalDateTime ahora) {
        OrdenPago orden = OrdenPagoMapper.aDominio(entity);
        String idCargo = datos.path("cargo").asText("");
        if (orden.getReferenciaPago() != null && !orden.getReferenciaPago().equals(idCargo)) {
            log.warn("El cargo {} no es el de la orden {}", idCargo, entity.getFolio());
            return PagoConstant.WEBHOOK_IGNORADO;
        }
        try {
            if (orden.getReferenciaPago() == null) {
                orden.registrarCargo(idCargo, datos.at("/tarjeta/marca").asText(null),
                        datos.at("/tarjeta/ultimos4").asText(null));
            }
            switch (tipo) {
                case "cargo.exitoso" -> orden.confirmarPorPasarela(new BigDecimal(datos.path("monto").asText("0")),
                        ahora);
                case "cargo.fallido" -> orden.rechazar(datos.path("motivo").asText("La pasarela rechazo el cargo"));
                default -> {
                    return PagoConstant.WEBHOOK_IGNORADO;
                }
            }
        } catch (ReglaNegocioException | NumberFormatException ex) {
            // Orden ya cerrada, monto alterado, etc.: se registra y no se toca la orden
            log.warn("Webhook no aplicable a {}: {}", entity.getFolio(), ex.getMessage());
            return PagoConstant.WEBHOOK_IGNORADO;
        }
        OrdenPagoMapper.aplicar(orden, entity);
        ordenRepository.save(entity);
        if (orden.getEstado() == EstadoOrden.PAGADO) {
            cierre.cerrar(entity, orden, ahora);
        }
        return PagoConstant.WEBHOOK_PROCESADO;
    }

    // ------------------------------------------------------------------ B2B: transferencia

    @Override
    @Transactional
    public SingleResponseVO<OrdenPagoVO> reportarTransferencia(RequestVO<TransferenciaRequestVO> request) {
        OrdenPagoDO entity = buscar(request.getParameters().getId());
        OrdenPago orden = OrdenPagoMapper.aDominio(entity);
        orden.reportarTransferencia(request.getParameters().getClaveRastreo(), LocalDateTime.now());
        OrdenPagoMapper.aplicar(orden, entity);
        ordenRepository.save(entity);
        return SingleResponseVO.ok(aVO(entity));
    }

    @Override
    @Transactional
    public SingleResponseVO<OrdenPagoVO> validarTransferencia(RequestVO<ValidacionPagoRequestVO> request) {
        ValidacionPagoRequestVO parameters = request.getParameters();
        UsuarioSesionVO sesion = request.getSesion();
        OrdenPagoDO entity = buscar(parameters.getId());
        ValidationUtil.validate(!MetodoPago.TRANSFERENCIA.name().equals(entity.getMetodo()),
                PagoErrorCode.SOLO_TRANSFERENCIAS.getCode(), HttpStatus.CONFLICT);
        OrdenPago orden = OrdenPagoMapper.aDominio(entity);
        LocalDateTime ahora = LocalDateTime.now();
        switch (StringUtils.upperCase(parameters.getEstado())) {
            case "PAGADO" -> orden.validarTransferencia(sesion.getId(), sesion.getPerfil(), ahora);
            case "RECHAZADO" -> orden.rechazar(parameters.getMotivo());
            default -> throw error(PagoErrorCode.STATE_NOT_SUPPORTED, HttpStatus.BAD_REQUEST);
        }
        OrdenPagoMapper.aplicar(orden, entity);
        ordenRepository.save(entity);
        if (orden.getEstado() == EstadoOrden.PAGADO) {
            cierre.cerrar(entity, orden, ahora);
        }
        return SingleResponseVO.ok(aVO(entity));
    }

    // ------------------------------------------------------------------ factura y vencimiento

    @Override
    @Transactional(readOnly = true)
    public FacturaXmlVO facturaXml(RequestVO<IdRequestVO> request) {
        FacturaDO factura = facturaRepository.findByIdAndActivoTrue(request.getParameters().getId())
                .orElseThrow(() -> error(PagoErrorCode.FACTURA_NOT_FOUND, HttpStatus.NOT_FOUND));
        String folio = ordenRepository.findById(factura.getIdOrden()).map(OrdenPagoDO::getFolio)
                .orElse(factura.getFolioFiscal());
        return new FacturaXmlVO("CFDI-" + folio + ".xml", factura.getXml());
    }

    @Override
    @Transactional
    public int vencerOrdenes(LocalDateTime ahora) {
        List<OrdenPagoDO> vencidas = ordenRepository.findByEstadoAndFechaExpiracionBeforeAndActivoTrue(
                EstadoOrden.PENDIENTE_PAGO.name(), ahora);
        for (OrdenPagoDO entity : vencidas) {
            OrdenPago orden = OrdenPagoMapper.aDominio(entity);
            orden.vencer(ahora);
            OrdenPagoMapper.aplicar(orden, entity);
        }
        ordenRepository.saveAll(vencidas);
        if (!vencidas.isEmpty()) {
            log.info("{} ordenes sin pagar pasaron a VENCIDO", vencidas.size());
        }
        return vencidas.size();
    }

    // ------------------------------------------------------------------ apoyo

    private SuscripcionDO suscripcionActual() {
        return suscripcionRepository.findFirstByActivoTrueOrderByIdAsc()
                .orElseThrow(() -> error(PagoErrorCode.SUSCRIPCION_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private static Suscripcion aDominio(SuscripcionDO fila) {
        return Suscripcion.reconstituir(fila.getIdPlan(), EstadoSuscripcion.valueOf(fila.getEstado()),
                fila.getFechaInicio(), fila.getVigenciaFin());
    }

    private OrdenPagoDO buscar(Long id) {
        return ordenRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> error(PagoErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private static <E extends Enum<E>> E enumValor(Class<E> tipo, String valor) {
        try {
            return Enum.valueOf(tipo, StringUtils.upperCase(StringUtils.trim(valor)));
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw error(PagoErrorCode.PERIODO_O_METODO_INVALIDO, HttpStatus.BAD_REQUEST);
        }
    }

    private static BusinessException error(PagoErrorCode code, HttpStatus status) {
        return new BusinessException(code.getCode(), status);
    }

    /** TK-yyMMdd-XXXX, sin letras que se confundan (O/0, I/1). Es la referencia del SPEI. */
    private String nuevoFolio(LocalDate hoy) {
        String folio;
        do {
            StringBuilder sufijo = new StringBuilder(4);
            for (int i = 0; i < 4; i++) {
                sufijo.append(ALFABETO_FOLIO.charAt(AZAR.nextInt(ALFABETO_FOLIO.length())));
            }
            folio = "TK-" + hoy.format(FECHA_FOLIO) + "-" + sufijo;
        } while (ordenRepository.findByFolio(folio).isPresent());
        return folio;
    }

    private static PlanVO planVO(PlanSuscripcionDO plan, Long idPlanActual) {
        BigDecimal anual = Desglose.centavos(plan.getPrecioMensual().multiply(BigDecimal.valueOf(
                Periodo.ANUAL.getMeses() - Periodo.ANUAL.getMesesDeRegalo())));
        return new PlanVO(plan.getId(), plan.getClave(), plan.getNombre(), plan.getDescripcion(),
                plan.getPrecioMensual(), anual, plan.getId().equals(idPlanActual));
    }

    private OrdenPagoVO aVO(OrdenPagoDO entity) {
        return aVOs(List.of(entity)).getFirst();
    }

    private List<OrdenPagoVO> aVOs(List<OrdenPagoDO> ordenes) {
        if (ordenes.isEmpty()) {
            return List.of();
        }
        List<Long> ids = ordenes.stream().map(OrdenPagoDO::getId).toList();
        Map<Long, PlanSuscripcionDO> planes = planRepository.findAllById(
                        ordenes.stream().map(OrdenPagoDO::getIdPlan).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(PlanSuscripcionDO::getId, Function.identity()));
        Map<Long, FacturaDO> facturas = facturaRepository.findByIdOrdenInAndActivoTrue(ids).stream()
                .collect(Collectors.toMap(FacturaDO::getIdOrden, Function.identity()));
        Map<Long, String> usuarios = nombres.usuarios(ordenes.stream()
                .flatMap(o -> Stream.of(o.getIdSolicita(), o.getIdValida())).toList());
        PagosProperties.Transferencia cuenta = propiedades.transferencia();

        return ordenes.stream().map(o -> {
            PlanSuscripcionDO plan = planes.get(o.getIdPlan());
            FacturaDO factura = facturas.get(o.getId());
            return new OrdenPagoVO(o.getId(), o.getFolio(), plan == null ? null : planVO(plan, null),
                    o.getPeriodo(), o.getMetodo(), o.getEstado(), DesgloseVO.de(OrdenPagoMapper.desglose(o)),
                    o.getCupon(), o.getFechaOrden(), o.getFechaExpiracion(), o.getFechaPago(), o.getReferenciaPago(),
                    o.getUltimos4() == null ? null : new OrdenPagoVO.TarjetaVO(o.getMarcaTarjeta(), o.getUltimos4()),
                    o.getMotivoRechazo(),
                    new DatosFiscalesVO(o.getRfc(), o.getRazonSocial(), o.getCodigoPostal(), o.getRegimenFiscal(),
                            o.getUsoCfdi()),
                    MetodoPago.TRANSFERENCIA.name().equals(o.getMetodo())
                            ? new OrdenPagoVO.DatosTransferenciaVO(cuenta.banco(), cuenta.clabe(),
                            cuenta.beneficiario(), o.getFolio())
                            : null,
                    factura == null ? null : new OrdenPagoVO.FacturaVO(factura.getId(), factura.getFolioFiscal(),
                            factura.getSerie() + "-" + o.getFolio().substring(3), factura.getFechaTimbrado(),
                            factura.getRfcEmisor(), factura.getRazonSocialEmisor(), factura.getFormaPago()),
                    usuarios.get(o.getIdSolicita()), usuarios.get(o.getIdValida()));
        }).toList();
    }
}
