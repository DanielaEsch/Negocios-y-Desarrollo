import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Endpoints } from '../../../environments/endpoints';
import { SingleResponse } from '../../models/api/api-response.model';
import { Evidencia } from '../../models/evidencia/evidencia.model';
import { ConsumeService } from '../consume/consume.service';

/** Fotos de evidencia: /ejecuciones/{id}/evidencias y /evidencias/{id}/archivo. */
@Injectable({ providedIn: 'root' })
export class EvidenciaService {
  private readonly consume = inject(ConsumeService);
  private readonly http = inject(HttpClient);

  listar(idRonda: number): Observable<Evidencia[]> {
    return this.consume
      .httpGet<SingleResponse<Evidencia[]>>(`${Endpoints.ejecuciones}/${idRonda}/evidencias`)
      .pipe(map((r) => r.data));
  }

  /** Multipart: el navegador pone el Content-Type con su boundary. */
  subir(idRonda: number, archivo: File, nota: string | null): Observable<Evidencia> {
    const datos = new FormData();
    datos.append('archivo', archivo);
    if (nota) {
      datos.append('nota', nota);
    }
    return this.consume
      .httpPost<SingleResponse<Evidencia>>(`${Endpoints.ejecuciones}/${idRonda}/evidencias`, datos)
      .pipe(map((r) => r.data));
  }

  /** La imagen pide token como cualquier ruta: se trae como blob y se muestra con una URL local. */
  imagen(evidencia: Evidencia): Observable<Blob> {
    return this.http.get(`${environment.apiUrl}${evidencia.archivo}`, { responseType: 'blob' });
  }
}
