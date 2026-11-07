import { AppConfig } from '../constants/app-config';
import { Rol, Session } from '../models/auth/session.model';

/** Sesion valida por una hora, como la que devuelve POST /auth/login. */
export function sesionDePrueba(perfil: Rol, id = 3): Session {
  return {
    token: 'token-de-prueba',
    expiraEn: new Date(Date.now() + 3_600_000).toISOString(),
    usuario: {
      id,
      nombre: 'Ana Torres',
      correo: 'ana@taskin.mx',
      perfil,
      areas: [{ id: 1, nombre: 'MANTENIMIENTO' }],
    },
  };
}

/** Deja la sesion en sessionStorage antes de crear AuthService, que la lee al iniciar. */
export function guardarSesion(session: Session): void {
  sessionStorage.setItem(AppConfig.storage.session, JSON.stringify(session));
}
