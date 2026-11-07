export type EstadoIncidencia = 'PENDIENTE' | 'REVISION' | 'RESUELTA';

/**
 * IncidenciaResponseVO; para supervisor y administrador llega IncidenciaDetalleVO, con
 * reporta y revisor.
 */
export interface Incidencia {
  id: number;
  idEjecucion: number;
  rutina: string;
  idArea: number;
  area: string;
  descripcion: string;
  estado: EstadoIncidencia;
  fechaReporte: string;
  comentarioResolucion: string | null;
  fechaResolucion: string | null;
  idReporta?: number;
  reporta?: string;
  idRevisor?: number | null;
  revisor?: string | null;
}

export interface IncidenciaFiltros {
  estado?: EstadoIncidencia;
  idArea?: number;
}
