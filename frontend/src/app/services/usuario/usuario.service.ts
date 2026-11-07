import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Endpoints } from '../../../environments/endpoints';
import { PageResponse, SingleResponse } from '../../models/api/api-response.model';
import { Usuario } from '../../models/auth/session.model';
import { UsuarioAdmin, UsuarioRequest } from '../../models/usuario/usuario.model';
import { ConsumeService } from '../consume/consume.service';

/** Acceso a /api/v1/usuarios. */
@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly consume = inject(ConsumeService);
  private readonly url = Endpoints.usuarios;

  findList(): Observable<UsuarioAdmin[]> {
    return this.consume
      .httpGet<PageResponse<UsuarioAdmin>>(this.url, { size: 100 })
      .pipe(map((response) => response.data));
  }

  create(body: UsuarioRequest): Observable<Usuario> {
    return this.consume
      .httpPost<SingleResponse<Usuario>>(this.url, body)
      .pipe(map((response) => response.data));
  }

  promover(id: number): Observable<Usuario> {
    return this.consume
      .httpPatch<SingleResponse<Usuario>>(`${this.url}/${id}/perfil`, { perfil: 'SUPERVISOR' })
      .pipe(map((response) => response.data));
  }

  updateAreas(id: number, areas: number[]): Observable<Usuario> {
    return this.consume
      .httpPut<SingleResponse<Usuario>>(`${this.url}/${id}/areas`, { areas })
      .pipe(map((response) => response.data));
  }

  delete(id: number): Observable<boolean> {
    return this.consume
      .httpDelete<SingleResponse<boolean>>(`${this.url}/${id}`)
      .pipe(map((response) => response.data));
  }
}
