export interface Area {
  id: number;
  nombre: string;
  descripcion: string | null;
  fechaCreacion: string;
}

/** Cuerpo de alta y edicion (CreateAreaRequestVO / UpdateAreaRequestVO). */
export interface AreaRequest {
  nombre: string;
  descripcion: string | null;
}

export interface AreaFilters {
  nombre?: string;
}
