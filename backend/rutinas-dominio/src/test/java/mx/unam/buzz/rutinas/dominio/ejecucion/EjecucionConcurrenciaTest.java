package mx.unam.buzz.rutinas.dominio.ejecucion;

import mx.unam.buzz.rutinas.dominio.comun.ConcurrenciaException;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Condicion de carrera simulada en la capa de dominio, antes de tocar la base de
 * datos. Es nuestro "Hot Sale": una ejecucion es el "stock = 1" y los operadores
 * que la quieren tomar al mismo tiempo son los compradores.
 */
class EjecucionConcurrenciaTest {

    private static final int OPERADORES = 50;

    private Ejecucion nuevaEjecucion() {
        return Ejecucion.programar(10L, LocalDateTime.of(2026, 10, 12, 14, 0), List.of(1L, 2L));
    }

    @Test
    void cincuentaOperadoresTomanLaMismaEjecucionYSoloUnoGana() throws InterruptedException {
        Ejecucion ejecucion = nuevaEjecucion();
        CountDownLatch listos = new CountDownLatch(OPERADORES);
        CountDownLatch salida = new CountDownLatch(1);
        AtomicInteger aceptados = new AtomicInteger();
        AtomicInteger rechazados = new AtomicInteger();
        AtomicLong ganador = new AtomicLong();

        ExecutorService pool = Executors.newFixedThreadPool(OPERADORES);
        for (long operador = 1; operador <= OPERADORES; operador++) {
            long idOperador = operador;
            pool.submit(() -> {
                // Todos leen la misma version antes de que alguno escriba
                long versionLeida = ejecucion.getVersion();
                listos.countDown();
                try {
                    salida.await();
                    ejecucion.iniciar(idOperador, versionLeida);
                    aceptados.incrementAndGet();
                    ganador.set(idOperador);
                } catch (ConcurrenciaException e) {
                    rechazados.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        listos.await();
        salida.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS), "Los hilos no terminaron a tiempo");

        assertEquals(1, aceptados.get());
        assertEquals(OPERADORES - 1, rechazados.get());
        assertEquals(EstadoEjecucion.EN_CURSO, ejecucion.getEstado());
        assertEquals(ganador.get(), ejecucion.getIdOperador());
        assertEquals(1, ejecucion.getVersion());
    }

    @Test
    void conUnaVersionViejaSeRechazaSinPisarAlPrimero() {
        Ejecucion ejecucion = nuevaEjecucion();
        long versionLeida = ejecucion.getVersion();

        ejecucion.iniciar(1L, versionLeida);

        assertThrows(ConcurrenciaException.class, () -> ejecucion.iniciar(2L, versionLeida));
        assertEquals(1L, ejecucion.getIdOperador());
    }
}
