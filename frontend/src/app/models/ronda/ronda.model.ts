import { Incidencia } from '../incidencia/incidencia.model';

/** Estados de EstadoEjecucion en el dominio. */
export type EstadoRonda = 'PROGRAMADA' | 'EN_CURSO' | 'COMPLETADA' | 'CANCELADA' | 'VENCIDA';
export type EstadoTarea = 'PENDIENTE' | 'EN_PROCESO' | 'COMPLETADA';

/** EjecucionResumenVO: una ronda en el listado. */
export interface RondaResumen {
  id: number;
  idRutina: number;
  rutina: string;
  idArea: number;
  area: string;
  frecuencia: string;
  fechaProgramada: string;
  estado: EstadoRonda;
  idOperador: number | null;
  operador: string | null;
  tareasTotal: number;
  tareasCompletadas: number;
  incidenciasAbiertas: number;
  version: number;
}

export interface TareaRonda {
  idTarea: number;
  nombre: string;
  orden: number;
  estado: EstadoTarea;
  fechaCompletada: string | null;
}

/** EjecucionResponseVO: detalle con tareas e incidencias. */
export interface Ronda extends RondaResumen {
  descripcionRutina: string | null;
  fechaInicio: string | null;
  fechaFin: string | null;
  tareas: TareaRonda[];
  incidencias: Incidencia[];
}

export interface RondaFiltros {
  fecha?: string;
  estado?: EstadoRonda;
  idArea?: number;
  soloMias?: boolean;
}
