package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.TareaDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ITareaRepository extends JpaRepository<TareaDO, Long> {

    List<TareaDO> findByIdRutinaAndActivoTrueOrderByOrdenAsc(Long idRutina);

    List<TareaDO> findByIdRutinaInAndActivoTrueOrderByOrdenAsc(Collection<Long> idsRutina);
}
