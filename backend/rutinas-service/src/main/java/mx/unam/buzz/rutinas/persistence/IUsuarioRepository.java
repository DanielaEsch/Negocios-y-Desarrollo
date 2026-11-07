package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.UsuarioDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUsuarioRepository extends JpaRepository<UsuarioDO, Long> {

    Optional<UsuarioDO> findByIdAndActivoTrue(Long id);

    Optional<UsuarioDO> findByCorreoAndActivoTrue(String correo);

    boolean existsByCorreoAndActivoTrue(String correo);

    /**
     * Listado con filtros opcionales. Los filtros de texto vacios llegan como '' y no
     * como null para no depender del tipado de null en PostgreSQL. Con todas = false
     * solo salen los usuarios que comparten alguna de las areas indicadas.
     */
    @Query("SELECT u FROM UsuarioDO u"
            + " WHERE u.activo = true"
            + " AND (:nombre = '' OR UPPER(u.nombre) LIKE CONCAT('%', :nombre, '%'))"
            + " AND (:perfil = '' OR u.perfil = :perfil)"
            + " AND (:todas = true OR EXISTS (SELECT 1 FROM UsuarioAreaDO ua"
            + "      WHERE ua.idUsuario = u.id AND ua.activo = true AND ua.idArea IN :areas))")
    Page<UsuarioDO> findList(@Param("nombre") String nombre, @Param("perfil") String perfil,
                             @Param("todas") boolean todas, @Param("areas") List<Long> areas, Pageable pageable);

    /** Operadores activos de un area (los usa la simulacion de la demo). */
    @Query("SELECT u FROM UsuarioDO u, UsuarioAreaDO ua"
            + " WHERE ua.idUsuario = u.id AND ua.activo = true AND u.activo = true"
            + " AND ua.idArea = :idArea AND u.perfil = :perfil ORDER BY u.id")
    List<UsuarioDO> findByAreaAndPerfil(@Param("idArea") Long idArea, @Param("perfil") String perfil);
}
