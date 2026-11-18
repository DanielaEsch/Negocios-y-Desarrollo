package mx.unam.buzz.rutinas.modelo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Orden de pago de la suscripcion. Guarda las entradas del calculo y el desglose resultante;
 * de la tarjeta, solo marca y ultimos 4.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "ORDEN_PAGO")
public class OrdenPagoDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "TX_FOLIO", nullable = false, length = 20)
    private String folio;

    @Column(name = "FK_SUSCRIPCION", nullable = false)
    private Long idSuscripcion;

    @Column(name = "FK_PLAN", nullable = false)
    private Long idPlan;

    @Column(name = "FK_USUARIO_SOLICITA", nullable = false)
    private Long idSolicita;

    @Column(name = "TX_PERIODO", nullable = false, length = 10)
    private String periodo;

    @Column(name = "TX_METODO", nullable = false, length = 15)
    private String metodo;

    @Column(name = "DN_PRECIO_MENSUAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual;

    @Column(name = "TX_CUPON", length = 20)
    private String cupon;

    @Column(name = "DN_PORCENTAJE_CUPON", nullable = false)
    private Integer porcentajeCupon;

    @Column(name = "DN_CREDITO", nullable = false, precision = 10, scale = 2)
    private BigDecimal credito;

    @Column(name = "DN_SUBTOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "DN_DESCUENTO", nullable = false, precision = 10, scale = 2)
    private BigDecimal descuento;

    @Column(name = "DN_IVA", nullable = false, precision = 10, scale = 2)
    private BigDecimal iva;

    @Column(name = "DN_TOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "TX_MONEDA", nullable = false, length = 3)
    private String moneda;

    /** Enum EstadoOrden del dominio. */
    @Column(name = "TX_ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "DD_FECHA_ORDEN", nullable = false)
    private LocalDateTime fechaOrden;

    @Column(name = "DD_FECHA_EXPIRACION", nullable = false)
    private LocalDateTime fechaExpiracion;

    /** ID del cargo en la pasarela o clave de rastreo SPEI. */
    @Column(name = "TX_REFERENCIA_PAGO", length = 60)
    private String referenciaPago;

    @Column(name = "TX_TARJETA_MARCA", length = 20)
    private String marcaTarjeta;

    @Column(name = "TX_TARJETA_ULTIMOS4", length = 4)
    private String ultimos4;

    @Column(name = "TX_MOTIVO_RECHAZO", length = 300)
    private String motivoRechazo;

    @Column(name = "FK_USUARIO_VALIDA")
    private Long idValida;

    @Column(name = "DD_FECHA_PAGO")
    private LocalDateTime fechaPago;

    @Column(name = "TX_RFC", nullable = false, length = 13)
    private String rfc;

    @Column(name = "TX_RAZON_SOCIAL", nullable = false, length = 300)
    private String razonSocial;

    @Column(name = "TX_CODIGO_POSTAL", nullable = false, length = 5)
    private String codigoPostal;

    @Column(name = "TX_REGIMEN_FISCAL", nullable = false, length = 3)
    private String regimenFiscal;

    @Column(name = "TX_USO_CFDI", nullable = false, length = 4)
    private String usoCfdi;

    @Version
    @Column(name = "DN_VERSION", nullable = false)
    private Long version;
}
