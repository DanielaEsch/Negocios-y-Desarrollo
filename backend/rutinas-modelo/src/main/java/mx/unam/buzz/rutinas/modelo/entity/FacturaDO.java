package mx.unam.buzz.rutinas.modelo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CFDI 4.0 simulado de una orden pagada. Inmutable: el servicio solo la inserta, nunca la actualiza.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "FACTURA")
public class FacturaDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "FK_ORDEN", nullable = false)
    private Long idOrden;

    @Column(name = "TX_FOLIO_FISCAL", nullable = false, length = 36)
    private String folioFiscal;

    @Column(name = "TX_SERIE", nullable = false, length = 10)
    private String serie;

    @Column(name = "TX_RFC_EMISOR", nullable = false, length = 13)
    private String rfcEmisor;

    @Column(name = "TX_RAZON_SOCIAL_EMISOR", nullable = false, length = 300)
    private String razonSocialEmisor;

    @Column(name = "TX_RFC_RECEPTOR", nullable = false, length = 13)
    private String rfcReceptor;

    @Column(name = "TX_RAZON_SOCIAL_RECEPTOR", nullable = false, length = 300)
    private String razonSocialReceptor;

    @Column(name = "TX_CODIGO_POSTAL_RECEPTOR", nullable = false, length = 5)
    private String codigoPostalReceptor;

    @Column(name = "TX_REGIMEN_FISCAL_RECEPTOR", nullable = false, length = 3)
    private String regimenFiscalReceptor;

    @Column(name = "TX_USO_CFDI", nullable = false, length = 4)
    private String usoCfdi;

    @Column(name = "TX_FORMA_PAGO", nullable = false, length = 2)
    private String formaPago;

    @Column(name = "DN_SUBTOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "DN_DESCUENTO", nullable = false, precision = 10, scale = 2)
    private BigDecimal descuento;

    @Column(name = "DN_IVA", nullable = false, precision = 10, scale = 2)
    private BigDecimal iva;

    @Column(name = "DN_TOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "DD_FECHA_TIMBRADO", nullable = false)
    private LocalDateTime fechaTimbrado;

    @ToString.Exclude
    @Column(name = "TX_XML", nullable = false, length = 20000)
    private String xml;
}
