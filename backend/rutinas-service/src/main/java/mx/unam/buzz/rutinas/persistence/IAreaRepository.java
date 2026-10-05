package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.AreaDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAreaRepository extends JpaRepository<AreaDO, Long> {

    Optional<AreaDO> findByIdAndActivoTrue(Long id);

    boolean existsByNombreAndActivoTrue(String nombre);

    boolean existsByNombreAndActivoTrueAndIdNot(String nombre, Long id);

    @Query("SELECT t FROM AreaDO t"
            + " WHERE t.activo = true"
            + " AND (:nombre IS NULL OR t.nombre LIKE CONCAT('%', :nombre, '%'))")
    Page<AreaDO> findList(@Param("nombre") String nombre, Pageable pageable);
}
