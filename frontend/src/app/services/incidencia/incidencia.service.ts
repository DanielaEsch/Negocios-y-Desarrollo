import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Endpoints } from '../../../environments/endpoints';
import { PageResponse, SingleResponse } from '../../models/api/api-response.model';
import {
  EstadoIncidencia,
  Incidencia,
  IncidenciaFiltros,
} from '../../models/incidencia/incidencia.model';
import { ConsumeService } from '../consume/consume.service';

/** Acceso a /api/v1/incidencias. */
@Injectable({ providedIn: 'root' })
export class IncidenciaService {
  private readonly consume = inject(ConsumeService);
  private readonly url = Endpoints.incidencias;

  findList(filtros: IncidenciaFiltros = {}, size = 100): Observable<Incidencia[]> {
    return this.consume
      .httpGet<PageResponse<Incidencia>>(this.url, { ...filtros, size })
      .pipe(map((response) => response.data));
  }

  cambiarEstado(
    id: number,
    estado: EstadoIncidencia,
    comentarioResolucion?: string,
  ): Observable<Incidencia> {
    return this.consume
      .httpPatch<SingleResponse<Incidencia>>(`${this.url}/${id}`, {
        estado,
        comentarioResolucion: comentarioResolucion ?? null,
      })
      .pipe(map((response) => response.data));
  }
}
