import { DatePipe } from '@angular/common';
import {
  afterNextRender,
  Component,
  computed,
  DestroyRef,
  ElementRef,
  inject,
  Injector,
  OnInit,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { forkJoin, interval } from 'rxjs';
import { StatusComponent } from '../../components/status-component/status-component';
import { Incidencia } from '../../models/incidencia/incidencia.model';
import { RondaResumen } from '../../models/ronda/ronda.model';
import { Conteo, Tablero } from '../../models/tablero/tablero.model';
import { IncidenciaService } from '../../services/incidencia/incidencia.service';
import { LanguageService } from '../../services/language/language.service';
import { RondaService } from '../../services/ronda/ronda.service';
import { TableroService } from '../../services/tablero/tablero.service';
import { horaDelDia, hoyIso, isoDate, minutosDesde, oracion } from '../../utils/format.util';

/** El tablero se refresca solo: es la pantalla que el supervisor deja abierta. */
const REFRESCO_MS = 30_000;

interface Fila {
  idRutina: number;
  rutina: string;
  celdas: Map<number, RondaResumen[]>;
}

interface Grupo {
  area: string;
  filas: Fila[];
}

/**
 * La hoja de control del dia: una fila por rutina, una columna por hora y en cada cruce
 * la marca de la ronda. Es la misma hoja de papel que se pega en la puerta del bano,
 * pero de todas las areas a la vez y al dia.
 */
@Component({
  selector: 'app-tablero-page',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    TranslatePipe,
    StatusComponent,
  ],
  templateUrl: './tablero-page.html',
  styleUrl: './tablero-page.scss',
})
export class TableroPage implements OnInit {
  private readonly rondaService = inject(RondaService);
  private readonly tableroService = inject(TableroService);
  private readonly incidenciaService = inject(IncidenciaService);
  private readonly translate = inject(TranslateService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly injector = inject(Injector);
  private readonly hoja = viewChild<ElementRef<HTMLElement>>('hoja');
  private centrado = false;
  protected readonly language = inject(LanguageService);

  protected readonly fecha = signal(hoyIso());
  protected readonly rondas = signal<RondaResumen[]>([]);
  protected readonly tablero = signal<Tablero | null>(null);
  protected readonly abiertas = signal<Incidencia[]>([]);
  protected readonly cargado = signal(false);
  protected readonly oracion = oracion;

  protected readonly esHoy = computed(() => this.fecha() === hoyIso());
  protected readonly fechaDate = computed(() => new Date(this.fecha() + 'T12:00:00'));

  /** Columnas: de la primera a la ultima hora con rondas. */
  protected readonly columnas = computed(() => {
    const horas = this.rondas().map((ronda) => horaDelDia(ronda.fechaProgramada));
    if (!horas.length) {
      return [];
    }
    const desde = Math.min(...horas);
    const hasta = Math.max(...horas);
    return Array.from({ length: hasta - desde + 1 }, (_, i) => desde + i);
  });

  protected readonly horaActual = computed(() => (this.esHoy() ? new Date().getHours() : -1));

  /** Filas agrupadas por area, en el orden en que llegan (area y hora). */
  protected readonly grupos = computed<Grupo[]>(() => {
    const areas = new Map<string, Map<number, Fila>>();
    this.rondas().forEach((ronda) => {
      const filas = areas.get(ronda.area) ?? new Map<number, Fila>();
      areas.set(ronda.area, filas);
      const fila = filas.get(ronda.idRutina) ?? {
        idRutina: ronda.idRutina,
        rutina: ronda.rutina,
        celdas: new Map<number, RondaResumen[]>(),
      };
      filas.set(ronda.idRutina, fila);
      const hora = horaDelDia(ronda.fechaProgramada);
      fila.celdas.set(hora, [...(fila.celdas.get(hora) ?? []), ronda]);
    });
    return [...areas.entries()]
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([area, filas]) => ({ area, filas: [...filas.values()] }));
  });

  /** Lo que el supervisor tiene que atender ya: vencidas de hoy e incidencias pendientes. */
  protected readonly vencidas = computed(() =>
    this.rondas().filter((ronda) => ronda.estado === 'VENCIDA'),
  );

  protected readonly dia = computed<Conteo | null>(() => this.tablero()?.dia ?? null);

  ngOnInit(): void {
    this.cargar();
    interval(REFRESCO_MS)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.cargar());
  }

  protected cambiarDia(delta: number): void {
    const fecha = new Date(this.fecha() + 'T12:00:00');
    fecha.setDate(fecha.getDate() + delta);
    const nueva = isoDate(fecha);
    if (nueva > hoyIso()) {
      return;
    }
    this.fecha.set(nueva);
    this.cargar();
  }

  protected irA(ronda: RondaResumen): void {
    void this.router.navigate(['/rondas', ronda.id]);
  }

  /** Texto del tooltip y del lector de pantalla de cada marca. */
  protected describir(ronda: RondaResumen): string {
    const estado = this.translate.instant('estado.ronda.' + ronda.estado);
    const partes = [`${ronda.fechaProgramada.substring(11, 16)} ${oracion(ronda.rutina)}`, estado];
    if (ronda.estado === 'EN_CURSO') {
      partes.push(`${ronda.tareasCompletadas}/${ronda.tareasTotal}`);
    }
    if (ronda.operador) {
      partes.push(ronda.operador);
    }
    if (ronda.incidenciasAbiertas) {
      partes.push(this.translate.instant('tablero.conIncidencia'));
    }
    return partes.join(', ');
  }

  protected hace(valor: string): string {
    const minutos = minutosDesde(valor);
    if (minutos < 60) {
      return this.translate.instant('tiempo.minutos', { n: Math.max(minutos, 1) });
    }
    if (minutos < 24 * 60) {
      return this.translate.instant('tiempo.horas', { n: Math.round(minutos / 60) });
    }
    return this.translate.instant('tiempo.dias', { n: Math.round(minutos / (24 * 60)) });
  }

  protected pct(valor: number): number {
    return Math.round(valor);
  }

  protected etiquetaHora(hora: number): string {
    return String(hora).padStart(2, '0');
  }

  private cargar(): void {
    const fecha = this.fecha();
    forkJoin({
      rondas: this.rondaService.findDia({ fecha }),
      tablero: this.tableroService.resumen(fecha),
      incidencias: this.incidenciaService.findList({}, 100),
    }).subscribe({
      next: ({ rondas, tablero, incidencias }) => {
        if (fecha !== this.fecha()) {
          return;
        }
        this.rondas.set(rondas);
        this.tablero.set(tablero);
        this.abiertas.set(incidencias.filter((incidencia) => incidencia.estado !== 'RESUELTA'));
        this.cargado.set(true);
        this.centrarAhora();
      },
      error: () => this.cargado.set(true),
    });
  }

  /** En pantallas angostas la hoja se desplaza: la primera vez se centra en la hora actual. */
  private centrarAhora(): void {
    if (this.centrado) {
      return;
    }
    afterNextRender(
      () => {
        const contenedor = this.hoja()?.nativeElement;
        const ahora = contenedor?.querySelector<HTMLElement>('.grid__hour--now');
        const nombres = contenedor?.querySelector<HTMLElement>('.grid__corner')?.offsetWidth ?? 0;
        const fuera =
          !!contenedor && !!ahora && ahora.offsetLeft + ahora.offsetWidth > contenedor.clientWidth;
        if (contenedor && ahora && fuera) {
          contenedor.scrollLeft =
            ahora.offsetLeft - nombres - (contenedor.clientWidth - nombres) / 2;
          this.centrado = true;
        }
      },
      { injector: this.injector },
    );
  }
}
