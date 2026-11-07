import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Endpoints } from '../../../environments/endpoints';
import { SingleResponse } from '../../models/api/api-response.model';
import { Tablero } from '../../models/tablero/tablero.model';
import { ConsumeService } from '../consume/consume.service';

/** Acceso a /api/v1/tablero. */
@Injectable({ providedIn: 'root' })
export class TableroService {
  private readonly consume = inject(ConsumeService);

  resumen(fecha?: string): Observable<Tablero> {
    return this.consume
      .httpGet<SingleResponse<Tablero>>(Endpoints.tablero, { fecha })
      .pipe(map((response) => response.data));
  }
}
