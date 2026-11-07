package mx.unam.buzz.rutinas.web;

import mx.unam.buzz.rutinas.common.vo.RequestVO;

/**
 * Pasa los parametros de paginacion de la URL (page empieza en 1) a RequestVO
 * (page empieza en 0), igual que el listado de areas.
 */
final class Paginacion {

    private Paginacion() {
    }

    static <T> RequestVO<T> de(T filtros, int page, int size, String orderBy, String orderType) {
        RequestVO<T> request = new RequestVO<>(filtros);
        request.setPage(Math.max(page - 1, 0));
        request.setSize(size);
        request.setOrderBy(orderBy);
        request.setOrderType(orderType);
        return request;
    }
}
