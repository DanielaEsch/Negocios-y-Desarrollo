package mx.unam.buzz.rutinas.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.unam.buzz.pasarela.FirmaWebhook;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;
import mx.unam.buzz.rutinas.integration.pasarela.CargoPasarela;
import mx.unam.buzz.rutinas.integration.pasarela.IPasarelaPagos;
import mx.unam.buzz.rutinas.integration.pasarela.PasarelaException;
import mx.unam.buzz.rutinas.modelo.entity.PlanSuscripcionDO;
import mx.unam.buzz.rutinas.modelo.entity.SuscripcionDO;
import mx.unam.buzz.rutinas.persistence.IEventoPagoRepository;
import mx.unam.buzz.rutinas.persistence.IFacturaRepository;
import mx.unam.buzz.rutinas.persistence.IOrdenPagoRepository;
import mx.unam.buzz.rutinas.persistence.IPlanSuscripcionRepository;
import mx.unam.buzz.rutinas.persistence.ISuscripcionRepository;
import mx.unam.buzz.rutinas.service.IPagoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ciclo del pago por las cuatro capas: cotizacion calculada en el servidor, cobro con
 * token (la pasarela es un doble de prueba), webhook firmado e idempotente,
 * transferencia B2B validada por Cobranza, CFDI y vencimiento.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(SesionesDePrueba.class)
class PagoFlujoTest {

    private static final String SECRETO_WEBHOOK = "whsec_taskin_sandbox";
    private static final String FISCALES = """
            {"rfc": "CLE210514AB1", "razonSocial": "Cafeteria La Esquina SA de CV", "codigoPostal": "04510",
             "regimenFiscal": "601", "usoCfdi": "G03"}""";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private SesionesDePrueba sesiones;
    @Autowired
    private IPlanSuscripcionRepository planRepository;
    @Autowired
    private ISuscripcionRepository suscripcionRepository;
    @Autowired
    private IOrdenPagoRepository ordenRepository;
    @Autowired
    private IEventoPagoRepository eventoRepository;
    @Autowired
    private IFacturaRepository facturaRepository;
    @Autowired
    private IPagoService pagoService;

    @MockitoBean
    private IPasarelaPagos pasarela;

    private final ObjectMapper json = new ObjectMapper();
    private MockMvc mockMvc;
    private String admin;
    private String cobranza;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true)).build();
        admin = sesiones.crear(Perfil.ADMINISTRADOR, List.of()).bearer();
        cobranza = sesiones.crear(Perfil.COBRANZA, List.of()).bearer();

        facturaRepository.deleteAll();
        eventoRepository.deleteAll();
        ordenRepository.deleteAll();
        PlanSuscripcionDO negocio = plan("NEGOCIO", "499.00", 1);
        plan("PRO", "1199.00", 2);
        SuscripcionDO suscripcion = suscripcionRepository.findFirstByActivoTrueOrderByIdAsc()
                .orElseGet(SuscripcionDO::new);
        suscripcion.setIdPlan(negocio.getId());
        suscripcion.setEstado("PRUEBA");
        suscripcion.setFechaInicio(LocalDate.now());
        suscripcion.setVigenciaFin(LocalDate.now().plusDays(14));
        suscripcionRepository.save(suscripcion);
    }

    private PlanSuscripcionDO plan(String clave, String precio, int orden) {
        return planRepository.findByClaveAndActivoTrue(clave).orElseGet(() -> {
            PlanSuscripcionDO plan = new PlanSuscripcionDO();
            plan.setClave(clave);
            plan.setNombre(clave.charAt(0) + clave.substring(1).toLowerCase());
            plan.setDescripcion("Plan " + clave);
            plan.setPrecioMensual(new BigDecimal(precio));
            plan.setOrden(orden);
            return planRepository.save(plan);
        });
    }

    @Test
    void elServidorDictaElPrecio() throws Exception {
        // (499 - 20 %) + 16 % = 399.20 + 63.87
        perform(post("/pagos/cotizaciones"), admin, """
                {"plan": "negocio", "periodo": "MENSUAL", "cupon": "buzz20"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.desglose.subtotal").value(499.00))
                .andExpect(jsonPath("$.data.desglose.descuento").value(99.80))
                .andExpect(jsonPath("$.data.desglose.iva").value(63.87))
                .andExpect(jsonPath("$.data.desglose.total").value(463.07));

        // Anual: 12 meses, 2 de regalo
        perform(post("/pagos/cotizaciones"), admin, """
                {"plan": "NEGOCIO", "periodo": "ANUAL"}""")
                .andExpect(jsonPath("$.data.desglose.subtotal").value(5988.00))
                .andExpect(jsonPath("$.data.desglose.total").value(5788.40));

        // Un total mandado por el cliente no se acepta
        perform(post("/pagos/ordenes"), admin, """
                {"plan": "NEGOCIO", "periodo": "MENSUAL", "metodo": "TARJETA", "total": 1.00,
                 "datosFiscales": %s}""".formatted(FISCALES))
                .andExpect(status().isBadRequest());

        perform(post("/pagos/cotizaciones"), admin, """
                {"plan": "NEGOCIO", "periodo": "MENSUAL", "cupon": "GRATIS100"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1602));
    }

    @Test
    void tarjetaAprobadaPorWebhookActivaLaSuscripcionYFactura() throws Exception {
        JsonNode orden = crearOrden("TARJETA", "BUZZ20");
        long id = orden.at("/data/id").asLong();
        String folio = orden.at("/data/folio").asText();
        assertThat(folio).matches("TK-\\d{6}-[A-Z2-9]{4}");
        when(pasarela.crearCargo(eq("tok_prueba1234"), any(), eq("MXN"), eq(folio), anyString()))
                .thenReturn(new CargoPasarela("ch_prueba", "procesando", "VISA", "4242"));

        perform(post("/pagos/ordenes/" + id + "/cobros"), admin, "{\"token\": \"tok_prueba1234\"}")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.estado").value("PROCESANDO"))
                .andExpect(jsonPath("$.data.tarjeta.ultimos4").value("4242"));

        String evento = evento("cargo.exitoso", folio, "ch_prueba", "463.07");
        webhook(evento, firmar(evento))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("PROCESADO"));

        JsonNode pagada = body(perform(get("/pagos/ordenes/" + id), admin, null)
                .andExpect(jsonPath("$.data.estado").value("PAGADO"))
                .andExpect(jsonPath("$.data.factura.folioFiscal").isNotEmpty()));
        perform(get("/pagos/suscripcion"), admin, null)
                .andExpect(jsonPath("$.data.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.data.plan.clave").value("NEGOCIO"))
                .andExpect(jsonPath("$.data.vigenciaFin").value(LocalDate.now().plusMonths(1).toString()));

        // La pasarela reintenta el mismo evento: no se cobra ni se factura dos veces
        webhook(evento, firmar(evento)).andExpect(jsonPath("$.resultado").value("DUPLICADO"));
        assertThat(facturaRepository.count()).isEqualTo(1);

        perform(get("/pagos/facturas/" + pagada.at("/data/factura/id").asLong() + "/xml"), admin, null)
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Total=\"463.07\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("FormaPago=\"04\"")));
    }

    @Test
    void webhookSinFirmaOConMontoAlteradoNoPagaNada() throws Exception {
        JsonNode orden = crearOrden("TARJETA", null);
        long id = orden.at("/data/id").asLong();
        String folio = orden.at("/data/folio").asText();
        when(pasarela.crearCargo(anyString(), any(), anyString(), anyString(), anyString()))
                .thenReturn(new CargoPasarela("ch_x", "procesando", "VISA", "4242"));
        perform(post("/pagos/ordenes/" + id + "/cobros"), admin, "{\"token\": \"tok_prueba1234\"}");

        String evento = evento("cargo.exitoso", folio, "ch_x", "1728.40");
        webhook(evento, null).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(1605));
        webhook(evento, firmar(evento).replace("v1=", "v1=0")).andExpect(status().isBadRequest());
        long viejo = Instant.now().getEpochSecond() - 3600;
        webhook(evento, FirmaWebhook.cabecera(SECRETO_WEBHOOK, viejo, evento)).andExpect(status().isBadRequest());

        String alterado = evento("cargo.exitoso", folio, "ch_x", "1.00");
        webhook(alterado, firmar(alterado)).andExpect(jsonPath("$.resultado").value("IGNORADO"));
        perform(get("/pagos/ordenes/" + id), admin, null).andExpect(jsonPath("$.data.estado").value("PROCESANDO"));

        // Mientras ese cobro sigue en proceso no se abre otro
        perform(post("/pagos/ordenes"), admin, ordenJson("TARJETA", null))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1606));
    }

    @Test
    void tarjetaDeclinadaQuedaRechazadaSinFactura() throws Exception {
        JsonNode orden = crearOrden("TARJETA", null);
        long id = orden.at("/data/id").asLong();
        String folio = orden.at("/data/folio").asText();
        when(pasarela.crearCargo(anyString(), any(), anyString(), anyString(), anyString()))
                .thenReturn(new CargoPasarela("ch_d", "procesando", "VISA", "0002"));
        perform(post("/pagos/ordenes/" + id + "/cobros"), admin, "{\"token\": \"tok_prueba1234\"}");

        String evento = """
                {"id":"evt_%s","tipo":"cargo.fallido","creado":1,"datos":{"cargo":"ch_d","referencia":"%s",\
                "monto":"1728.40","moneda":"MXN","tarjeta":{"marca":"VISA","ultimos4":"0002"},\
                "motivo":"El banco emisor declino la tarjeta","codigo":"tarjeta_declinada"}}"""
                .formatted(UUID.randomUUID(), folio);
        webhook(evento, firmar(evento)).andExpect(jsonPath("$.resultado").value("PROCESADO"));

        perform(get("/pagos/ordenes/" + id), admin, null)
                .andExpect(jsonPath("$.data.estado").value("RECHAZADO"))
                .andExpect(jsonPath("$.data.motivoRechazo").value("El banco emisor declino la tarjeta"))
                .andExpect(jsonPath("$.data.factura").isEmpty());
        perform(get("/pagos/suscripcion"), admin, null).andExpect(jsonPath("$.data.estado").value("PRUEBA"));
    }

    @Test
    void siLaPasarelaRechazaElTokenLaOrdenQuedaRechazada() throws Exception {
        long id = crearOrden("TARJETA", null).at("/data/id").asLong();
        when(pasarela.crearCargo(anyString(), any(), anyString(), anyString(), anyString()))
                .thenThrow(new PasarelaException(true, "token_invalido", "El token no existe, ya se uso o expiro"));

        perform(post("/pagos/ordenes/" + id + "/cobros"), admin, "{\"token\": \"tok_prueba1234\"}")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.estado").value("RECHAZADO"));
    }

    @Test
    void transferenciaB2BLaValidaCobranza() throws Exception {
        JsonNode orden = crearOrden("TRANSFERENCIA", null);
        long id = orden.at("/data/id").asLong();
        assertThat(orden.at("/data/transferencia/clabe").asText()).hasSize(18);
        assertThat(orden.at("/data/transferencia/referencia").asText()).isEqualTo(orden.at("/data/folio").asText());

        perform(post("/pagos/ordenes/" + id + "/transferencias"), admin, "{\"claveRastreo\": \"bnet0100261115\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("PROCESANDO"))
                .andExpect(jsonPath("$.data.referenciaPago").value("BNET0100261115"));

        // El administrador del negocio no puede validarse su propio pago
        perform(patch("/pagos/ordenes/" + id), admin, "{\"estado\": \"PAGADO\"}").andExpect(status().isForbidden());

        perform(get("/pagos/ordenes?metodo=TRANSFERENCIA&estado=PROCESANDO"), cobranza, null)
                .andExpect(jsonPath("$.totalElements").value(1));
        perform(patch("/pagos/ordenes/" + id), cobranza, "{\"estado\": \"PAGADO\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("PAGADO"))
                .andExpect(jsonPath("$.data.validador").value("COBRANZA de prueba"))
                .andExpect(jsonPath("$.data.factura.formaPago").value("03"));
        perform(get("/pagos/suscripcion"), admin, null).andExpect(jsonPath("$.data.estado").value("ACTIVA"));

        // Cobranza no puede dar por pagada una orden de tarjeta
        long tarjeta = crearOrden("TARJETA", null).at("/data/id").asLong();
        perform(patch("/pagos/ordenes/" + tarjeta), cobranza, "{\"estado\": \"PAGADO\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(1611));
    }

    @Test
    void cambiarDePlanDescuentaLosDiasNoUsados() throws Exception {
        long id = crearOrden("TRANSFERENCIA", null).at("/data/id").asLong();
        perform(post("/pagos/ordenes/" + id + "/transferencias"), admin, "{\"claveRastreo\": \"ABC123456\"}");
        perform(patch("/pagos/ordenes/" + id), cobranza, "{\"estado\": \"PAGADO\"}").andExpect(status().isOk());

        // Recien pagado el plan Negocio (30 dias), subir a Pro abona esos dias
        perform(post("/pagos/cotizaciones"), admin, "{\"plan\": \"PRO\", \"periodo\": \"MENSUAL\"}")
                .andExpect(jsonPath("$.data.desglose.descuentos[0].concepto")
                        .value("Credito por los dias no usados del plan anterior"))
                .andExpect(jsonPath("$.data.desglose.descuento").value(499.00));
    }

    @Test
    void lasOrdenesSinPagarVencen() throws Exception {
        long id = crearOrden("TARJETA", null).at("/data/id").asLong();

        assertThat(pagoService.vencerOrdenes(LocalDateTime.now())).isZero();
        assertThat(pagoService.vencerOrdenes(LocalDateTime.now().plusMinutes(31))).isEqualTo(1);
        perform(get("/pagos/ordenes/" + id), admin, null).andExpect(jsonPath("$.data.estado").value("VENCIDO"));
    }

    @Test
    void permisosPorPerfil() throws Exception {
        String operador = sesiones.crear(Perfil.OPERADOR, List.of()).bearer();

        perform(get("/pagos/planes"), operador, null).andExpect(status().isForbidden());
        perform(post("/pagos/ordenes"), cobranza, ordenJson("TARJETA", null)).andExpect(status().isForbidden());
        perform(get("/pagos/ordenes"), operador, null).andExpect(status().isForbidden());
        // Todos ven el estado de la suscripcion, pero los datos fiscales solo el administrador
        perform(get("/pagos/suscripcion"), operador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.datosFiscales").isEmpty());
        perform(get("/pagos/planes"), admin, null)
                .andExpect(jsonPath("$.data[?(@.clave == 'NEGOCIO')].actual").value(true));
    }

    // ------------------------------------------------------------------ apoyo

    private JsonNode crearOrden(String metodo, String cupon) throws Exception {
        return body(perform(post("/pagos/ordenes"), admin, ordenJson(metodo, cupon))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.estado").value("PENDIENTE_PAGO")));
    }

    private static String ordenJson(String metodo, String cupon) {
        return """
                {"plan": "NEGOCIO", "periodo": "MENSUAL", "metodo": "%s", %s "datosFiscales": %s}"""
                .formatted(metodo, cupon == null ? "" : "\"cupon\": \"" + cupon + "\",", FISCALES);
    }

    private static String evento(String tipo, String folio, String cargo, String monto) {
        return """
                {"id":"evt_%s","tipo":"%s","creado":1,"datos":{"cargo":"%s","referencia":"%s","monto":"%s",\
                "moneda":"MXN","tarjeta":{"marca":"VISA","ultimos4":"4242"},"motivo":null,"codigo":null}}"""
                .formatted(UUID.randomUUID(), tipo, cargo, folio, monto);
    }

    /** Se firma con la misma clase que usa la pasarela: prueba que ambos lados hablan igual. */
    private static String firmar(String cuerpo) {
        return FirmaWebhook.cabecera(SECRETO_WEBHOOK, Instant.now().getEpochSecond(), cuerpo);
    }

    private ResultActions webhook(String cuerpo, String firma) throws Exception {
        MockHttpServletRequestBuilder request = post("/pagos/webhook")
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo);
        if (firma != null) {
            request.header(FirmaWebhook.CABECERA, firma);
        }
        return mockMvc.perform(request);
    }

    private ResultActions perform(MockHttpServletRequestBuilder request, String bearer, String content)
            throws Exception {
        request.header(HttpHeaders.AUTHORIZATION, bearer);
        if (content != null && !content.isEmpty()) {
            request.contentType(MediaType.APPLICATION_JSON).content(content);
        }
        return mockMvc.perform(request);
    }

    private JsonNode body(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }
}
