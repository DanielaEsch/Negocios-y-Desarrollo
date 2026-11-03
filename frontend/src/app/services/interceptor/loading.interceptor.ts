import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';
import { LoadingService } from '../loading/loading.service';

/** Enciende la barra de carga durante cada peticion, salvo los archivos de traduccion. */
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.startsWith('/i18n/')) {
    return next(req);
  }
  const loading = inject(LoadingService);
  loading.start();
  return next(req).pipe(finalize(() => loading.stop()));
};
