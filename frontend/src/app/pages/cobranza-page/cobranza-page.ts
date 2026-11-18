import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { filter } from 'rxjs';
import {
  ConfirmDialogComponent,
  ConfirmDialogData,
} from '../../components/confirm-dialog-component/confirm-dialog-component';
import { EstadoPagoComponent } from '../../components/estado-pago-component/estado-pago-component';
import { ApiError } from '../../models/api/problem-detail.model';
import { OrdenPago } from '../../models/pago/pago.model';
import { LanguageService } from '../../services/language/language.service';
import { NotificationService } from '../../services/notification/notification.service';
import { PagoService } from '../../services/pago/pago.service';
import { pesos } from '../../utils/tarjeta.util';
import { RechazoDialog } from './rechazo-dialog';

/**
 * Cobranza de TaskIn: el lado B2B de la matriz de monetizacion. Concilia las
 * transferencias contra el estado de cuenta y aprieta "Validar pago"; eso sustituye al
 * webhook de la pasarela y dispara la activacion y el CFDI.
 */
@Component({
  selector: 'app-cobranza-page',
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    TranslatePipe,
    EstadoPagoComponent,
  ],
  templateUrl: './cobranza-page.html',
  styleUrl: './cobranza-page.scss',
})
export class CobranzaPage implements OnInit {
  protected readonly language = inject(LanguageService);
  private readonly pagoService = inject(PagoService);
  private readonly notification = inject(NotificationService);
  private readonly dialog = inject(MatDialog);

  protected readonly ordenes = signal<OrdenPago[]>([]);
  protected readonly ocupada = signal<number | null>(null);
  protected readonly pesos = pesos;

  protected readonly porValidar = computed(() =>
    this.ordenes().filter((o) => o.estado === 'PROCESANDO'),
  );
  protected readonly esperando = computed(() =>
    this.ordenes().filter((o) => o.estado === 'PENDIENTE_PAGO'),
  );
  protected readonly resueltas = computed(() =>
    this.ordenes()
      .filter((o) => o.estado === 'PAGADO' || o.estado === 'RECHAZADO' || o.estado === 'VENCIDO')
      .slice(0, 10),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected validar(orden: OrdenPago): void {
    this.dialog
      .open<ConfirmDialogComponent, ConfirmDialogData, boolean>(ConfirmDialogComponent, {
        data: {
          titleKey: 'cobranza.validarTitulo',
          messageKey: 'cobranza.validarMensaje',
          params: {
            total: pesos(orden.desglose.total),
            clave: orden.referenciaPago,
            folio: orden.folio,
          },
          confirmKey: 'cobranza.validar',
          peligro: false,
        },
        width: '480px',
        maxWidth: 'calc(100vw - 32px)',
      })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe(() => this.resolver(orden, 'PAGADO'));
  }

  protected rechazar(orden: OrdenPago): void {
    this.dialog
      .open<RechazoDialog, OrdenPago, string>(RechazoDialog, {
        data: orden,
        width: '480px',
        maxWidth: 'calc(100vw - 32px)',
      })
      .afterClosed()
      .pipe(filter((motivo): motivo is string => !!motivo))
      .subscribe((motivo) => this.resolver(orden, 'RECHAZADO', motivo));
  }

  private resolver(orden: OrdenPago, estado: 'PAGADO' | 'RECHAZADO', motivo?: string): void {
    this.ocupada.set(orden.id);
    this.pagoService.validar(orden.id, estado, motivo).subscribe({
      next: (actualizada) => {
        this.ocupada.set(null);
        this.ordenes.update((lista) =>
          lista.map((o) => (o.id === actualizada.id ? actualizada : o)),
        );
        this.notification.success(estado === 'PAGADO' ? 'cobranza.validada' : 'cobranza.rechazada');
      },
      error: (error: unknown) => {
        this.ocupada.set(null);
        if (error instanceof ApiError && error.status > 0 && error.status < 500) {
          this.notification.raw(error.message);
        }
        this.cargar();
      },
    });
  }

  private cargar(): void {
    this.pagoService
      .ordenes({ metodo: 'TRANSFERENCIA' }, 100)
      .subscribe((o) => this.ordenes.set(o));
  }
}
