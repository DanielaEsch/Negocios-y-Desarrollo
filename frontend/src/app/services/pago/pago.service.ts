import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable, tap } from 'rxjs';
import { Endpoints } from '../../../environments/endpoints';
import { PageResponse, SingleResponse } from '../../models/api/api-response.model';
import {
  Cotizacion,
  CotizacionRequest,
  EstadoOrden,
  MetodoPago,
  OrdenPago,
  OrdenRequest,
  Plan,
  Suscripcion,
} from '../../models/pago/pago.model';
import { ConsumeService } from '../consume/consume.service';

/** Acceso a /api/v1/pagos. */
@Injectable({ providedIn: 'root' })
export class PagoService {
  private readonly consume = inject(ConsumeService);
  private readonly http = inject(HttpClient);
  private readonly url = Endpoints.pagos;
  private readonly actual = signal<Suscripcion | null>(null);

  /** Ultima suscripcion consultada: la usa el aviso de prueba o vencida del layout. */
  readonly suscripcionActual = this.actual.asReadonly();

  planes(): Observable<Plan[]> {
    return this.consume
      .httpGet<SingleResponse<Plan[]>>(`${this.url}/planes`)
      .pipe(map((r) => r.data));
  }

  suscripcion(): Observable<Suscripcion> {
    return this.consume.httpGet<SingleResponse<Suscripcion>>(`${this.url}/suscripcion`).pipe(
      map((r) => r.data),
      tap((s) => this.actual.set(s)),
    );
  }

  cotizar(request: CotizacionRequest): Observable<Cotizacion> {
    return this.consume
      .httpPost<SingleResponse<Cotizacion>>(`${this.url}/cotizaciones`, request)
      .pipe(map((r) => r.data));
  }

  crearOrden(request: OrdenRequest): Observable<OrdenPago> {
    return this.consume
      .httpPost<SingleResponse<OrdenPago>>(`${this.url}/ordenes`, request)
      .pipe(map((r) => r.data));
  }

  ordenes(
    filtros: { estado?: EstadoOrden; metodo?: MetodoPago } = {},
    size = 50,
  ): Observable<OrdenPago[]> {
    return this.consume
      .httpGet<PageResponse<OrdenPago>>(`${this.url}/ordenes`, { ...filtros, size })
      .pipe(map((r) => r.data));
  }

  orden(id: number): Observable<OrdenPago> {
    return this.consume
      .httpGet<SingleResponse<OrdenPago>>(`${this.url}/ordenes/${id}`)
      .pipe(map((r) => r.data));
  }

  /** Solo el token: el numero de la tarjeta nunca llega a TaskIn. */
  cobrar(id: number, token: string): Observable<OrdenPago> {
    return this.consume
      .httpPost<SingleResponse<OrdenPago>>(`${this.url}/ordenes/${id}/cobros`, { token })
      .pipe(map((r) => r.data));
  }

  reportarTransferencia(id: number, claveRastreo: string): Observable<OrdenPago> {
    return this.consume
      .httpPost<SingleResponse<OrdenPago>>(`${this.url}/ordenes/${id}/transferencias`, {
        claveRastreo,
      })
      .pipe(map((r) => r.data));
  }

  /** Cobranza: PAGADO o RECHAZADO (con motivo). */
  validar(id: number, estado: 'PAGADO' | 'RECHAZADO', motivo?: string): Observable<OrdenPago> {
    return this.consume
      .httpPatch<SingleResponse<OrdenPago>>(`${this.url}/ordenes/${id}`, {
        estado,
        motivo: motivo ?? null,
      })
      .pipe(map((r) => r.data));
  }

  facturaXml(id: number): Observable<Blob> {
    return this.http.get(`${this.url}/facturas/${id}/xml`, { responseType: 'blob' });
  }
}
