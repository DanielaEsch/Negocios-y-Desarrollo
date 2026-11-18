import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, input, OnInit, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { TranslatePipe } from '@ngx-translate/core';
import { ApiError } from '../../../models/api/problem-detail.model';
import {
  Evidencia,
  EVIDENCIA_MAX_BYTES,
  EVIDENCIA_MAX_POR_RONDA,
} from '../../../models/evidencia/evidencia.model';
import { LanguageService } from '../../../services/language/language.service';
import { EvidenciaService } from '../../../services/evidencia/evidencia.service';
import { NotificationService } from '../../../services/notification/notification.service';

interface Foto {
  evidencia: Evidencia;
  url: string | null;
}

/**
 * Fotos de evidencia de la ronda. En el telefono el boton abre la camara trasera
 * (capture="environment"). El tamano y el tipo se revisan aqui para avisar rapido, y el
 * backend los vuelve a revisar por el contenido del archivo.
 */
@Component({
  selector: 'app-evidencias',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    TranslatePipe,
  ],
  templateUrl: './evidencias-component.html',
  styleUrl: './evidencias-component.scss',
})
export class EvidenciasComponent implements OnInit {
  readonly idRonda = input.required<number>();
  readonly puedeSubir = input(false);

  protected readonly language = inject(LanguageService);
  private readonly evidenciaService = inject(EvidenciaService);
  private readonly notification = inject(NotificationService);

  protected readonly fotos = signal<Foto[]>([]);
  protected readonly subiendo = signal(false);
  protected readonly max = EVIDENCIA_MAX_POR_RONDA;
  protected readonly nota = new FormControl('', { nonNullable: true });

  constructor() {
    inject(DestroyRef).onDestroy(() =>
      this.fotos().forEach((f) => f.url && URL.revokeObjectURL(f.url)),
    );
  }

  ngOnInit(): void {
    this.evidenciaService.listar(this.idRonda()).subscribe((lista) => {
      this.fotos.set(lista.map((evidencia) => ({ evidencia, url: null })));
      lista.forEach((evidencia) => this.cargarImagen(evidencia));
    });
  }

  protected elegir(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const archivo = campo.files?.[0];
    campo.value = '';
    if (!archivo) {
      return;
    }
    if (!/^image\/(jpeg|png|webp)$/.test(archivo.type)) {
      this.notification.error('evidencias.soloFotos');
      return;
    }
    if (archivo.size > EVIDENCIA_MAX_BYTES) {
      this.notification.error('evidencias.muyGrande');
      return;
    }
    this.subiendo.set(true);
    this.evidenciaService.subir(this.idRonda(), archivo, this.nota.value.trim() || null).subscribe({
      next: (evidencia) => {
        this.subiendo.set(false);
        this.nota.setValue('');
        this.fotos.update((lista) => [...lista, { evidencia, url: URL.createObjectURL(archivo) }]);
        this.notification.success('evidencias.subida');
      },
      error: (error: unknown) => {
        this.subiendo.set(false);
        if (error instanceof ApiError && error.status > 0 && error.status < 500) {
          this.notification.raw(error.message);
        }
      },
    });
  }

  private cargarImagen(evidencia: Evidencia): void {
    this.evidenciaService.imagen(evidencia).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      this.fotos.update((lista) =>
        lista.map((f) => (f.evidencia.id === evidencia.id ? { ...f, url } : f)),
      );
    });
  }
}
