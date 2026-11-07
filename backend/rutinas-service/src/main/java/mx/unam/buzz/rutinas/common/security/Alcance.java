package mx.unam.buzz.rutinas.common.security;

import java.util.List;

/**
 * Traduce la sesion a filtros de consulta por area. Las consultas reciben siempre
 * una lista no vacia (IN () no es SQL valido) y una bandera para "todas".
 */
public final class Alcance {

    private static final List<Long> NINGUNA = List.of(-1L);

    private Alcance() {
    }

    /** Areas visibles; si ademas se pidio un area, la interseccion. */
    public static List<Long> areas(UsuarioSesionVO sesion, Long idAreaFiltro) {
        if (idAreaFiltro != null) {
            return sesion.tieneArea(idAreaFiltro) ? List.of(idAreaFiltro) : NINGUNA;
        }
        if (sesion.esAdministrador() || sesion.getAreas().isEmpty()) {
            return NINGUNA;
        }
        return List.copyOf(sesion.getAreas());
    }

    /** true cuando la consulta no debe filtrar por area. */
    public static boolean sinFiltro(UsuarioSesionVO sesion, Long idAreaFiltro) {
        return idAreaFiltro == null && sesion.esAdministrador();
    }
}
