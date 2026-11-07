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
 * Usuario del sistema. El hash nunca sale de la capa de servicio: ningun VO lo declara.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "USUARIO")
public class UsuarioDO extends AuditableDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_ID")
    private Long id;

    @Column(name = "TX_NOMBRE", nullable = false, length = 150)
    private String nombre;

    @Column(name = "TX_CORREO", nullable = false, length = 150)
    private String correo;

    @ToString.Exclude
    @Column(name = "TX_HASH_CONTRASENA", nullable = false, length = 100)
    private String hashContrasena;

    /** OPERADOR, SUPERVISOR o ADMINISTRADOR (enum Perfil del dominio). */
    @Column(name = "TX_PERFIL", nullable = false, length = 20)
    private String perfil;

    @Column(name = "DN_CONSENTIMIENTO_DATOS", nullable = false)
    private Boolean consentimientoDatos;

    @Column(name = "DD_FECHA_CONSENTIMIENTO")
    private LocalDateTime fechaConsentimiento;
}
