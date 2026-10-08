package mx.unam.buzz.rutinas.dominio.incidencia;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.comun.TransicionInvalidaException;
import mx.unam.buzz.rutinas.dominio.usuario.Perfil;

import java.time.LocalDateTime;

/**
 * Algo que salio mal durante una ejecucion y alguien tiene que atender
 * (por ejemplo, "falta jabon en el bano 2").
 *
 * Reglas: cualquier usuario reporta, solo un SUPERVISOR o ADMINISTRADOR la
 * revisa, y no se puede cerrar sin dejar por escrito que se hizo.
 */
public class Incidencia {

    private final Long idEjecucion;
    private final Long idReporta;
    private final String descripcion;
    private final LocalDateTime fechaReporte;
    private EstadoIncidencia estado;
    private Long idRevisor;
    private String comentarioResolucion;
    private LocalDateTime fechaResolucion;

    private Incidencia(Long idEjecucion, Long idReporta, String descripcion) {
        this.idEjecucion = idEjecucion;
        this.idReporta = idReporta;
        this.descripcion = descripcion;
        this.fechaReporte = LocalDateTime.now();
    }

    public static Incidencia reportar(Long idEjecucion, Long idReporta, String descripcion) {
        validar(idEjecucion != null && idEjecucion > 0, "La ejecucion no es valida");
        validar(idReporta != null && idReporta > 0, "El usuario que reporta no es valido");
        validar(descripcion != null && !descripcion.isBlank(), "La descripcion es obligatoria");

        Incidencia incidencia = new Incidencia(idEjecucion, idReporta, descripcion.trim());
        incidencia.estado = EstadoIncidencia.PENDIENTE;
        return incidencia;
    }

    public void tomarEnRevision(Long idRevisor, Perfil perfilRevisor) {
        validar(perfilRevisor == Perfil.SUPERVISOR || perfilRevisor == Perfil.ADMINISTRADOR,
                "Solo un SUPERVISOR o ADMINISTRADOR puede revisar incidencias");
        validar(idRevisor != null && idRevisor > 0, "El revisor no es valido");
        cambiarEstado(EstadoIncidencia.REVISION);
        this.idRevisor = idRevisor;
    }

    public void resolver(String comentario) {
        validar(comentario != null && !comentario.isBlank(),
                "Para resolver una incidencia hay que explicar que se hizo");
        cambiarEstado(EstadoIncidencia.RESUELTA);
        this.comentarioResolucion = comentario.trim();
        this.fechaResolucion = LocalDateTime.now();
    }

    public Long getIdEjecucion() {
        return idEjecucion;
    }

    public Long getIdReporta() {
        return idReporta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaReporte() {
        return fechaReporte;
    }

    public EstadoIncidencia getEstado() {
        return estado;
    }

    public Long getIdRevisor() {
        return idRevisor;
    }

    public String getComentarioResolucion() {
        return comentarioResolucion;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    private void cambiarEstado(EstadoIncidencia destino) {
        if (!estado.puedePasarA(destino)) {
            throw new TransicionInvalidaException("Incidencia", estado, destino);
        }
        estado = destino;
    }

    private static void validar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ReglaNegocioException(mensaje);
        }
    }
}
