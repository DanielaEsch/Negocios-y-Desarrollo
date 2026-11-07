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
 * Area a cargo de un usuario. Las llaves foraneas son Long planos, como en el resto del modelo.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "USUARIO_AREA")
public class UsuarioAreaDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "FK_USUARIO", nullable = false)
    private Long idUsuario;

    @Column(name = "FK_AREA", nullable = false)
    private Long idArea;
}
