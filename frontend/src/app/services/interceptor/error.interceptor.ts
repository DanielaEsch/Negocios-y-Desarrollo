import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject, Injector } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ApiError, ProblemDetail } from '../../models/api/problem-detail.model';
import { isProblemDetail } from '../../utils/problem-detail.util';
import { AuthService } from '../auth/auth.service';
import { NotificationService } from '../notification/notification.service';

/**
 * Convierte cualquier error HTTP en un ApiError con su ProblemDetail (RFC 7807).
 *
 * - 400, 404 y 409 los resuelve la pagina (errores por campo, mensajes de negocio).
 * - 401 cierra la sesion.
 * - Sin conexion (status 0) y 5xx se avisan aqui con un snackbar, una sola vez.
 *
 * AuthService y NotificationService se piden hasta que hay un error: TranslateService
 * descarga sus JSON con HttpClient y, si se inyectaran al inicio, NotificationService
 * pediria a TranslateService mientras este aun se construye (dependencia circular).
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const injector = inject(Injector);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse)) {
        return throwError(() => error);
      }
      const problem = toProblemDetail(error);

      if (error.status === 401) {
        injector.get(AuthService).logout();
      } else if (error.status === 0) {
        injector.get(NotificationService).error('errors.offline');
      } else if (error.status >= 500) {
        injector.get(NotificationService).error('errors.server');
      }
      return throwError(() => new ApiError(problem));
    }),
  );
};

function toProblemDetail(error: HttpErrorResponse): ProblemDetail {
  if (isProblemDetail(error.error)) {
    return error.error;
  }
  return {
    type: 'about:blank',
    title: error.statusText || 'Error',
    status: error.status,
    detail: error.message,
    instance: error.url ?? undefined,
  };
}
