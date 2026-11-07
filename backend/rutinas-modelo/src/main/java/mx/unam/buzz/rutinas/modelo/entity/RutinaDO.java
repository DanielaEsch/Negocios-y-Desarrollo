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
import java.time.LocalTime;

/**
 * Rutina de un area: que hay que hacer y cada cuando. Las columnas de horario que
 * se usan dependen de la frecuencia.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "RUTINA")
public class RutinaDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "FK_AREA", nullable = false)
    private Long idArea;

    @Column(name = "TX_NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "TX_DESCRIPCION", length = 250)
    private String descripcion;

    /** CADA_2_HORAS, DIARIA, SEMANAL o POR_EVENTO (enum Frecuencia del dominio). */
    @Column(name = "TX_FRECUENCIA", nullable = false, length = 20)
    private String frecuencia;

    @Column(name = "DD_HORA_INICIO")
    private LocalTime horaInicio;

    @Column(name = "DD_HORA_FIN")
    private LocalTime horaFin;

    /** 1 = lunes ... 7 = domingo (ISO-8601), solo para SEMANAL. */
    @Column(name = "DN_DIA_SEMANA")
    private Integer diaSemana;
}
