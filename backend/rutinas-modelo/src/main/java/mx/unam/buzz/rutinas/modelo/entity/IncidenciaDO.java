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
 * Algo que salio mal durante una ejecucion.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "INCIDENCIA")
public class IncidenciaDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "FK_EJECUCION", nullable = false)
    private Long idEjecucion;

    @Column(name = "FK_USUARIO_REPORTA", nullable = false)
    private Long idReporta;

    @Column(name = "TX_DESCRIPCION", nullable = false, length = 500)
    private String descripcion;

    /** PENDIENTE, REVISION o RESUELTA (enum EstadoIncidencia del dominio). */
    @Column(name = "TX_ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "DD_FECHA_REPORTE", nullable = false)
    private LocalDateTime fechaReporte;

    @Column(name = "FK_USUARIO_REVISOR")
    private Long idRevisor;

    @Column(name = "TX_COMENTARIO_RESOLUCION", length = 500)
    private String comentarioResolucion;

    @Column(name = "DD_FECHA_RESOLUCION")
    private LocalDateTime fechaResolucion;
}
