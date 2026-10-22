import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter } from '@angular/router';
import { provideTranslateService, TranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { ApiError } from '../../models/api/problem-detail.model';
import { AuthService } from '../auth/auth.service';
import { LoadingService } from '../loading/loading.service';
import { errorInterceptor } from './error.interceptor';
import { headersInterceptor } from './headers.interceptor';
import { loadingInterceptor } from './loading.interceptor';

describe('interceptores', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let snackOpen: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    snackOpen = vi.fn();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideTranslateService(),
        { provide: MatSnackBar, useValue: { open: snackOpen } },
        provideHttpClient(
          withInterceptors([headersInterceptor, loadingInterceptor, errorInterceptor]),
        ),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('agrega los headers de trazabilidad solo hacia la API', () => {
    http.get('/api/v1/areas').subscribe();
    http.get('/i18n/es.json').subscribe();

    const api = backend.expectOne('/api/v1/areas');
    expect(api.request.headers.get('X-Client-Request-Id')).toMatch(/^[0-9a-f-]{36}$/);
    expect(api.request.headers.get('X-Client-App-Name')).toBe('TaskIn');
    api.flush({});

    const asset = backend.expectOne('/i18n/es.json');
    expect(asset.request.headers.has('X-Client-Request-Id')).toBe(false);
    asset.flush({});
  });

  it('enciende la barra de carga mientras la peticion esta en curso', () => {
    const loading = TestBed.inject(LoadingService);
    http.get('/api/v1/areas').subscribe();
    expect(loading.isLoading()).toBe(true);

    backend.expectOne('/api/v1/areas').flush({});
    expect(loading.isLoading()).toBe(false);
  });

  it('convierte el problem+json en ApiError', () => {
    let received: unknown;
    http.post('/api/v1/areas', {}).subscribe({ error: (e: unknown) => (received = e) });

    backend.expectOne('/api/v1/areas').flush(
      {
        type: 'urn:buzz-rutinas:problema:validacion',
        title: 'Error de Validación en los Datos de Entrada',
        status: 400,
        detail: 'Uno o más campos del payload no cumplen con las reglas establecidas.',
        invalid_params: [{ name: 'nombre', reason: 'El nombre es obligatorio.' }],
      },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(received).toBeInstanceOf(ApiError);
    const error = received as ApiError;
    expect(error.status).toBe(400);
    expect(error.problem.invalid_params?.[0].name).toBe('nombre');
    expect(snackOpen).not.toHaveBeenCalled();
  });

  it('avisa los errores del servidor y cierra la sesion en 401', () => {
    const logout = vi
      .spyOn(TestBed.inject(AuthService), 'logout')
      .mockImplementation(() => undefined);

    http.get('/api/v1/areas').subscribe({ error: () => undefined });
    backend.expectOne('/api/v1/areas').flush(null, { status: 500, statusText: 'Server Error' });
    expect(snackOpen).toHaveBeenCalledTimes(1);

    http.get('/api/v1/areas').subscribe({ error: () => undefined });
    backend.expectOne('/api/v1/areas').flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(logout).toHaveBeenCalled();
  });
});

describe('interceptores con ngx-translate', () => {
  it('no forman un ciclo con TranslateService al descargar las traducciones', () => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideTranslateService({
          loader: provideTranslateHttpLoader({ prefix: '/i18n/', suffix: '.json' }),
          lang: 'es',
        }),
        provideHttpClient(
          withInterceptors([headersInterceptor, loadingInterceptor, errorInterceptor]),
        ),
        provideHttpClientTesting(),
      ],
    });
    const translate = TestBed.inject(TranslateService);
    const backend = TestBed.inject(HttpTestingController);

    backend.expectOne('/i18n/es.json').flush({ hola: 'Hola' });

    expect(translate.instant('hola')).toBe('Hola');
    backend.verify();
  });
});
