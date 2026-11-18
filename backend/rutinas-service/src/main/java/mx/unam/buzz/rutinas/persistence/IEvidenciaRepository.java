package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.EvidenciaDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IEvidenciaRepository extends JpaRepository<EvidenciaDO, Long> {

    Optional<EvidenciaDO> findByIdAndActivoTrue(Long id);

    List<EvidenciaDO> findByIdEjecucionAndActivoTrueOrderByFechaCapturaAsc(Long idEjecucion);

    int countByIdEjecucionAndActivoTrue(Long idEjecucion);
}
