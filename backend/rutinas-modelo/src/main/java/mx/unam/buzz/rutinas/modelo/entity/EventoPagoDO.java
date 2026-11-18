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
 * Webhook recibido de la pasarela. El ID del evento es unico: idempotencia.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "EVENTO_PAGO")
public class EventoPagoDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "TX_ID_EVENTO", nullable = false, length = 60)
    private String idEvento;

    @Column(name = "TX_TIPO", nullable = false, length = 40)
    private String tipo;

    @Column(name = "FK_ORDEN")
    private Long idOrden;

    @Column(name = "TX_RESULTADO", nullable = false, length = 20)
    private String resultado;

    @Column(name = "DD_FECHA_RECIBIDO", nullable = false)
    private LocalDateTime fechaRecibido;
}
