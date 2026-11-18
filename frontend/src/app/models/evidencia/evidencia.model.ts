/** EvidenciaVO del backend: metadatos de una foto; la imagen se descarga aparte. */
export interface Evidencia {
  id: number;
  idEjecucion: number;
  tipoContenido: string;
  tamanoBytes: number;
  nota: string | null;
  fechaCaptura: string;
  subidaPor: string | null;
  archivo: string;
}

/** Mismos limites que el dominio (Evidencia.java). */
export const EVIDENCIA_MAX_BYTES = 5 * 1024 * 1024;
export const EVIDENCIA_MAX_POR_RONDA = 6;
