package mx.unam.buzz.rutinas.common.vo.evidencia;

/**
 * Foto recibida en multipart. El tipo real se detecta por su contenido, no por el nombre.
 */
public record SubirEvidenciaRequestVO(Long idEjecucion, byte[] contenido, String nota) {
}
