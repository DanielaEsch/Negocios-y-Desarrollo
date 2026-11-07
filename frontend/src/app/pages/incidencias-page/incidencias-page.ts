import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { filter, interval } from 'rxjs';
import { ApiError } from '../../models/api/problem-detail.model';
import { EstadoIncidencia, Incidencia } from '../../models/incidencia/incidencia.model';
import { IncidenciaService } from '../../services/incidencia/incidencia.service';
import { NotificationService } from '../../services/notification/notification.service';
import { minutosDesde, oracion } from '../../utils/format.util';
import { ResolverDialog } from './resolver-dialog';

const REFRESCO_MS = 30_000;
/** Las resueltas se acumulan; en el tablero solo importan las ultimas. */
const MAX_RESUELTAS = 8;

interface Columna {
  estado: EstadoIncidencia;
  items: Incidencia[];
  total: number;
}

/**
 * Las incidencias como un tablero de tres columnas: lo que nadie ha tomado, lo que alguien
 * ya esta viendo y lo que se cerro. Cada tarjeta tiene un solo boton: el siguiente paso.
 */
@Component({
  selector: 'app-incidencias-page',
  imports: [RouterLink, MatButtonModule, MatIconModule, TranslatePipe],
  templateUrl: './incidencias-page.html',
  styleUrl: './incidencias-page.scss',
})
export class IncidenciasPage implements OnInit {
  private readonly incidenciaService = inject(IncidenciaService);
  private readonly notification = inject(NotificationService);
  private readonly translate = inject(TranslateService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly incidencias = signal<Incidencia[]>([]);
  protected readonly area = signal<number | null>(null);
  protected readonly ocupada = signal<number | null>(null);
  protected readonly oracion = oracion;

  /** Areas que aparecen en las incidencias, para el filtro. */
  protected readonly areas = computed(() => {
    const areas = new Map<number, string>();
    this.incidencias().forEach((incidencia) => areas.set(incidencia.idArea, incidencia.area));
    return [...areas.entries()]
      .map(([id, nombre]) => ({ id, nombre }))
      .sort((a, b) => a.nombre.localeCompare(b.nombre));
  });

  protected readonly columnas = computed<Columna[]>(() => {
    const area = this.area();
    const visibles = this.incidencias().filter(
      (incidencia) => area === null || incidencia.idArea === area,
    );
    return (['PENDIENTE', 'REVISION', 'RESUELTA'] as const).map((estado) => {
      const items = visibles.filter((incidencia) => incidencia.estado === estado);
      return {
        estado,
        total: items.length,
        items: estado === 'RESUELTA' ? items.slice(0, MAX_RESUELTAS) : items,
      };
    });
  });

  ngOnInit(): void {
    this.cargar();
    interval(REFRESCO_MS)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.cargar());
  }

  protected filtrar(area: number | null): void {
    this.area.set(area);
  }

  protected atender(incidencia: Incidencia): void {
    this.cambiar(incidencia, 'REVISION', undefined, 'incidencias.atendida');
  }

  protected resolver(incidencia: Incidencia): void {
    this.dialog
      .open<ResolverDialog, Incidencia, string>(ResolverDialog, {
        data: incidencia,
        width: '520px',
        maxWidth: 'calc(100vw - 32px)',
      })
      .afterClosed()
      .pipe(filter((comentario): comentario is string => !!comentario))
      .subscribe((comentario) =>
        this.cambiar(incidencia, 'RESUELTA', comentario, 'incidencias.resuelta'),
      );
  }

  protected hace(valor: string | null): string {
    if (!valor) {
      return '';
    }
    const minutos = minutosDesde(valor);
    if (minutos < 60) {
      return this.translate.instant('tiempo.minutos', { n: Math.max(minutos, 1) });
    }
    if (minutos < 24 * 60) {
      return this.translate.instant('tiempo.horas', { n: Math.round(minutos / 60) });
    }
    return this.translate.instant('tiempo.dias', { n: Math.round(minutos / (24 * 60)) });
  }

  private cambiar(
    incidencia: Incidencia,
    estado: EstadoIncidencia,
    comentario: string | undefined,
    mensaje: string,
  ): void {
    this.ocupada.set(incidencia.id);
    this.incidenciaService.cambiarEstado(incidencia.id, estado, comentario).subscribe({
      next: (actualizada) => {
        this.ocupada.set(null);
        this.incidencias.update((lista) =>
          lista.map((item) => (item.id === actualizada.id ? actualizada : item)),
        );
        this.notification.success(mensaje);
      },
      error: (error: unknown) => {
        this.ocupada.set(null);
        if (error instanceof ApiError && error.status < 500 && error.status !== 0) {
          this.notification.raw(error.message);
        }
        this.cargar();
      },
    });
  }

  private cargar(): void {
    this.incidenciaService
      .findList({}, 100)
      .subscribe((incidencias) => this.incidencias.set(incidencias));
  }
}
