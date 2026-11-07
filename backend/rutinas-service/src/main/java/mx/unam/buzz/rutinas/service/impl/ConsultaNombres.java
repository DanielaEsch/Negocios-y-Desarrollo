package mx.unam.buzz.rutinas.service.impl;

import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.modelo.entity.AreaDO;
import mx.unam.buzz.rutinas.modelo.entity.EjecucionDO;
import mx.unam.buzz.rutinas.modelo.entity.RutinaDO;
import mx.unam.buzz.rutinas.modelo.entity.UsuarioDO;
import mx.unam.buzz.rutinas.persistence.IAreaRepository;
import mx.unam.buzz.rutinas.persistence.IEjecucionRepository;
import mx.unam.buzz.rutinas.persistence.IRutinaRepository;
import mx.unam.buzz.rutinas.persistence.IUsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Carga por lotes lo que los listados necesitan para mostrar nombres en vez de ids
 * (una consulta por tabla, no una por renglon). Las llaves foraneas son Long planos,
 * asi que la "union" se hace aqui y no con relaciones JPA.
 */
@Component
@RequiredArgsConstructor
class ConsultaNombres {

    private final IAreaRepository areaRepository;
    private final IRutinaRepository rutinaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IEjecucionRepository ejecucionRepository;

    Map<Long, String> areas(Collection<Long> ids) {
        return areaRepository.findAllById(sinNulos(ids)).stream()
                .collect(Collectors.toMap(AreaDO::getId, AreaDO::getNombre));
    }

    Map<Long, RutinaDO> rutinas(Collection<Long> ids) {
        return rutinaRepository.findAllById(sinNulos(ids)).stream()
                .collect(Collectors.toMap(RutinaDO::getId, Function.identity()));
    }

    Map<Long, String> usuarios(Collection<Long> ids) {
        return usuarioRepository.findAllById(sinNulos(ids)).stream()
                .collect(Collectors.toMap(UsuarioDO::getId, UsuarioDO::getNombre));
    }

    Map<Long, EjecucionDO> ejecuciones(Collection<Long> ids) {
        return ejecucionRepository.findAllById(sinNulos(ids)).stream()
                .collect(Collectors.toMap(EjecucionDO::getId, Function.identity()));
    }

    private static java.util.Set<Long> sinNulos(Collection<Long> ids) {
        return ids.stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
