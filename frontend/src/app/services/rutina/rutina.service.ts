import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Endpoints } from '../../../environments/endpoints';
import { PageResponse, SingleResponse } from '../../models/api/api-response.model';
import { Ronda } from '../../models/ronda/ronda.model';
import { Rutina, RutinaRequest } from '../../models/rutina/rutina.model';
import { ConsumeService } from '../consume/consume.service';

/** Acceso a /api/v1/rutinas. */
@Injectable({ providedIn: 'root' })
export class RutinaService {
  private readonly consume = inject(ConsumeService);
  private readonly url = Endpoints.rutinas;

  findList(idArea?: number): Observable<Rutina[]> {
    return this.consume
      .httpGet<PageResponse<Rutina>>(this.url, { idArea, size: 100 })
      .pipe(map((response) => response.data));
  }

  create(body: RutinaRequest): Observable<Rutina> {
    return this.consume
      .httpPost<SingleResponse<Rutina>>(this.url, body)
      .pipe(map((response) => response.data));
  }

  update(id: number, body: RutinaRequest): Observable<Rutina> {
    return this.consume
      .httpPut<SingleResponse<Rutina>>(`${this.url}/${id}`, body)
      .pipe(map((response) => response.data));
  }

  delete(id: number): Observable<boolean> {
    return this.consume
      .httpDelete<SingleResponse<boolean>>(`${this.url}/${id}`)
      .pipe(map((response) => response.data));
  }

  /** Crea ahora una ronda de una rutina por evento. */
  lanzar(id: number): Observable<Ronda> {
    return this.consume
      .httpPost<SingleResponse<Ronda>>(`${this.url}/${id}/ejecuciones`, {})
      .pipe(map((response) => response.data));
  }
}
