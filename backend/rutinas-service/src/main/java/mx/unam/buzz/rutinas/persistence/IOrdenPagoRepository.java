package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.OrdenPagoDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IOrdenPagoRepository extends JpaRepository<OrdenPagoDO, Long> {

    Optional<OrdenPagoDO> findByIdAndActivoTrue(Long id);

    Optional<OrdenPagoDO> findByFolio(String folio);

    boolean existsByIdSuscripcionAndEstadoAndActivoTrue(Long idSuscripcion, String estado);

    List<OrdenPagoDO> findByEstadoAndFechaExpiracionBeforeAndActivoTrue(String estado, LocalDateTime limite);

    @Query("SELECT o FROM OrdenPagoDO o WHERE o.activo = true"
            + " AND (:estado = '' OR o.estado = :estado)"
            + " AND (:metodo = '' OR o.metodo = :metodo)")
    Page<OrdenPagoDO> findList(@Param("estado") String estado, @Param("metodo") String metodo, Pageable pageable);
}
