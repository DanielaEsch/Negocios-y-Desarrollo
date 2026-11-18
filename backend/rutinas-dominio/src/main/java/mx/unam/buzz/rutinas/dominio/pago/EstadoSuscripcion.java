package mx.unam.buzz.rutinas.dominio.pago;

/**
 * Situacion de la suscripcion de un negocio. PRUEBA y ACTIVA se guardan; VENCIDA se
 * deduce de la fecha (no hace falta un proceso que la marque).
 */
public enum EstadoSuscripcion {

    /** Los primeros dias, sin pagar. */
    PRUEBA,

    /** Pagada y dentro de su vigencia. */
    ACTIVA,

    /** Paso la fecha de vigencia sin renovar. */
    VENCIDA
}
