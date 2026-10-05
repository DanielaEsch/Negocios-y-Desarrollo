package mx.unam.buzz.rutinas.dominio.usuario;

/**
 * Perfil del usuario dentro del negocio.
 */
public enum Perfil {

    /** Ejecuta las tareas de las areas que tiene asignadas. */
    OPERADOR,

    /** Revisa la ejecucion y las incidencias de sus areas. */
    SUPERVISOR,

    /** Configura areas, rutinas y usuarios. */
    ADMINISTRADOR
}
