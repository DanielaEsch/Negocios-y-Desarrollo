import { DatePipe, LowerCasePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { EstadoPagoComponent } from '../../components/estado-pago-component/estado-pago-component';
import { OrdenPago, Periodo, Plan, Suscripcion } from '../../models/pago/pago.model';
import { LanguageService } from '../../services/language/language.service';
import { PagoService } from '../../services/pago/pago.service';
import { pesos } from '../../utils/tarjeta.util';

/**
 * La suscripcion del negocio a TaskIn: en que plan esta, hasta cuando, los planes a la
 * venta y el historial de pagos con sus facturas.
 */
@Component({
  selector: 'app-suscripcion-page',
  imports: [
    DatePipe,
    LowerCasePipe,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    TranslatePipe,
    EstadoPagoComponent,
  ],
  templateUrl: './suscripcion-page.html',
  styleUrl: './suscripcion-page.scss',
})
export class SuscripcionPage implements OnInit {
  protected readonly language = inject(LanguageService);
  private readonly pagoService = inject(PagoService);

  protected readonly suscripcion = signal<Suscripcion | null>(null);
  protected readonly planes = signal<Plan[]>([]);
  protected readonly ordenes = signal<OrdenPago[]>([]);
  protected readonly periodo = signal<Periodo>('MENSUAL');
  protected readonly pesos = pesos;

  ngOnInit(): void {
    this.pagoService.suscripcion().subscribe((s) => this.suscripcion.set(s));
    this.pagoService.planes().subscribe((p) => this.planes.set(p));
    this.pagoService.ordenes({}, 20).subscribe((o) => this.ordenes.set(o));
  }

  /** Lo que se ve como precio mensual: en anual, el equivalente con los meses de regalo. */
  protected precioPorMes(plan: Plan): number {
    return this.periodo() === 'ANUAL' ? plan.precioAnual / 12 : plan.precioMensual;
  }
}
