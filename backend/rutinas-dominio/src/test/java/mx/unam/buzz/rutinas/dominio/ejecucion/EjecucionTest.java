package mx.unam.buzz.rutinas.dominio.ejecucion;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.comun.TransicionInvalidaException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Maquina de estados de la ejecucion en aislamiento: sin Spring y sin base de
 * datos. Equivalente en nuestro dominio a la maquina de estados del pedido.
 */
class EjecucionTest {

    private static final LocalDateTime A_LAS_DOS = LocalDateTime.of(2026, 10, 12, 14, 0);
    private static final Long OPERADOR = 7L;
    private static final Long OTRO_OPERADOR = 8L;
    private static final Long LAVABOS = 1L;
    private static final Long JABON = 2L;

    private Ejecucion ejecucion;

    @BeforeEach
    void setUp() {
        ejecucion = Ejecucion.programar(10L, A_LAS_DOS, List.of(LAVABOS, JABON));
    }

    private void tomarYCompletarTareas() {
        ejecucion.iniciar(OPERADOR, ejecucion.getVersion());
        for (Long tarea : List.of(LAVABOS, JABON)) {
            ejecucion.iniciarTarea(tarea, OPERADOR);
            ejecucion.completarTarea(tarea, OPERADOR);
        }
    }

    // --- Programacion ---

    @Test
    void programarCreaEjecucionProgramadaConTareasPendientes() {
        assertEquals(EstadoEjecucion.PROGRAMADA, ejecucion.getEstado());
        assertNull(ejecucion.getIdOperador());
        assertTrue(ejecucion.getTareas().values().stream().allMatch(e -> e == EstadoTareaEjecucion.PENDIENTE));
    }

    @Test
    void programarSinTareasFalla() {
        assertThrows(ReglaNegocioException.class, () -> Ejecucion.programar(10L, A_LAS_DOS, List.of()));
    }

    // --- Secuencia estricta ---

    @Test
    void flujoCompletoProgramadaEnCursoCompletada() {
        tomarYCompletarTareas();

        ejecucion.completar();

        assertEquals(EstadoEjecucion.COMPLETADA, ejecucion.getEstado());
        assertEquals(OPERADOR, ejecucion.getIdOperador());
    }

    @Test
    void noSePuedeSaltarDeProgramadaACompletada() {
        assertThrows(TransicionInvalidaException.class, ejecucion::completar);
        assertEquals(EstadoEjecucion.PROGRAMADA, ejecucion.getEstado());
    }

    @Test
    void noSePuedeCompletarConTareasPendientes() {
        ejecucion.iniciar(OPERADOR, ejecucion.getVersion());
        ejecucion.iniciarTarea(LAVABOS, OPERADOR);
        ejecucion.completarTarea(LAVABOS, OPERADOR);

        assertThrows(ReglaNegocioException.class, ejecucion::completar);
        assertEquals(EstadoEjecucion.EN_CURSO, ejecucion.getEstado());
    }

    @Test
    void noSePuedeCompletarUnaTareaQueNoSeInicio() {
        ejecucion.iniciar(OPERADOR, ejecucion.getVersion());

        assertThrows(TransicionInvalidaException.class, () -> ejecucion.completarTarea(LAVABOS, OPERADOR));
        assertEquals(EstadoTareaEjecucion.PENDIENTE, ejecucion.getEstadoTarea(LAVABOS));
    }

    @Test
    void soloElOperadorQueTomoLaEjecucionPuedeAvanzarSusTareas() {
        ejecucion.iniciar(OPERADOR, ejecucion.getVersion());

        assertThrows(ReglaNegocioException.class, () -> ejecucion.iniciarTarea(LAVABOS, OTRO_OPERADOR));
    }

    @Test
    void unaEjecucionCompletadaEsIrreversible() {
        tomarYCompletarTareas();
        ejecucion.completar();

        assertThrows(TransicionInvalidaException.class, ejecucion::cancelar);
        assertEquals(EstadoEjecucion.COMPLETADA, ejecucion.getEstado());
    }

    // --- Cancelacion y vencimiento ---

    @Test
    void unaEjecucionEnCursoNoSePuedeCancelar() {
        ejecucion.iniciar(OPERADOR, ejecucion.getVersion());

        assertThrows(TransicionInvalidaException.class, ejecucion::cancelar);
    }

    @Test
    void seMarcaVencidaSoloSiPasoLaFecha() {
        assertThrows(ReglaNegocioException.class, () -> ejecucion.marcarVencida(A_LAS_DOS.minusMinutes(5)));

        ejecucion.marcarVencida(A_LAS_DOS.plusHours(1));

        assertEquals(EstadoEjecucion.VENCIDA, ejecucion.getEstado());
    }

    @Test
    void unaEjecucionVencidaYaNoSePuedeIniciar() {
        ejecucion.marcarVencida(A_LAS_DOS.plusHours(1));

        assertThrows(TransicionInvalidaException.class, () -> ejecucion.iniciar(OPERADOR, ejecucion.getVersion()));
    }
}
