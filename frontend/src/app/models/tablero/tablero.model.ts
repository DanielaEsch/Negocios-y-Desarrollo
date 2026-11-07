/** ConteoVO: rondas de un dia o de un area por estado. */
export interface Conteo {
  nombre: string | null;
  fecha: string;
  total: number;
  completadas: number;
  enCurso: number;
  programadas: number;
  vencidas: number;
  canceladas: number;
  /** De 0 a 100; nulo si aun no hay rondas decididas. */
  cumplimiento: number | null;
}

export interface Tablero {
  fecha: string;
  dia: Conteo;
  porArea: Conteo[];
  tendencia: Conteo[];
  incidenciasAbiertas: number;
}
