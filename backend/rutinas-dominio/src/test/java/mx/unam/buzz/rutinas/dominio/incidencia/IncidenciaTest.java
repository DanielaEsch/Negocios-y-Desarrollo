package mx.unam.buzz.rutinas.dominio.incidencia;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.comun.TransicionInvalidaException;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ciclo de vida de la incidencia en aislamiento.
 */
class IncidenciaTest {

    private static final Long OPERADOR = 7L;
    private static final Long SUPERVISOR = 3L;

    private Incidencia incidencia;

    @BeforeEach
    void setUp() {
        incidencia = Incidencia.reportar(100L, OPERADOR, "  Falta jabon en el bano 2  ");
    }

    @Test
    void reportarCreaIncidenciaPendiente() {
        assertEquals(EstadoIncidencia.PENDIENTE, incidencia.getEstado());
        assertEquals("Falta jabon en el bano 2", incidencia.getDescripcion());
        assertNotNull(incidencia.getFechaReporte());
    }

    @Test
    void reportarSinDescripcionFalla() {
        assertThrows(ReglaNegocioException.class, () -> Incidencia.reportar(100L, OPERADOR, " "));
    }

    @Test
    void flujoCompletoPendienteRevisionResuelta() {
        incidencia.tomarEnRevision(SUPERVISOR, Perfil.SUPERVISOR);
        incidencia.resolver("Se repuso el jabon y se agrego al checklist diario");

        assertEquals(EstadoIncidencia.RESUELTA, incidencia.getEstado());
        assertEquals(SUPERVISOR, incidencia.getIdRevisor());
        assertNotNull(incidencia.getFechaResolucion());
    }

    @Test
    void noSePuedeResolverSinPasarPorRevision() {
        assertThrows(TransicionInvalidaException.class, () -> incidencia.resolver("Listo"));
        assertEquals(EstadoIncidencia.PENDIENTE, incidencia.getEstado());
    }

    @Test
    void unOperadorNoPuedeRevisarIncidencias() {
        assertThrows(ReglaNegocioException.class, () -> incidencia.tomarEnRevision(OPERADOR, Perfil.OPERADOR));
        assertEquals(EstadoIncidencia.PENDIENTE, incidencia.getEstado());
    }

    @Test
    void resolverSinComentarioFalla() {
        incidencia.tomarEnRevision(SUPERVISOR, Perfil.SUPERVISOR);

        assertThrows(ReglaNegocioException.class, () -> incidencia.resolver(""));
        assertEquals(EstadoIncidencia.REVISION, incidencia.getEstado());
    }

    @Test
    void unaIncidenciaResueltaNoSeReabre() {
        incidencia.tomarEnRevision(SUPERVISOR, Perfil.SUPERVISOR);
        incidencia.resolver("Se repuso el jabon");

        assertThrows(TransicionInvalidaException.class,
                () -> incidencia.tomarEnRevision(SUPERVISOR, Perfil.SUPERVISOR));
    }
}
