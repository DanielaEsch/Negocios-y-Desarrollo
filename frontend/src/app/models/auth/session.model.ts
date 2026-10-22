/** Perfiles del sistema, los mismos del ADR y de la capa de dominio. */
export type Rol = 'OPERADOR' | 'SUPERVISOR' | 'ADMINISTRADOR';

export interface Session {
  nombre: string;
  correo: string;
  rol: Rol;
}

export interface LoginRequest {
  correo: string;
  contrasena: string;
  rol: Rol;
}
