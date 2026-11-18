package mx.unam.buzz.rutinas.dominio.usuario;

/**
 * Perfil del usuario dentro del negocio.
 */
public enum Perfil {

    /** Ejecuta las tareas de las areas que tiene asignadas. */
    OPERADOR,

    /** Revisa la ejecucion y las incidencias de sus areas. */
    SUPERVISOR,

    /** Configura areas, rutinas y usuarios, y paga la suscripcion. */
    ADMINISTRADOR,

    /**
     * Personal de TaskIn (no del negocio cliente) que concilia las transferencias:
     * es el "panel de administracion" del modelo B2B. No tiene areas ni se da de alta
     * desde el modulo de equipo.
     */
    COBRANZA
}
