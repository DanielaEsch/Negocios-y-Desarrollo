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
 * Foto de evidencia de una ronda. El archivo vive en el almacenamiento; aqui, la llave.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "EVIDENCIA")
public class EvidenciaDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "FK_EJECUCION", nullable = false)
    private Long idEjecucion;

    @Column(name = "FK_USUARIO_SUBE", nullable = false)
    private Long idSube;

    @Column(name = "TX_TIPO_CONTENIDO", nullable = false, length = 30)
    private String tipoContenido;

    @Column(name = "DN_TAMANO_BYTES", nullable = false)
    private Long tamanoBytes;

    @Column(name = "TX_LLAVE_ALMACEN", nullable = false, length = 200)
    private String llaveAlmacen;

    @Column(name = "TX_NOTA", length = 200)
    private String nota;

    @Column(name = "DD_FECHA_CAPTURA", nullable = false)
    private LocalDateTime fechaCaptura;
}
