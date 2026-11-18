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

/**
 * Plan que vende TaskIn. El precio vive aqui y solo aqui: el cliente nunca lo manda.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "PLAN_SUSCRIPCION")
public class PlanSuscripcionDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "TX_CLAVE", nullable = false, length = 20)
    private String clave;

    @Column(name = "TX_NOMBRE", nullable = false, length = 60)
    private String nombre;

    @Column(name = "TX_DESCRIPCION", nullable = false, length = 300)
    private String descripcion;

    /** MXN al mes, sin IVA. */
    @Column(name = "DN_PRECIO_MENSUAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual;

    @Column(name = "DN_ORDEN", nullable = false)
    private Integer orden;
}
