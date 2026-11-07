import { Component, computed, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { TranslatePipe } from '@ngx-translate/core';

type Tono = 'done' | 'doing' | 'late' | 'idle' | 'off';

/** Tono e icono de cada estado: el mismo color significa lo mismo en toda la app. */
const ESTADOS: Record<string, { tono: Tono; icon: string }> = {
  PROGRAMADA: { tono: 'idle', icon: 'schedule' },
  EN_CURSO: { tono: 'doing', icon: 'pending' },
  COMPLETADA: { tono: 'done', icon: 'check_circle' },
  VENCIDA: { tono: 'late', icon: 'error' },
  CANCELADA: { tono: 'off', icon: 'block' },
  PENDIENTE: { tono: 'late', icon: 'report' },
  REVISION: { tono: 'doing', icon: 'visibility' },
  RESUELTA: { tono: 'done', icon: 'task_alt' },
};

/** Etiqueta de estado de una ronda o una incidencia. */
@Component({
  selector: 'app-status-component',
  imports: [MatIconModule, TranslatePipe],
  template: `
    <span class="status" [attr.data-tono]="meta().tono">
      <mat-icon aria-hidden="true">{{ meta().icon }}</mat-icon>
      {{ 'estado.' + tipo() + '.' + estado() | translate }}
    </span>
  `,
  styles: `
    .status {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 2px 10px 2px 6px;
      border-radius: 999px;
      font-size: 0.82rem;
      font-weight: 600;
      white-space: nowrap;
      background: var(--tk-surface-sunk);
      color: var(--tk-ink-soft);
    }
    mat-icon {
      width: 18px;
      height: 18px;
      font-size: 18px;
    }
    [data-tono='done'] {
      background: var(--tk-done-wash);
      color: var(--tk-done);
    }
    [data-tono='doing'] {
      background: var(--tk-doing-wash);
      color: var(--tk-doing);
    }
    [data-tono='late'] {
      background: var(--tk-late-wash);
      color: var(--tk-late);
    }
    [data-tono='off'] {
      text-decoration: line-through;
    }
  `,
})
export class StatusComponent {
  readonly estado = input.required<string>();
  readonly tipo = input<'ronda' | 'incidencia'>('ronda');
  protected readonly meta = computed(() => ESTADOS[this.estado()] ?? ESTADOS['PROGRAMADA']);
}
