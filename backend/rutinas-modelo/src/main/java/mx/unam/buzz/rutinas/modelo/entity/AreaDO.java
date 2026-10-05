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

/**
 * Area del negocio (cocina, recepcion, mantenimiento...).
 * Es el modulo de ejemplo: los demas se construyen copiando este molde.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "AREA")
public class AreaDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "TX_NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "TX_DESCRIPCION", length = 250)
    private String descripcion;
}
