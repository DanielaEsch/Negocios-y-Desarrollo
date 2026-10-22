import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AppConfig } from '../../constants/app-config';
import { AuthService } from './auth.service';

describe('AuthService (sesion simulada)', () => {
  let service: AuthService;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    service = TestBed.inject(AuthService);
  });

  it('abre la sesion con el rol elegido y la guarda', async () => {
    const session = await firstValueFrom(
      service.login({ correo: 'Ana.Lopez@Correo.mx', contrasena: '12345678', rol: 'SUPERVISOR' }),
    );

    expect(session).toEqual({
      nombre: 'Ana Lopez',
      correo: 'ana.lopez@correo.mx',
      rol: 'SUPERVISOR',
    });
    expect(service.isAuthenticated()).toBe(true);
    expect(service.hasRole('SUPERVISOR', 'ADMINISTRADOR')).toBe(true);
    expect(service.hasRole('ADMINISTRADOR')).toBe(false);
    expect(sessionStorage.getItem(AppConfig.storage.session)).toContain('SUPERVISOR');
  });

  it('cierra la sesion y regresa al login', async () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    await firstValueFrom(
      service.login({ correo: 'a@b.mx', contrasena: '12345678', rol: 'OPERADOR' }),
    );

    service.logout();

    expect(service.session()).toBeNull();
    expect(sessionStorage.getItem(AppConfig.storage.session)).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });
});
