package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.RutinaDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRutinaRepository extends JpaRepository<RutinaDO, Long> {

    Optional<RutinaDO> findByIdAndActivoTrue(Long id);

    boolean existsByIdAreaAndNombreAndActivoTrue(Long idArea, String nombre);

    boolean existsByIdAreaAndNombreAndActivoTrueAndIdNot(Long idArea, String nombre, Long id);

    List<RutinaDO> findByActivoTrue();

    @Query("SELECT r FROM RutinaDO r"
            + " WHERE r.activo = true"
            + " AND (:todas = true OR r.idArea IN :areas)"
            + " AND (:nombre = '' OR r.nombre LIKE CONCAT('%', :nombre, '%'))")
    Page<RutinaDO> findList(@Param("todas") boolean todas, @Param("areas") List<Long> areas,
                            @Param("nombre") String nombre, Pageable pageable);
}
