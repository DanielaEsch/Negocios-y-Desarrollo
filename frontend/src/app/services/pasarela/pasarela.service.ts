import { HttpBackend, HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DatosTarjeta, TokenTarjeta } from '../../models/pago/pago.model';

/** Error con el formato de la pasarela: { error: { tipo, codigo, mensaje } }. */
export class PasarelaError extends Error {
  constructor(
    readonly codigo: string,
    mensaje: string,
  ) {
    super(mensaje);
    this.name = 'PasarelaError';
  }
}

/**
 * SDK minimo de la pasarela, como el que un proveedor real da para el navegador.
 * Usa HttpBackend para saltarse los interceptores de TaskIn: a la pasarela no le llega
 * nuestro token de sesion, y a TaskIn no le llega la tarjeta. Solo la llave publica.
 */
@Injectable({ providedIn: 'root' })
export class PasarelaService {
  private readonly http = new HttpClient(inject(HttpBackend));

  tokenizar(tarjeta: DatosTarjeta): Observable<TokenTarjeta> {
    const headers = new HttpHeaders({
      Authorization: `Bearer ${environment.pasarela.llavePublica}`,
      'Content-Type': 'application/json',
    });
    return this.http
      .post<TokenTarjeta>(`${environment.pasarela.url}/tokens`, tarjeta, { headers })
      .pipe(
        catchError((error: HttpErrorResponse) => {
          const detalle = error.error?.error;
          return throwError(
            () =>
              new PasarelaError(
                detalle?.codigo ?? 'sin_conexion',
                detalle?.mensaje ?? 'No se pudo contactar a la pasarela',
              ),
          );
        }),
      );
  }
}
