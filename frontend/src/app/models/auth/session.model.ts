/** Perfiles del sistema, los mismos del ADR y de la capa de dominio. */
export type Rol = 'OPERADOR' | 'SUPERVISOR' | 'ADMINISTRADOR';

export interface AreaResumen {
  id: number;
  nombre: string;
}

/** UsuarioResponseVO del backend. */
export interface Usuario {
  id: number;
  nombre: string;
  correo: string;
  perfil: Rol;
  areas: AreaResumen[];
}

/** Lo que se guarda al iniciar sesion: el token Bearer y quien es. */
export interface Session {
  token: string;
  expiraEn: string;
  usuario: Usuario;
}

/** AuthTokenVO del backend. */
export interface AuthToken {
  token: string;
  expiraEn: string;
  usuario: Usuario;
}

export interface LoginRequest {
  correo: string;
  contrasena: string;
}
