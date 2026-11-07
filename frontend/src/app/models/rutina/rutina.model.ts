export type Frecuencia = 'CADA_2_HORAS' | 'DIARIA' | 'SEMANAL' | 'POR_EVENTO';

export interface Tarea {
  id: number;
  nombre: string;
  orden: number;
}

/** RutinaResponseVO. Las horas llegan como "08:00" o "08:00:00". */
export interface Rutina {
  id: number;
  idArea: number;
  area: string;
  nombre: string;
  descripcion: string | null;
  frecuencia: Frecuencia;
  horaInicio: string | null;
  horaFin: string | null;
  diaSemana: number | null;
  tareas: Tarea[];
}

/** SaveRutinaRequestVO: las tareas van en orden, como texto. */
export interface RutinaRequest {
  idArea: number;
  nombre: string;
  descripcion: string | null;
  frecuencia: Frecuencia;
  horaInicio: string | null;
  horaFin: string | null;
  diaSemana: number | null;
  tareas: string[];
}
