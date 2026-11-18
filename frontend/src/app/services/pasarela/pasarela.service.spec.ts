import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { guardarSesion, sesionDePrueba } from '../../testing/sesion.testing';
import { headersInterceptor } from '../interceptor/headers.interceptor';
import { PagoService } from '../pago/pago.service';
import { PasarelaError, PasarelaService } from './pasarela.service';

/**
 * PCI-DSS en el navegador: la tarjeta va solo a la pasarela, con su llave publica y sin
 * nuestro token; a TaskIn solo le llega el token de la tarjeta.
 */
describe('PasarelaService y PagoService', () => {
  let backend: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    guardarSesion(sesionDePrueba('ADMINISTRADOR'));
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([headersInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('tokeniza con la llave publica y sin el token de sesion de TaskIn', () => {
    let token = '';
    TestBed.inject(PasarelaService)
      .tokenizar({
        numero: '4242424242424242',
        mesExpiracion: 12,
        anioExpiracion: 2030,
        cvc: '123',
        titular: 'Laura',
      })
      .subscribe((t) => (token = t.id));

    const req = backend.expectOne('/api/v1/sandbox/pasarela/tokens');
    expect(req.request.headers.get('Authorization')).toBe('Bearer pk_test_taskin_sandbox');
    expect(req.request.headers.has('X-Client-UserId')).toBe(false);
    req.flush({ id: 'tok_abc', tarjeta: { marca: 'VISA', ultimos4: '4242' } });
    expect(token).toBe('tok_abc');
  });

  it('traduce el error de la pasarela', () => {
    let error: unknown;
    TestBed.inject(PasarelaService)
      .tokenizar({ numero: '1', mesExpiracion: 1, anioExpiracion: 2020, cvc: '1', titular: 'x' })
      .subscribe({ error: (e: unknown) => (error = e) });

    backend
      .expectOne('/api/v1/sandbox/pasarela/tokens')
      .flush(
        {
          error: {
            tipo: 'error_tarjeta',
            codigo: 'numero_invalido',
            mensaje: 'El numero no es valido',
          },
        },
        { status: 402, statusText: 'Payment Required' },
      );
    expect(error).toBeInstanceOf(PasarelaError);
    expect((error as PasarelaError).codigo).toBe('numero_invalido');
  });

  it('a TaskIn solo le manda el token, con la sesion del administrador', () => {
    TestBed.inject(PagoService).cobrar(7, 'tok_abc').subscribe();

    const req = backend.expectOne('/api/v1/pagos/ordenes/7/cobros');
    expect(req.request.body).toEqual({ token: 'tok_abc' });
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-de-prueba');
    req.flush({ success: true, data: {} });
  });
});
