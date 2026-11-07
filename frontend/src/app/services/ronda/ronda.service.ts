import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Endpoints } from '../../../environments/endpoints';
import { PageResponse, SingleResponse } from '../../models/api/api-response.model';
import { Incidencia } from '../../models/incidencia/incidencia.model';
import { EstadoRonda, Ronda, RondaFiltros, RondaResumen } from '../../models/ronda/ronda.model';
import { ConsumeService } from '../consume/consume.service';

/**
 * Acceso a /api/v1/ejecuciones. Cada cambio de estado manda la version que se leyo: si
 * otra persona modifico la ronda, el backend responde 409 y la pantalla recarga.
 */
@Injectable({ providedIn: 'root' })
export class RondaService {
  private readonly consume = inject(ConsumeService);
  private readonly url = Endpoints.ejecuciones;

  /** Todas las rondas de un dia (el backend admite hasta 100 por pagina). */
  findDia(filtros: RondaFiltros = {}): Observable<RondaResumen[]> {
    return this.consume
      .httpGet<PageResponse<RondaResumen>>(this.url, { ...filtros, size: 100 })
      .pipe(map((response) => response.data));
  }

  findById(id: number): Observable<Ronda> {
    return this.consume
      .httpGet<SingleResponse<Ronda>>(`${this.url}/${id}`)
      .pipe(map((response) => response.data));
  }

  cambiarEstado(ronda: Ronda, estado: EstadoRonda): Observable<Ronda> {
    return this.consume
      .httpPatch<SingleResponse<Ronda>>(`${this.url}/${ronda.id}`, {
        estado,
        version: ronda.version,
      })
      .pipe(map((response) => response.data));
  }

  completarTarea(ronda: Ronda, idTarea: number): Observable<Ronda> {
    return this.consume
      .httpPatch<SingleResponse<Ronda>>(`${this.url}/${ronda.id}/tareas/${idTarea}`, {
        estado: 'COMPLETADA',
        version: ronda.version,
      })
      .pipe(map((response) => response.data));
  }

  reportar(idRonda: number, descripcion: string): Observable<Incidencia> {
    return this.consume
      .httpPost<SingleResponse<Incidencia>>(`${this.url}/${idRonda}/incidencias`, { descripcion })
      .pipe(map((response) => response.data));
  }
}
