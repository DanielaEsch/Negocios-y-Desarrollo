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
import java.time.LocalDateTime;

/**
 * Ocurrencia concreta de una rutina. La columna de version es el bloqueo optimista
 * del dominio: JPA la compara en cada UPDATE.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "EJECUCION")
public class EjecucionDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "FK_RUTINA", nullable = false)
    private Long idRutina;

    @Column(name = "DD_FECHA_PROGRAMADA", nullable = false)
    private LocalDateTime fechaProgramada;

    /** Estado de la maquina de estados (enum EstadoEjecucion del dominio). */
    @Column(name = "TX_ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "FK_OPERADOR")
    private Long idOperador;

    @Column(name = "DD_FECHA_INICIO")
    private LocalDateTime fechaInicio;

    @Column(name = "DD_FECHA_FIN")
    private LocalDateTime fechaFin;

    @Version
    @Column(name = "DN_VERSION", nullable = false)
    private Long version;
}
