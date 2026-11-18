package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.FacturaDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IFacturaRepository extends JpaRepository<FacturaDO, Long> {

    Optional<FacturaDO> findByIdAndActivoTrue(Long id);

    List<FacturaDO> findByIdOrdenInAndActivoTrue(Collection<Long> idsOrden);

    boolean existsByIdOrden(Long idOrden);
}
