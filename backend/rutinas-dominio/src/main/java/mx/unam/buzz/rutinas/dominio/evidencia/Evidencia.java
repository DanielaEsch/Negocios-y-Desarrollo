package mx.unam.buzz.rutinas.dominio.evidencia;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.ejecucion.EstadoEjecucion;

import java.time.LocalDateTime;

/**
 * Foto que prueba que una ronda se hizo (lo que antes se mandaba por WhatsApp).
 *
 * Reglas: solo la sube el operador que tomo la ronda, mientras esta en curso o recien
 * completada; solo imagenes JPEG, PNG o WEBP de hasta 5 MB, y el tipo se reconoce por
 * los primeros bytes del archivo, no por lo que diga el navegador.
 */
public final class Evidencia {

    public static final long TAMANO_MAXIMO_BYTES = 5L * 1024 * 1024;
    public static final int MAXIMO_POR_RONDA = 6;

    private final Long idEjecucion;
    private final Long idUsuario;
    private final TipoImagen tipo;
    private final long tamanoBytes;
    private final String nota;
    private final LocalDateTime fechaCaptura;

    private Evidencia(Long idEjecucion, Long idUsuario, TipoImagen tipo, long tamanoBytes, String nota,
                      LocalDateTime fechaCaptura) {
        this.idEjecucion = idEjecucion;
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.tamanoBytes = tamanoBytes;
        this.nota = nota;
        this.fechaCaptura = fechaCaptura;
    }

    /**
     * @param estadoRonda      estado actual de la ronda
     * @param idOperadorRonda  quien tomo la ronda
     * @param yaAdjuntas       cuantas evidencias tiene ya la ronda
     */
    public static Evidencia adjuntar(Long idEjecucion, EstadoEjecucion estadoRonda, Long idOperadorRonda,
                                     Long idUsuario, byte[] contenido, String nota, int yaAdjuntas,
                                     LocalDateTime ahora) {
        validar(idEjecucion != null && idEjecucion > 0, "La ronda no es valida");
        validar(estadoRonda == EstadoEjecucion.EN_CURSO || estadoRonda == EstadoEjecucion.COMPLETADA,
                "Solo se adjuntan evidencias a una ronda en curso o completada");
        validar(idUsuario != null && idUsuario.equals(idOperadorRonda),
                "Solo el operador que tomo la ronda adjunta evidencias");
        validar(contenido != null && contenido.length > 0, "El archivo esta vacio");
        validar(contenido.length <= TAMANO_MAXIMO_BYTES, "La foto pesa mas de 5 MB");
        validar(yaAdjuntas < MAXIMO_POR_RONDA, "Una ronda admite como maximo " + MAXIMO_POR_RONDA + " fotos");
        TipoImagen tipo = TipoImagen.detectar(contenido)
                .orElseThrow(() -> new ReglaNegocioException("Solo se aceptan fotos JPEG, PNG o WEBP"));
        String limpia = nota == null || nota.isBlank() ? null : nota.trim();
        return new Evidencia(idEjecucion, idUsuario, tipo, contenido.length, limpia, ahora);
    }

    public Long getIdEjecucion() {
        return idEjecucion;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public TipoImagen getTipo() {
        return tipo;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public String getNota() {
        return nota;
    }

    public LocalDateTime getFechaCaptura() {
        return fechaCaptura;
    }

    private static void validar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ReglaNegocioException(mensaje);
        }
    }
}
