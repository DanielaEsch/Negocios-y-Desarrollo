import { Component, input } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { EstadoOrden, EstadoSuscripcion } from '../../models/pago/pago.model';

/** Etiqueta de color para el estado de una orden o de la suscripcion. */
@Component({
  selector: 'app-estado-pago',
  imports: [TranslatePipe],
  template: `<span class="badge" [attr.data-tono]="tono()">{{
    'pagos.estado.' + estado() | translate
  }}</span>`,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 2px 10px;
      border-radius: 999px;
      font-size: 0.82rem;
      font-weight: 700;
      white-space: nowrap;
      background: var(--tk-surface-sunk);
      color: var(--tk-ink-soft);
    }
    .badge::before {
      content: '';
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: currentColor;
    }
    .badge[data-tono='ok'] {
      background: var(--tk-done-wash);
      color: var(--tk-done);
    }
    .badge[data-tono='espera'] {
      background: var(--tk-doing-wash);
      color: var(--tk-doing);
    }
    .badge[data-tono='mal'] {
      background: var(--tk-late-wash);
      color: var(--tk-late);
    }
  `,
})
export class EstadoPagoComponent {
  readonly estado = input.required<EstadoOrden | EstadoSuscripcion>();

  protected tono(): string {
    switch (this.estado()) {
      case 'PAGADO':
      case 'ACTIVA':
        return 'ok';
      case 'PROCESANDO':
      case 'PENDIENTE_PAGO':
      case 'PRUEBA':
        return 'espera';
      case 'RECHAZADO':
      case 'VENCIDA':
        return 'mal';
      default:
        return 'neutro';
    }
  }
}
