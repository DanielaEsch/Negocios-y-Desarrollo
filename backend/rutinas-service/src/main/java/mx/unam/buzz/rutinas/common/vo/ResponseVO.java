package mx.unam.buzz.rutinas.common.vo;

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

    private Boolean success;
    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
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
