import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  provideRouter,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { authGuard, guestGuard, roleGuard } from './auth.guard';

describe('guards', () => {
  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/areas' } as RouterStateSnapshot;
  const run = (guard: typeof authGuard) => TestBed.runInInjectionContext(() => guard(route, state));

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('sin sesion manda al login con returnUrl', () => {
    const result = run(authGuard) as UrlTree;
    expect(result.toString()).toBe('/login?returnUrl=%2Fareas');
  });

  it('con sesion deja pasar y el login redirige al inicio', async () => {
    await firstValueFrom(
      TestBed.inject(AuthService).login({
        correo: 'a@b.mx',
        contrasena: '12345678',
        rol: 'OPERADOR',
      }),
    );
    expect(run(authGuard)).toBe(true);
    expect((run(guestGuard) as UrlTree).toString()).toBe('/inicio');
  });

  it('roleGuard bloquea a un perfil sin permiso', async () => {
    await firstValueFrom(
      TestBed.inject(AuthService).login({
        correo: 'a@b.mx',
        contrasena: '12345678',
        rol: 'OPERADOR',
      }),
    );
    expect((run(roleGuard('ADMINISTRADOR')) as UrlTree).toString()).toBe('/inicio');
    expect(run(roleGuard('OPERADOR', 'ADMINISTRADOR'))).toBe(true);
  });
});
