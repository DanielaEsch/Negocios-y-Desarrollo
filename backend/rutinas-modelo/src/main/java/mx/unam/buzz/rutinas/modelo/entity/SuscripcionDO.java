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
import java.time.LocalDate;

/**
 * Suscripcion del negocio a TaskIn, con sus datos fiscales por omision.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "SUSCRIPCION")
public class SuscripcionDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "FK_PLAN", nullable = false)
    private Long idPlan;

    /** PRUEBA o ACTIVA. VENCIDA se deriva de la vigencia. */
    @Column(name = "TX_ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "DD_FECHA_INICIO", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "DD_VIGENCIA_FIN", nullable = false)
    private LocalDate vigenciaFin;

    @Column(name = "TX_RFC", length = 13)
    private String rfc;

    @Column(name = "TX_RAZON_SOCIAL", length = 300)
    private String razonSocial;

    @Column(name = "TX_CODIGO_POSTAL", length = 5)
    private String codigoPostal;

    @Column(name = "TX_REGIMEN_FISCAL", length = 3)
    private String regimenFiscal;

    @Column(name = "TX_USO_CFDI", length = 4)
    private String usoCfdi;

    @Version
    @Column(name = "DN_VERSION", nullable = false)
    private Long version;
}
