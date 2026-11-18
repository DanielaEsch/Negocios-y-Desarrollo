package mx.unam.buzz.rutinas.dominio.evidencia;

import java.util.Optional;

/**
 * Formatos de foto aceptados, reconocidos por su firma (los primeros bytes del archivo).
 * Asi un archivo renombrado a .jpg no pasa por foto.
 */
public enum TipoImagen {

    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    private final String tipoContenido;
    private final String extension;

    TipoImagen(String tipoContenido, String extension) {
        this.tipoContenido = tipoContenido;
        this.extension = extension;
    }

    public static Optional<TipoImagen> detectar(byte[] b) {
        if (b == null || b.length < 12) {
            return Optional.empty();
        }
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return Optional.of(JPEG);
        }
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return Optional.of(PNG);
        }
        if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public String getExtension() {
        return extension;
    }
}
