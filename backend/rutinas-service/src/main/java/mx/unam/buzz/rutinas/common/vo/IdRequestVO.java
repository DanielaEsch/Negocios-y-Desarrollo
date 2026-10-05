package mx.unam.buzz.rutinas.common.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Peticion que solo lleva el identificador (detalle y borrado).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdRequestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
}
