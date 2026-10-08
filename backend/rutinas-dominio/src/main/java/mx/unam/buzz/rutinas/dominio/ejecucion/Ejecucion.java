package mx.unam.buzz.rutinas.dominio.ejecucion;

import mx.unam.buzz.rutinas.dominio.comun.ConcurrenciaException;
import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.comun.TransicionInvalidaException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Una ocurrencia concreta de una rutina (por ejemplo, "revision de banos de las
 * 14:00") con el avance de cada una de sus tareas.
 *
 * Dos reglas viven aqui y no en la base de datos:
 * <ul>
 *   <li>Maquina de estados: el cambio de estado no es actualizar un texto, es
 *       cumplir el contrato de {@link EstadoEjecucion}.</li>
 *   <li>Bloqueo optimista: la ejecucion la toma un solo operador. Si dos la
 *       quieren tomar a la vez, gana el primero y el otro recibe
 *       {@link ConcurrenciaException}. En persistencia esto se traduce a una
 *       columna de version (@Version de JPA).</li>
 * </ul>
 */
public class Ejecucion {

    private final Long idRutina;
    private final LocalDateTime fechaProgramada;
    private final Map<Long, EstadoTareaEjecucion> tareas = new LinkedHashMap<>();
    private EstadoEjecucion estado;
    private Long idOperador;
    private long version;

    private Ejecucion(Long idRutina, LocalDateTime fechaProgramada) {
        this.idRutina = idRutina;
        this.fechaProgramada = fechaProgramada;
    }

    /**
     * Programa una ejecucion nueva con todas sus tareas en PENDIENTE.
     *
     * @param idsTareas tareas de la rutina que hay que cubrir en esta ejecucion
     */
    public static Ejecucion programar(Long idRutina, LocalDateTime fechaProgramada, List<Long> idsTareas) {
        validar(idRutina != null && idRutina > 0, "La rutina no es valida");
        validar(fechaProgramada != null, "La fecha programada es obligatoria");
        validar(idsTareas != null && !idsTareas.isEmpty(), "Una ejecucion necesita al menos una tarea");
        validar(idsTareas.stream().allMatch(Objects::nonNull), "Hay una tarea sin identificador");

        Ejecucion ejecucion = new Ejecucion(idRutina, fechaProgramada);
        idsTareas.forEach(id -> ejecucion.tareas.put(id, EstadoTareaEjecucion.PENDIENTE));
        ejecucion.estado = EstadoEjecucion.PROGRAMADA;
        return ejecucion;
    }

    /**
     * Un operador toma la ejecucion. Recibe la version que leyo: si alguien mas la
     * modifico en medio, la operacion se rechaza en lugar de pisar el cambio.
     *
     * Es synchronized porque representa el UPDATE atomico de la base
     * (UPDATE ... WHERE DN_VERSION = ?): leer, comparar y escribir es un solo paso.
     */
    public synchronized void iniciar(Long idOperador, long versionLeida) {
        if (versionLeida != version) {
            throw new ConcurrenciaException("La ejecucion ya fue tomada o modificada por otro usuario");
        }
        validar(idOperador != null && idOperador > 0, "El operador no es valido");
        cambiarEstado(EstadoEjecucion.EN_CURSO);
        this.idOperador = idOperador;
    }

    public void iniciarTarea(Long idTarea, Long idOperador) {
        cambiarEstadoTarea(idTarea, idOperador, EstadoTareaEjecucion.EN_PROCESO);
    }

    public void completarTarea(Long idTarea, Long idOperador) {
        cambiarEstadoTarea(idTarea, idOperador, EstadoTareaEjecucion.COMPLETADA);
    }

    /** Cierra la ejecucion. Solo se puede si no queda ninguna tarea sin completar. */
    public void completar() {
        if (!estado.puedePasarA(EstadoEjecucion.COMPLETADA)) {
            throw new TransicionInvalidaException("Ejecucion", estado, EstadoEjecucion.COMPLETADA);
        }
        validar(tareas.values().stream().allMatch(e -> e == EstadoTareaEjecucion.COMPLETADA),
                "No se puede completar la ejecucion: hay tareas sin completar");
        cambiarEstado(EstadoEjecucion.COMPLETADA);
    }

    public void cancelar() {
        cambiarEstado(EstadoEjecucion.CANCELADA);
    }

    /** Marca como vencida una ejecucion que nadie inicio antes de su fecha. */
    public void marcarVencida(LocalDateTime ahora) {
        validar(ahora != null && ahora.isAfter(fechaProgramada),
                "La ejecucion todavia no vence");
        cambiarEstado(EstadoEjecucion.VENCIDA);
    }

    public Long getIdRutina() {
        return idRutina;
    }

    public LocalDateTime getFechaProgramada() {
        return fechaProgramada;
    }

    public EstadoEjecucion getEstado() {
        return estado;
    }

    public Long getIdOperador() {
        return idOperador;
    }

    public synchronized long getVersion() {
        return version;
    }

    public EstadoTareaEjecucion getEstadoTarea(Long idTarea) {
        return tareas.get(idTarea);
    }

    public Map<Long, EstadoTareaEjecucion> getTareas() {
        return Collections.unmodifiableMap(tareas);
    }

    private void cambiarEstado(EstadoEjecucion destino) {
        if (!estado.puedePasarA(destino)) {
            throw new TransicionInvalidaException("Ejecucion", estado, destino);
        }
        estado = destino;
        version++;
    }

    private void cambiarEstadoTarea(Long idTarea, Long idOperador, EstadoTareaEjecucion destino) {
        validar(estado == EstadoEjecucion.EN_CURSO, "La ejecucion no esta en curso");
        validar(this.idOperador.equals(idOperador), "Solo el operador que tomo la ejecucion puede avanzar sus tareas");
        EstadoTareaEjecucion actual = tareas.get(idTarea);
        validar(actual != null, "La tarea no pertenece a esta ejecucion");
        if (!actual.puedePasarA(destino)) {
            throw new TransicionInvalidaException("Tarea " + idTarea, actual, destino);
        }
        tareas.put(idTarea, destino);
        version++;
    }

    private static void validar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ReglaNegocioException(mensaje);
        }
    }
}
