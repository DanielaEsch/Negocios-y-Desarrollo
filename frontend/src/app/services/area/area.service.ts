import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Endpoints } from '../../../environments/endpoints';
import { PageQuery, PageResponse, SingleResponse } from '../../models/api/api-response.model';
import { Area, AreaFilters, AreaRequest } from '../../models/area/area.model';
import { ConsumeService } from '../consume/consume.service';

/**
 * Acceso a /api/v1/areas. Devuelve los datos ya desenvueltos de SingleResponse, salvo el
 * listado, que necesita los totales para paginar.
 */
@Injectable({ providedIn: 'root' })
export class AreaService {
  private readonly consume = inject(ConsumeService);
  private readonly url = Endpoints.areas;

  findList(filters: AreaFilters, query: PageQuery): Observable<PageResponse<Area>> {
    return this.consume.httpGet<PageResponse<Area>>(this.url, { ...filters, ...query });
  }

  findById(id: number): Observable<Area> {
    return this.consume
      .httpGet<SingleResponse<Area>>(`${this.url}/${id}`)
      .pipe(map((response) => response.data));
  }

  create(body: AreaRequest): Observable<Area> {
    return this.consume
      .httpPost<SingleResponse<Area>>(this.url, body)
      .pipe(map((response) => response.data));
  }

  update(id: number, body: AreaRequest): Observable<boolean> {
    return this.consume
      .httpPut<SingleResponse<boolean>>(`${this.url}/${id}`, body)
      .pipe(map((response) => response.data));
  }

  delete(id: number): Observable<boolean> {
    return this.consume
      .httpDelete<SingleResponse<boolean>>(`${this.url}/${id}`)
      .pipe(map((response) => response.data));
  }
}
