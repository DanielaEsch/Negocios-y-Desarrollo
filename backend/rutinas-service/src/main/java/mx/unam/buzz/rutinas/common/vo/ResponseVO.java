package mx.unam.buzz.rutinas.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Respuesta paginada para listados.
 * page empieza en 1 hacia afuera, igual que el parametro del controlador.
 */
@Getter
@Setter
@NoArgsConstructor
public class ResponseVO<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "true cuando la consulta se completo", example = "true")
    private Boolean success;

    @Schema(description = "Elementos de la pagina pedida")
    private List<T> data;

    @Schema(description = "Pagina devuelta; empieza en 1", example = "1")
    private int page;

    @Schema(description = "Tamano de pagina", example = "50")
    private int size;

    @Schema(description = "Total de elementos que cumplen el filtro", example = "1")
    private long totalElements;

    @Schema(description = "Total de paginas", example = "1")
    private int totalPages;

    public static <T> ResponseVO<T> of(Page<?> page, List<T> data) {
        ResponseVO<T> response = new ResponseVO<>();
        response.setSuccess(Boolean.TRUE);
        response.setData(data);
        response.setPage(page.getNumber() + 1);
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        return response;
    }
}
