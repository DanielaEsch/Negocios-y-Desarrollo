package mx.unam.buzz.rutinas.persistence;

import mx.unam.buzz.rutinas.modelo.entity.UsuarioAreaDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface IUsuarioAreaRepository extends JpaRepository<UsuarioAreaDO, Long> {

    List<UsuarioAreaDO> findByIdUsuarioAndActivoTrue(Long idUsuario);

    List<UsuarioAreaDO> findByIdUsuarioInAndActivoTrue(Collection<Long> idsUsuario);
}
