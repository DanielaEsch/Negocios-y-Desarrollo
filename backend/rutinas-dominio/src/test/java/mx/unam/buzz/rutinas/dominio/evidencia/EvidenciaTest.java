package mx.unam.buzz.rutinas.dominio.evidencia;

import mx.unam.buzz.rutinas.dominio.comun.ReglaNegocioException;
import mx.unam.buzz.rutinas.dominio.ejecucion.EstadoEjecucion;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Reglas de las fotos de evidencia.
 */
class EvidenciaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 15, 10, 0);
    private static final Long OPERADOR = 7L;

    static byte[] jpeg(int tamano) {
        byte[] bytes = new byte[tamano];
        bytes[0] = (byte) 0xFF;
        bytes[1] = (byte) 0xD8;
        bytes[2] = (byte) 0xFF;
        return bytes;
    }

    @Test
    void elOperadorDeLaRondaAdjuntaUnaFoto() {
        Evidencia evidencia = Evidencia.adjuntar(10L, EstadoEjecucion.EN_CURSO, OPERADOR, OPERADOR, jpeg(2048),
                "  ", 0, AHORA);

        assertEquals(TipoImagen.JPEG, evidencia.getTipo());
        assertEquals(2048, evidencia.getTamanoBytes());
        assertNull(evidencia.getNota());
    }

    @Test
    void reconocePngYWebpPorSuFirma() {
        byte[] png = new byte[20];
        png[0] = (byte) 0x89;
        png[1] = 'P';
        png[2] = 'N';
        png[3] = 'G';
        byte[] webp = Arrays.copyOf("RIFF0000WEBPVP8 ".getBytes(StandardCharsets.US_ASCII), 20);

        assertEquals(TipoImagen.PNG, TipoImagen.detectar(png).orElseThrow());
        assertEquals(TipoImagen.WEBP, TipoImagen.detectar(webp).orElseThrow());
    }

    @Test
    void unArchivoQueNoEsImagenSeRechazaAunqueSeLlameFoto() {
        byte[] pdf = Arrays.copyOf("%PDF-1.7 hola mundo".getBytes(StandardCharsets.US_ASCII), 30);

        assertThrows(ReglaNegocioException.class, () -> Evidencia.adjuntar(10L, EstadoEjecucion.EN_CURSO,
                OPERADOR, OPERADOR, pdf, null, 0, AHORA));
    }

    @Test
    void otroUsuarioNoAdjuntaEnUnaRondaAjena() {
        assertThrows(ReglaNegocioException.class, () -> Evidencia.adjuntar(10L, EstadoEjecucion.EN_CURSO,
                OPERADOR, 99L, jpeg(100), null, 0, AHORA));
    }

    @Test
    void soloEnRondasEnCursoOCompletadas() {
        assertThrows(ReglaNegocioException.class, () -> Evidencia.adjuntar(10L, EstadoEjecucion.PROGRAMADA,
                OPERADOR, OPERADOR, jpeg(100), null, 0, AHORA));
        assertThrows(ReglaNegocioException.class, () -> Evidencia.adjuntar(10L, EstadoEjecucion.VENCIDA,
                OPERADOR, OPERADOR, jpeg(100), null, 0, AHORA));
        Evidencia.adjuntar(10L, EstadoEjecucion.COMPLETADA, OPERADOR, OPERADOR, jpeg(100), "Bano 2", 0, AHORA);
    }

    @Test
    void respetaElTamanoYElMaximoPorRonda() {
        assertThrows(ReglaNegocioException.class, () -> Evidencia.adjuntar(10L, EstadoEjecucion.EN_CURSO,
                OPERADOR, OPERADOR, jpeg((int) Evidencia.TAMANO_MAXIMO_BYTES + 1), null, 0, AHORA));
        assertThrows(ReglaNegocioException.class, () -> Evidencia.adjuntar(10L, EstadoEjecucion.EN_CURSO,
                OPERADOR, OPERADOR, jpeg(100), null, Evidencia.MAXIMO_POR_RONDA, AHORA));
    }
}
