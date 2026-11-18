package mx.unam.buzz.rutinas.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import mx.unam.buzz.rutinas.common.constant.EvidenciaErrorCode;
import mx.unam.buzz.rutinas.common.exception.BusinessException;
import mx.unam.buzz.rutinas.common.vo.IdRequestVO;
import mx.unam.buzz.rutinas.common.vo.RequestVO;
import mx.unam.buzz.rutinas.common.vo.SingleResponseVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.ArchivoVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.EvidenciaVO;
import mx.unam.buzz.rutinas.common.vo.evidencia.SubirEvidenciaRequestVO;
import mx.unam.buzz.rutinas.facade.IEvidenciaFacade;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Fotos de evidencia de una ronda: lo que respalda que la tarea se hizo.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Evidencias", description = "Fotos JPEG, PNG o WEBP de hasta 5 MB; 6 por ronda")
public class EvidenciaController {

    private final IEvidenciaFacade evidenciaFacade;

    @PostMapping(path = "/ejecuciones/{id}/evidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Adjuntar foto", description = "Solo el operador que tomo la ronda, en curso o completada. "
            + "El tipo se valida por el contenido del archivo, no por su nombre.")
    @ApiResponse(responseCode = "413", description = "La foto pesa mas de 5 MB")
    public ResponseEntity<SingleResponseVO<EvidenciaVO>> adjuntar(
            @PathVariable Long id, @RequestPart("archivo") MultipartFile archivo,
            @RequestParam(required = false) @Size(max = 200, message = "{validacion.incidencia.largo}") String nota) {
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException ex) {
            throw new BusinessException(EvidenciaErrorCode.ARCHIVO_OBLIGATORIO.getCode(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evidenciaFacade.adjuntar(new RequestVO<>(new SubirEvidenciaRequestVO(id, contenido, nota))));
    }

    @GetMapping(path = "/ejecuciones/{id}/evidencias", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fotos de una ronda")
    public ResponseEntity<SingleResponseVO<List<EvidenciaVO>>> findByEjecucion(@PathVariable Long id) {
        return ResponseEntity.ok(evidenciaFacade.findByEjecucion(new RequestVO<>(new IdRequestVO(id))));
    }

    @GetMapping("/evidencias/{id}/archivo")
    @Operation(summary = "Descargar la foto", description = "Pide el token como cualquier otra ruta: el "
            + "navegador la trae con fetch y la muestra como blob.")
    public ResponseEntity<byte[]> archivo(@PathVariable Long id) {
        ArchivoVO archivo = evidenciaFacade.archivo(new RequestVO<>(new IdRequestVO(id)));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(archivo.tipoContenido()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .header("X-Content-Type-Options", "nosniff")
                .body(archivo.contenido());
    }
}
