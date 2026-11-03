import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service';

/**
 * Agrega headers de trazabilidad a las peticiones hacia nuestra API: un id unico por
 * peticion para cruzarla con los logs del backend, la fecha del cliente, la app que llama
 * y el usuario de la sesion.
 */
export const headersInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }
  const auth = inject(AuthService);
  return next(
    req.clone({
      setHeaders: {
        'X-Client-Request-Id': crypto.randomUUID(),
        'X-Client-Date': new Date().toISOString(),
        'X-Client-App-Name': environment.appName,
        'X-Client-UserId': auth.session()?.correo ?? '',
      },
    }),
  );
};
