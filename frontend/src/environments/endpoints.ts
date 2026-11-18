import { environment } from './environment';

/**
 * Rutas de la API agrupadas por modulo. Los servicios nunca arman URLs a mano.
 */
export const Endpoints = {
  login: `${environment.apiUrl}/auth/login`,
  areas: `${environment.apiUrl}/areas`,
  usuarios: `${environment.apiUrl}/usuarios`,
  rutinas: `${environment.apiUrl}/rutinas`,
  ejecuciones: `${environment.apiUrl}/ejecuciones`,
  incidencias: `${environment.apiUrl}/incidencias`,
  tablero: `${environment.apiUrl}/tablero`,
  evidencias: `${environment.apiUrl}/evidencias`,
  pagos: `${environment.apiUrl}/pagos`,
} as const;
