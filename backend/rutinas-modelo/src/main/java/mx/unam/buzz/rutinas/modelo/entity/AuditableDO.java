package mx.unam.buzz.rutinas.modelo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Clase base de todas las entidades: auditoria y borrado logico.
 * Las entidades solo declaran sus columnas propias (patron Template Method).
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @CreatedBy
    @Column(name = "DN_USUARIO_CREADOR", nullable = false, updatable = false)
    protected Long usuarioCreador;

    @CreatedDate
    @Column(name = "DD_FECHA_CREACION", nullable = false, updatable = false)
    protected LocalDateTime fechaCreacion;

    @LastModifiedBy
    @Column(name = "DN_USUARIO_MODIFICADOR")
    protected Long usuarioModificador;

    @LastModifiedDate
    @Column(name = "DD_FECHA_MODIFICACION")
    protected LocalDateTime fechaModificacion;

    @Column(name = "DN_ACTIVO", nullable = false)
    protected Boolean activo;

    @PrePersist
    public void prePersist() {
        if (activo == null) {
            activo = Boolean.TRUE;
        }
    }
}
