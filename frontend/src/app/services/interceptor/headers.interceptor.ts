import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';

/**
 * Agrega a las peticiones hacia nuestra API el token de sesion y los headers de
 * trazabilidad: un id unico por peticion para cruzarla con los logs del backend, la fecha
 * del cliente, la app que llama y el usuario de la sesion.
 */
export const headersInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }
  const session = inject(AuthService).session();
  const headers: Record<string, string> = {
    'X-Client-Request-Id': crypto.randomUUID(),
    'X-Client-Date': new Date().toISOString(),
    'X-Client-App-Name': environment.appName,
    'X-Client-UserId': session?.usuario.correo ?? '',
  };
  if (session) {
    headers['Authorization'] = `Bearer ${session.token}`;
  }
  return next(req.clone({ setHeaders: headers }));
};
