package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.PlanSuscripcionDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPlanSuscripcionRepository extends JpaRepository<PlanSuscripcionDO, Long> {

    Optional<PlanSuscripcionDO> findByClaveAndActivoTrue(String clave);

    List<PlanSuscripcionDO> findByActivoTrueOrderByOrdenAsc();
}
