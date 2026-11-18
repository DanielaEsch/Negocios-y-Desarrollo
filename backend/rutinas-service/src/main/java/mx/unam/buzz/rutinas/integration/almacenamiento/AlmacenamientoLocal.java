package mx.unam.buzz.rutinas.integration.almacenamiento;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Almacenamiento en disco: {directorio}/yyyy/MM/uuid.ext. El nombre lo pone el servidor,
 * nunca el usuario, y se valida que la llave no salga del directorio.
 */
@Slf4j
@Component
public class AlmacenamientoLocal implements IAlmacenamientoArchivos {

    private static final DateTimeFormatter CARPETA = DateTimeFormatter.ofPattern("yyyy/MM");

    private final Path raiz;

    public AlmacenamientoLocal(@Value("${taskin.evidencias.directorio}") String directorio) {
        this.raiz = Path.of(directorio).toAbsolutePath().normalize();
    }

    @Override
    public String guardar(byte[] contenido, String extension) {
        String llave = LocalDate.now().format(CARPETA) + "/" + UUID.randomUUID() + "." + extension;
        Path destino = resolver(llave);
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido);
            return llave;
        } catch (IOException ex) {
            log.error("No se pudo guardar {}: {}", llave, ex.getMessage());
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public byte[] leer(String llave) {
        try {
            return Files.readAllBytes(resolver(llave));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Path resolver(String llave) {
        Path ruta = raiz.resolve(llave).normalize();
        if (!ruta.startsWith(raiz)) {
            throw new IllegalArgumentException("Llave fuera del almacenamiento");
        }
        return ruta;
    }
}
