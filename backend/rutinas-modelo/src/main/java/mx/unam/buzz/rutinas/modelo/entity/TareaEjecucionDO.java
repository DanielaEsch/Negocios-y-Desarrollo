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
import java.time.LocalDateTime;

/**
 * Avance de una tarea dentro de una ejecucion.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "TAREA_EJECUCION")
public class TareaEjecucionDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "FK_EJECUCION", nullable = false)
    private Long idEjecucion;

    @Column(name = "FK_TAREA", nullable = false)
    private Long idTarea;

    /** PENDIENTE, EN_PROCESO o COMPLETADA (enum EstadoTareaEjecucion del dominio). */
    @Column(name = "TX_ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "DD_FECHA_COMPLETADA")
    private LocalDateTime fechaCompletada;
}
