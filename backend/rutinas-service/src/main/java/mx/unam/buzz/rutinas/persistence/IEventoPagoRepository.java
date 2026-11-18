package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.EventoPagoDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEventoPagoRepository extends JpaRepository<EventoPagoDO, Long> {

    boolean existsByIdEvento(String idEvento);

    long countByIdOrden(Long idOrden);
}
