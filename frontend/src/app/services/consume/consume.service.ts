import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

type QueryValue = string | number | boolean | null | undefined;
export type QueryParams = Record<string, QueryValue>;

/**
 * Unico punto de salida HTTP de los servicios. Limpia los parametros vacios para no
 * mandar `?nombre=` y deja que los interceptores agreguen headers, carga y errores.
 */
@Injectable({ providedIn: 'root' })
export class ConsumeService {
  private readonly http = inject(HttpClient);

  httpGet<T>(url: string, params?: QueryParams): Observable<T> {
    return this.http.get<T>(url, { params: this.buildParams(params) });
  }

  httpPost<T>(url: string, body: unknown, params?: QueryParams): Observable<T> {
    return this.http.post<T>(url, body, { params: this.buildParams(params) });
  }

  httpPut<T>(url: string, body: unknown, params?: QueryParams): Observable<T> {
    return this.http.put<T>(url, body, { params: this.buildParams(params) });
  }

  httpDelete<T>(url: string, params?: QueryParams): Observable<T> {
    return this.http.delete<T>(url, { params: this.buildParams(params) });
  }

  private buildParams(params?: QueryParams): HttpParams {
    let httpParams = new HttpParams();
    Object.entries(params ?? {}).forEach(([key, value]) => {
      if (value === null || value === undefined) {
        return;
      }
      const text = String(value).trim();
      if (text !== '') {
        httpParams = httpParams.set(key, text);
      }
    });
    return httpParams;
  }
}
