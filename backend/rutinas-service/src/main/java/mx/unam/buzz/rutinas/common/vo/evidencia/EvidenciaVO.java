package mx.unam.buzz.rutinas.common.vo.evidencia;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Metadatos de una foto de evidencia. El archivo se descarga aparte, con el token.
 */
public record EvidenciaVO(
        @Schema(example = "1") Long id,
        @Schema(example = "10") Long idEjecucion,
        @Schema(example = "image/jpeg") String tipoContenido,
        @Schema(example = "184233") long tamanoBytes,
        @Schema(example = "Bano 2 sin jabon") String nota,
        LocalDateTime fechaCaptura,
        @Schema(example = "Ana Torres") String subidaPor,
        @Schema(description = "Ruta relativa a /api/v1 para descargar la imagen", example = "/evidencias/1/archivo")
        String archivo) {
}
