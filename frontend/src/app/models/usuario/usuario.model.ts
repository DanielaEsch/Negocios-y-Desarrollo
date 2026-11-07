import { Usuario } from '../auth/session.model';

/** UsuarioAdminVO: lo que ve el administrador. */
export interface UsuarioAdmin extends Usuario {
  fechaConsentimiento: string | null;
  fechaCreacion: string | null;
}

export interface UsuarioRequest {
  nombre: string;
  correo: string;
  contrasena: string;
  aceptaAvisoPrivacidad: boolean;
  areas: number[];
}
