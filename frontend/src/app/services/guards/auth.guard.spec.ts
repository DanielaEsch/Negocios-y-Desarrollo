import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  provideRouter,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { guardarSesion, sesionDePrueba } from '../../testing/sesion.testing';
import { authGuard, guestGuard, homeGuard, roleGuard } from './auth.guard';

describe('guards', () => {
  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/areas' } as RouterStateSnapshot;
  const run = (guard: typeof authGuard) => TestBed.runInInjectionContext(() => guard(route, state));

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient()] });
  });

  it('sin sesion manda al login con returnUrl', () => {
    const result = run(authGuard) as UrlTree;
    expect(result.toString()).toBe('/login?returnUrl=%2Fareas');
  });

  it('con sesion deja pasar y el login redirige al inicio del perfil', () => {
    guardarSesion(sesionDePrueba('OPERADOR'));
    expect(run(authGuard)).toBe(true);
    expect((run(guestGuard) as UrlTree).toString()).toBe('/turno');
    expect((run(homeGuard) as UrlTree).toString()).toBe('/turno');
  });

  it('roleGuard bloquea a un perfil sin permiso', () => {
    guardarSesion(sesionDePrueba('SUPERVISOR'));
    expect((run(roleGuard('ADMINISTRADOR')) as UrlTree).toString()).toBe('/tablero');
    expect(run(roleGuard('SUPERVISOR', 'ADMINISTRADOR'))).toBe(true);
  });
});
