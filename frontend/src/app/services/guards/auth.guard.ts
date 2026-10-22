import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Rol } from '../../models/auth/session.model';
import { AuthService } from '../auth/auth.service';

/** Rutas privadas: sin sesion manda al login y recuerda a donde iba. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  if (auth.isAuthenticated()) {
    return true;
  }
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/** El login solo se ve sin sesion. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isAuthenticated() ? inject(Router).createUrlTree(['/inicio']) : true;
};

/** Restringe una ruta a ciertos perfiles: `canActivate: [roleGuard('ADMINISTRADOR')]`. */
export function roleGuard(...roles: Rol[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    return auth.hasRole(...roles) ? true : inject(Router).createUrlTree(['/inicio']);
  };
}
