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
 * Paso de una rutina.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "TAREA")
public class TareaDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "FK_RUTINA", nullable = false)
    private Long idRutina;

    @Column(name = "TX_NOMBRE", nullable = false, length = 150)
    private String nombre;

    @Column(name = "DN_ORDEN", nullable = false)
    private Integer orden;
}
