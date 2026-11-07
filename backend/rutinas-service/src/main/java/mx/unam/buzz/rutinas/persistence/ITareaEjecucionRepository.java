package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.TareaEjecucionDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ITareaEjecucionRepository extends JpaRepository<TareaEjecucionDO, Long> {

    List<TareaEjecucionDO> findByIdEjecucionAndActivoTrueOrderByIdAsc(Long idEjecucion);

    /** Avance por ronda: [idEjecucion, total, completadas]. */
    @Query("SELECT t.idEjecucion, COUNT(t), SUM(CASE WHEN t.estado = 'COMPLETADA' THEN 1 ELSE 0 END)"
            + " FROM TareaEjecucionDO t WHERE t.activo = true AND t.idEjecucion IN :ids GROUP BY t.idEjecucion")
    List<Object[]> countAvance(@Param("ids") Collection<Long> idsEjecucion);
}
