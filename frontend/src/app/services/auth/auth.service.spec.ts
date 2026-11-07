import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Endpoints } from '../../../environments/endpoints';
import { AppConfig } from '../../constants/app-config';
import { guardarSesion, sesionDePrueba } from '../../testing/sesion.testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let backend: HttpTestingController;

  const configurar = () => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    backend = TestBed.inject(HttpTestingController);
    return TestBed.inject(AuthService);
  };

  beforeEach(() => sessionStorage.clear());
  afterEach(() => backend.verify());

  it('inicia sesion contra /auth/login y guarda el token', async () => {
    const service = configurar();
    const respuesta = sesionDePrueba('SUPERVISOR');

    const login = firstValueFrom(
      service.login({ correo: ' Ana@TaskIn.mx ', contrasena: 'TaskIn2026' }),
    );
    const request = backend.expectOne(Endpoints.login);
    expect(request.request.body).toEqual({ correo: 'ana@taskin.mx', contrasena: 'TaskIn2026' });
    request.flush({ success: true, data: respuesta });

    expect(await login).toEqual(respuesta);
    expect(service.isAuthenticated()).toBe(true);
    expect(service.hasRole('SUPERVISOR', 'ADMINISTRADOR')).toBe(true);
    expect(service.hasRole('ADMINISTRADOR')).toBe(false);
    expect(service.home()).toBe('/tablero');
    expect(sessionStorage.getItem(AppConfig.storage.session)).toContain('token-de-prueba');
  });

  it('manda al operador a su turno', () => {
    guardarSesion(sesionDePrueba('OPERADOR'));
    expect(configurar().home()).toBe('/turno');
  });

  it('descarta una sesion vencida', () => {
    guardarSesion({ ...sesionDePrueba('OPERADOR'), expiraEn: '2020-01-01T00:00:00Z' });
    expect(configurar().isAuthenticated()).toBe(false);
  });

  it('cierra la sesion y regresa al login', () => {
    guardarSesion(sesionDePrueba('OPERADOR'));
    const service = configurar();
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    service.logout();

    expect(service.session()).toBeNull();
    expect(sessionStorage.getItem(AppConfig.storage.session)).toBeNull();
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });
});
