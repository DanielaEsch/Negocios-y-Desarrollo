import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { filter, forkJoin } from 'rxjs';
import {
  ConfirmDialogComponent,
  ConfirmDialogData,
} from '../../components/confirm-dialog-component/confirm-dialog-component';
import { ApiError } from '../../models/api/problem-detail.model';
import { AreaResumen } from '../../models/auth/session.model';
import { Rutina } from '../../models/rutina/rutina.model';
import { AreaService } from '../../services/area/area.service';
import { NotificationService } from '../../services/notification/notification.service';
import { RutinaService } from '../../services/rutina/rutina.service';
import { oracion } from '../../utils/format.util';
import { describirFrecuencia } from '../../utils/frecuencia.util';
import { RutinaDialog, RutinaDialogData } from './rutina-dialog/rutina-dialog';

/**
 * Las rutinas agrupadas por area, cada una leida en una frase ("Cada 2 horas, de 08:00 a
 * 20:00"). Al crear o cambiar una rutina el backend reprograma las rondas de hoy.
 */
@Component({
  selector: 'app-rutinas-page',
  imports: [MatButtonModule, MatIconModule, MatMenuModule, TranslatePipe],
  templateUrl: './rutinas-page.html',
  styleUrl: './rutinas-page.scss',
})
export class RutinasPage implements OnInit {
  private readonly rutinaService = inject(RutinaService);
  private readonly areaService = inject(AreaService);
  private readonly notification = inject(NotificationService);
  private readonly translate = inject(TranslateService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  protected readonly rutinas = signal<Rutina[]>([]);
  protected readonly areas = signal<AreaResumen[]>([]);
  protected readonly cargado = signal(false);
  protected readonly oracion = oracion;

  protected readonly grupos = computed(() =>
    this.areas()
      .map((area) => ({
        area,
        rutinas: this.rutinas().filter((rutina) => rutina.idArea === area.id),
      }))
      .filter((grupo) => grupo.rutinas.length),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected frecuencia(rutina: Rutina): string {
    return describirFrecuencia(rutina, this.translate);
  }

  protected abrir(rutina: Rutina | null): void {
    this.dialog
      .open<RutinaDialog, RutinaDialogData, Rutina>(RutinaDialog, {
        data: { rutina, areas: this.areas() },
        width: '640px',
        maxWidth: 'calc(100vw - 32px)',
        autoFocus: 'first-tabbable',
      })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe((guardada) => {
        this.notification.success(rutina ? 'rutinas.guardada' : 'rutinas.creada', {
          name: oracion(guardada.nombre),
        });
        this.cargar();
      });
  }

  protected eliminar(rutina: Rutina): void {
    this.dialog
      .open<ConfirmDialogComponent, ConfirmDialogData, boolean>(ConfirmDialogComponent, {
        data: {
          titleKey: 'rutinas.eliminarTitulo',
          messageKey: 'rutinas.eliminarTexto',
          params: { name: oracion(rutina.nombre) },
        },
      })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe(() =>
        this.rutinaService.delete(rutina.id).subscribe({
          next: () => {
            this.notification.success('rutinas.eliminada', { name: oracion(rutina.nombre) });
            this.cargar();
          },
          error: (error: unknown) => this.fallo(error),
        }),
      );
  }

  /** Rutinas por evento: se lanzan a mano cuando pasa algo (un derrame, una visita). */
  protected lanzar(rutina: Rutina): void {
    this.rutinaService.lanzar(rutina.id).subscribe({
      next: (ronda) => {
        this.notification.success('rutinas.lanzada', { name: oracion(rutina.nombre) });
        void this.router.navigate(['/rondas', ronda.id]);
      },
      error: (error: unknown) => this.fallo(error),
    });
  }

  private cargar(): void {
    forkJoin({
      rutinas: this.rutinaService.findList(),
      areas: this.areaService.findList({}, { page: 1, size: 100 }),
    }).subscribe({
      next: ({ rutinas, areas }) => {
        this.rutinas.set(rutinas);
        this.areas.set(
          areas.data
            .map(({ id, nombre }) => ({ id, nombre }))
            .sort((a, b) => a.nombre.localeCompare(b.nombre)),
        );
        this.cargado.set(true);
      },
      error: () => this.cargado.set(true),
    });
  }

  private fallo(error: unknown): void {
    if (error instanceof ApiError && error.status < 500 && error.status !== 0) {
      this.notification.raw(error.message);
    }
  }
}
