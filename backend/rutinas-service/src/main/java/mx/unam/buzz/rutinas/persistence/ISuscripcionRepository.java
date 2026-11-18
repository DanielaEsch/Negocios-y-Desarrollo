package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.SuscripcionDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ISuscripcionRepository extends JpaRepository<SuscripcionDO, Long> {

    /** Una instalacion = un negocio = una suscripcion activa. */
    Optional<SuscripcionDO> findFirstByActivoTrueOrderByIdAsc();
}
