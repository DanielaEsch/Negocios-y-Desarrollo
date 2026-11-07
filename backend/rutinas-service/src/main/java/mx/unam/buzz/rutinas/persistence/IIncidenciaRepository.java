package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.IncidenciaDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IIncidenciaRepository extends JpaRepository<IncidenciaDO, Long> {

    Optional<IncidenciaDO> findByIdAndActivoTrue(Long id);

    List<IncidenciaDO> findByIdEjecucionAndActivoTrueOrderByFechaReporteAsc(Long idEjecucion);

    /** Incidencias abiertas por ronda: [idEjecucion, total]. */
    @Query("SELECT i.idEjecucion, COUNT(i) FROM IncidenciaDO i"
            + " WHERE i.activo = true AND i.estado <> 'RESUELTA' AND i.idEjecucion IN :ids"
            + " GROUP BY i.idEjecucion")
    List<Object[]> countAbiertas(@Param("ids") Collection<Long> idsEjecucion);

    /**
     * Listado con filtros. Con idReporta distinto de 0 solo salen las que reporto ese
     * usuario (la vista del operador).
     */
    @Query("SELECT i FROM IncidenciaDO i, EjecucionDO e, RutinaDO r"
            + " WHERE e.id = i.idEjecucion AND r.id = e.idRutina AND i.activo = true"
            + " AND (:estado = '' OR i.estado = :estado)"
            + " AND (:idReporta = 0 OR i.idReporta = :idReporta)"
            + " AND (:todas = true OR r.idArea IN :areas)")
    Page<IncidenciaDO> findList(@Param("estado") String estado, @Param("idReporta") Long idReporta,
                                @Param("todas") boolean todas, @Param("areas") List<Long> areas,
                                Pageable pageable);

    @Query("SELECT COUNT(i) FROM IncidenciaDO i, EjecucionDO e, RutinaDO r"
            + " WHERE e.id = i.idEjecucion AND r.id = e.idRutina AND i.activo = true AND i.estado <> 'RESUELTA'"
            + " AND (:todas = true OR r.idArea IN :areas)")
    long countAbiertas(@Param("todas") boolean todas, @Param("areas") List<Long> areas);
}
