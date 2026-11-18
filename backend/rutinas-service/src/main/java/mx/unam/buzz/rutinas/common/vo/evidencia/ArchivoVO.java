package mx.unam.buzz.rutinas.common.vo.evidencia;

/**
 * Contenido binario con su tipo, para responder la descarga.
 */
public record ArchivoVO(byte[] contenido, String tipoContenido) {
}
