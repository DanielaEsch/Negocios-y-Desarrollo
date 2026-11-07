import { DatePipe } from '@angular/common';
import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { interval } from 'rxjs';
import { StatusComponent } from '../../components/status-component/status-component';
import { ApiError } from '../../models/api/problem-detail.model';
import { RondaResumen } from '../../models/ronda/ronda.model';
import { AuthService } from '../../services/auth/auth.service';
import { LanguageService } from '../../services/language/language.service';
import { NotificationService } from '../../services/notification/notification.service';
import { RondaService } from '../../services/ronda/ronda.service';
import { hora, horaAhora, oracion } from '../../utils/format.util';

/** Cada minuto se vuelve a pedir el dia: otra persona pudo tomar o terminar una ronda. */
const REFRESCO_MS = 60_000;

type Foco =
  | { tipo: 'sigue'; ronda: RondaResumen }
  | { tipo: 'toca'; ronda: RondaResumen }
  | { tipo: 'siguiente'; ronda: RondaResumen }
  | { tipo: 'libre' };

/**
 * El turno del operador: lo que toca ahora en grande y, abajo, el dia como linea de
 * tiempo. Todo lo de sus areas, para que vea tambien lo que ya hizo su companero.
 */
@Component({
  selector: 'app-turno-page',
  imports: [DatePipe, RouterLink, MatButtonModule, MatIconModule, TranslatePipe, StatusComponent],
  templateUrl: './turno-page.html',
  styleUrl: './turno-page.scss',
})
export class TurnoPage implements OnInit {
  private readonly rondaService = inject(RondaService);
  private readonly router = inject(Router);
  private readonly notification = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly auth = inject(AuthService);
  protected readonly language = inject(LanguageService);

  protected readonly rondas = signal<RondaResumen[]>([]);
  protected readonly cargado = signal(false);
  protected readonly iniciando = signal(false);
  protected readonly hoy = new Date();
  protected readonly hora = hora;
  protected readonly oracion = oracion;
  protected readonly horaAhora = horaAhora;

  protected readonly nombre = computed(() => this.auth.usuario()?.nombre.split(' ')[0] ?? '');
  protected readonly hechas = computed(
    () => this.rondas().filter((ronda) => ronda.estado === 'COMPLETADA').length,
  );
  protected readonly activas = computed(
    () => this.rondas().filter((ronda) => ronda.estado !== 'CANCELADA').length,
  );

  /** Lo que hay que mostrar arriba: la ronda que llevo, la que ya toca o la que sigue. */
  protected readonly foco = computed<Foco>(() => {
    const yo = this.auth.usuario()?.id;
    const ahora = Date.now();
    const rondas = this.rondas();
    const sigue = rondas.find((ronda) => ronda.estado === 'EN_CURSO' && ronda.idOperador === yo);
    if (sigue) {
      return { tipo: 'sigue', ronda: sigue };
    }
    const programadas = rondas.filter((ronda) => ronda.estado === 'PROGRAMADA');
    const toca = programadas.find((ronda) => new Date(ronda.fechaProgramada).getTime() <= ahora);
    if (toca) {
      return { tipo: 'toca', ronda: toca };
    }
    return programadas.length ? { tipo: 'siguiente', ronda: programadas[0] } : { tipo: 'libre' };
  });

  /** La linea de tiempo agrupada por hora. */
  protected readonly horas = computed(() => {
    const grupos = new Map<string, RondaResumen[]>();
    this.rondas().forEach((ronda) => {
      const clave = hora(ronda.fechaProgramada);
      grupos.set(clave, [...(grupos.get(clave) ?? []), ronda]);
    });
    return [...grupos.entries()].map(([clave, items]) => ({ hora: clave, items }));
  });

  /** Hora a la que va la marca de "ahora" en la linea de tiempo. */
  protected readonly horaActual = computed(() => {
    const ahora = horaAhora();
    return this.horas().find((grupo) => grupo.hora > ahora)?.hora ?? null;
  });

  ngOnInit(): void {
    this.cargar();
    interval(REFRESCO_MS)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.cargar());
  }

  protected cargar(): void {
    this.rondaService.findDia().subscribe({
      next: (rondas) => {
        this.rondas.set(rondas);
        this.cargado.set(true);
      },
      error: () => this.cargado.set(true),
    });
  }

  /** Empezar desde la tarjeta: toma la ronda y abre su lista de tareas. */
  protected empezar(ronda: RondaResumen): void {
    this.iniciando.set(true);
    this.rondaService.findById(ronda.id).subscribe({
      next: (detalle) =>
        this.rondaService.cambiarEstado(detalle, 'EN_CURSO').subscribe({
          next: () => void this.router.navigate(['/rondas', ronda.id]),
          error: (error: unknown) => this.fallo(error),
        }),
      error: (error: unknown) => this.fallo(error),
    });
  }

  protected esMia(ronda: RondaResumen): boolean {
    return ronda.idOperador === this.auth.usuario()?.id;
  }

  private fallo(error: unknown): void {
    this.iniciando.set(false);
    if (error instanceof ApiError && error.status < 500 && error.status !== 0) {
      this.notification.raw(error.message);
    }
    this.cargar();
  }
}
