import { environment } from './environment';

/**
 * Rutas de la API agrupadas por modulo. Los servicios nunca arman URLs a mano.
 */
export const Endpoints = {
  areas: `${environment.apiUrl}/areas`,
} as const;
