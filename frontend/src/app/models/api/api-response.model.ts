/**
 * Formas de respuesta del backend. Corresponden a SingleResponseVO y ResponseVO.
 */
export interface SingleResponse<T> {
  success: boolean;
  data: T;
}

export interface PageResponse<T> {
  success: boolean;
  data: T[];
  /** Pagina actual, empieza en 1. */
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Parametros comunes de los listados paginados. */
export interface PageQuery {
  page: number;
  size: number;
  orderBy?: string;
  orderType?: 'ASC' | 'DESC';
}
