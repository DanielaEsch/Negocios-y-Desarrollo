package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.EjecucionDO;
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
public interface IEjecucionRepository extends JpaRepository<EjecucionDO, Long> {

    Optional<EjecucionDO> findByIdAndActivoTrue(Long id);

    boolean existsByIdRutinaAndFechaProgramadaAndActivoTrue(Long idRutina, LocalDateTime fechaProgramada);

    boolean existsByFechaProgramadaBetween(LocalDateTime desde, LocalDateTime hasta);

    List<EjecucionDO> findByIdRutinaAndEstadoAndFechaProgramadaAfterAndActivoTrue(Long idRutina, String estado,
                                                                                  LocalDateTime despues);

    @Query("SELECT e FROM EjecucionDO e WHERE e.activo = true"
            + " AND e.fechaProgramada >= :desde AND e.fechaProgramada < :hasta ORDER BY e.fechaProgramada, e.id")
    List<EjecucionDO> findRango(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    List<EjecucionDO> findByEstadoAndFechaProgramadaBeforeAndActivoTrue(String estado, LocalDateTime antes);

    /** Rondas de un rango de fechas con filtros opcionales; '' y 0 significan "sin filtro". */
    @Query("SELECT e FROM EjecucionDO e JOIN RutinaDO r ON r.id = e.idRutina"
            + " WHERE e.activo = true"
            + " AND e.fechaProgramada >= :desde AND e.fechaProgramada < :hasta"
            + " AND (:estado = '' OR e.estado = :estado)"
            + " AND (:idOperador = 0 OR e.idOperador = :idOperador)"
            + " AND (:todas = true OR r.idArea IN :areas)")
    Page<EjecucionDO> findList(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                               @Param("estado") String estado, @Param("idOperador") Long idOperador,
                               @Param("todas") boolean todas, @Param("areas") List<Long> areas,
                               Pageable pageable);

    /** Conteo por area y estado de un rango (para el tablero): [idArea, estado, total]. */
    @Query("SELECT r.idArea, e.estado, COUNT(e) FROM EjecucionDO e JOIN RutinaDO r ON r.id = e.idRutina"
            + " WHERE e.activo = true"
            + " AND e.fechaProgramada >= :desde AND e.fechaProgramada < :hasta"
            + " AND (:todas = true OR r.idArea IN :areas)"
            + " GROUP BY r.idArea, e.estado")
    List<Object[]> countByAreaAndEstado(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                                        @Param("todas") boolean todas, @Param("areas") List<Long> areas);

    /** Rondas de un rango con su estado (para la tendencia): [fechaProgramada, estado]. */
    @Query("SELECT e.fechaProgramada, e.estado FROM EjecucionDO e JOIN RutinaDO r ON r.id = e.idRutina"
            + " WHERE e.activo = true"
            + " AND e.fechaProgramada >= :desde AND e.fechaProgramada < :hasta"
            + " AND (:todas = true OR r.idArea IN :areas)")
    List<Object[]> findEstados(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                               @Param("todas") boolean todas, @Param("areas") List<Long> areas);
}
