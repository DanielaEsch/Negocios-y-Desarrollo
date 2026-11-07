import { Component, computed, inject, input, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { filter, Observable } from 'rxjs';
import {
  ConfirmDialogComponent,
  ConfirmDialogData,
} from '../../components/confirm-dialog-component/confirm-dialog-component';
import { StatusComponent } from '../../components/status-component/status-component';
import { ApiError } from '../../models/api/problem-detail.model';
import { Ronda, TareaRonda } from '../../models/ronda/ronda.model';
import { AuthService } from '../../services/auth/auth.service';
import { NotificationService } from '../../services/notification/notification.service';
import { RondaService } from '../../services/ronda/ronda.service';
import { hora, oracion } from '../../utils/format.util';
import { ReportarDialog, ReportarDialogData } from './reportar-dialog/reportar-dialog';

/**
 * Una ronda: la lista de tareas que el operador va marcando. Cada toque es un PATCH con
 * la version leida; si otra persona movio la ronda, el backend responde 409 y la pantalla
 * se recarga con el estado real en vez de pisarlo.
 */
@Component({
  selector: 'app-ronda-page',
  imports: [RouterLink, MatButtonModule, MatIconModule, TranslatePipe, StatusComponent],
  templateUrl: './ronda-page.html',
  styleUrl: './ronda-page.scss',
})
export class RondaPage implements OnInit {
  /** Id de la ruta (withComponentInputBinding). */
  readonly id = input.required<string>();

  private readonly rondaService = inject(RondaService);
  private readonly notification = inject(NotificationService);
  private readonly dialog = inject(MatDialog);
  protected readonly auth = inject(AuthService);

  protected readonly ronda = signal<Ronda | null>(null);
  protected readonly ocupado = signal(false);
  protected readonly noEncontrada = signal<string | null>(null);
  protected readonly hora = hora;
  protected readonly oracion = oracion;

  protected readonly volver = computed(() =>
    this.auth.hasRole('OPERADOR') ? '/turno' : '/tablero',
  );
  protected readonly esMia = computed(() => this.ronda()?.idOperador === this.auth.usuario()?.id);
  protected readonly puedeTrabajar = computed(
    () => this.ronda()?.estado === 'EN_CURSO' && this.esMia(),
  );
  protected readonly puedeEmpezar = computed(() => this.ronda()?.estado === 'PROGRAMADA');
  protected readonly puedeCancelar = computed(
    () => this.puedeEmpezar() && this.auth.hasRole('SUPERVISOR', 'ADMINISTRADOR'),
  );
  protected readonly hechas = computed(
    () => this.ronda()?.tareas.filter((tarea) => tarea.estado === 'COMPLETADA').length ?? 0,
  );
  protected readonly todasHechas = computed(() => {
    const ronda = this.ronda();
    return ronda !== null && this.hechas() === ronda.tareas.length;
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.rondaService.findById(Number(this.id())).subscribe({
      next: (ronda) => this.ronda.set(ronda),
      error: (error: unknown) => {
        if (error instanceof ApiError && error.status < 500 && error.status !== 0) {
          this.noEncontrada.set(error.message);
        }
      },
    });
  }

  protected empezar(): void {
    const ronda = this.ronda();
    if (ronda) {
      this.aplicar(this.rondaService.cambiarEstado(ronda, 'EN_CURSO'));
    }
  }

  protected marcar(tarea: TareaRonda): void {
    const ronda = this.ronda();
    if (!ronda || !this.puedeTrabajar() || tarea.estado === 'COMPLETADA' || this.ocupado()) {
      return;
    }
    this.aplicar(this.rondaService.completarTarea(ronda, tarea.idTarea));
  }

  protected terminar(): void {
    const ronda = this.ronda();
    if (ronda) {
      this.aplicar(this.rondaService.cambiarEstado(ronda, 'COMPLETADA'), 'ronda.terminada');
    }
  }

  protected cancelar(): void {
    const ronda = this.ronda();
    if (!ronda) {
      return;
    }
    this.dialog
      .open<ConfirmDialogComponent, ConfirmDialogData, boolean>(ConfirmDialogComponent, {
        data: {
          titleKey: 'ronda.cancelarTitulo',
          messageKey: 'ronda.cancelarTexto',
          params: { rutina: oracion(ronda.rutina), hora: hora(ronda.fechaProgramada) },
          confirmKey: 'ronda.cancelar',
        },
      })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe(() => this.aplicar(this.rondaService.cambiarEstado(ronda, 'CANCELADA')));
  }

  protected reportar(): void {
    const ronda = this.ronda();
    if (!ronda) {
      return;
    }
    this.dialog
      .open<ReportarDialog, ReportarDialogData, boolean>(ReportarDialog, {
        data: { idRonda: ronda.id, rutina: oracion(ronda.rutina) },
        width: '520px',
        maxWidth: 'calc(100vw - 32px)',
      })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe(() => {
        this.notification.success('reportar.enviado');
        this.cargar();
      });
  }

  private aplicar(peticion: Observable<Ronda>, mensaje?: string): void {
    this.ocupado.set(true);
    peticion.subscribe({
      next: (ronda) => {
        this.ronda.set(ronda);
        this.ocupado.set(false);
        if (mensaje) {
          this.notification.success(mensaje);
        }
      },
      error: (error: unknown) => {
        this.ocupado.set(false);
        if (error instanceof ApiError && error.status === 409) {
          this.notification.error('ronda.conflicto');
          this.cargar();
        } else if (error instanceof ApiError && error.status < 500 && error.status !== 0) {
          this.notification.raw(error.message);
        }
      },
    });
  }
}
