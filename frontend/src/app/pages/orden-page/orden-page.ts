import { DatePipe } from '@angular/common';
import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { filter, interval, switchMap, take, takeWhile } from 'rxjs';
import { EstadoPagoComponent } from '../../components/estado-pago-component/estado-pago-component';
import { ApiError } from '../../models/api/problem-detail.model';
import { ESTADOS_FINALES, OrdenPago } from '../../models/pago/pago.model';
import { LanguageService } from '../../services/language/language.service';
import { AuthService } from '../../services/auth/auth.service';
import { NotificationService } from '../../services/notification/notification.service';
import { PagoService } from '../../services/pago/pago.service';
import { pesos } from '../../utils/tarjeta.util';

/** Cada cuanto se pregunta por la orden mientras la pasarela decide, y por cuanto tiempo. */
const SONDEO_MS = 1_000;
const SONDEO_MAX = 45;

interface Paso {
  clave: string;
  hecho: boolean;
  actual: boolean;
  falla: boolean;
}

/**
 * Recibo de una orden de pago. Muestra en vivo la maquina de estados mientras llega el
 * webhook, la orden de compra (CLABE y referencia) si es transferencia, y el CFDI cuando
 * queda PAGADA. Se puede imprimir.
 */
@Component({
  selector: 'app-orden-page',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    TranslatePipe,
    EstadoPagoComponent,
  ],
  templateUrl: './orden-page.html',
  styleUrl: './orden-page.scss',
})
export class OrdenPage implements OnInit {
  protected readonly language = inject(LanguageService);
  private readonly pagoService = inject(PagoService);
  private readonly notification = inject(NotificationService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly auth = inject(AuthService);

  protected readonly orden = signal<OrdenPago | null>(null);
  protected readonly esperando = signal(false);
  protected readonly enviando = signal(false);
  protected readonly pesos = pesos;
  protected readonly claveRastreo = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(/^[A-Za-z0-9]{6,30}$/)],
  });

  /** Los pasos del ciclo universal del pago, con el estado actual marcado. */
  protected readonly pasos = computed<Paso[]>(() => {
    const orden = this.orden();
    if (!orden) {
      return [];
    }
    const estado = orden.estado;
    const final = ESTADOS_FINALES.includes(estado);
    const falla = estado === 'RECHAZADO' || estado === 'VENCIDO';
    return [
      {
        clave: 'PENDIENTE_PAGO',
        hecho: estado !== 'PENDIENTE_PAGO',
        actual: estado === 'PENDIENTE_PAGO',
        falla: false,
      },
      {
        clave: 'PROCESANDO',
        hecho: final && estado !== 'VENCIDO',
        actual: estado === 'PROCESANDO',
        falla: false,
      },
      {
        clave: final ? estado : 'PAGADO',
        hecho: estado === 'PAGADO',
        actual: final,
        falla,
      },
    ];
  });

  protected readonly esAdmin = computed(() => this.auth.hasRole('ADMINISTRADOR'));

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.pagoService.orden(id).subscribe((orden) => {
      this.orden.set(orden);
      if (orden.metodo === 'TARJETA' && orden.estado === 'PROCESANDO') {
        this.esperarWebhook(id);
      }
    });
  }

  /** La pantalla no decide nada: solo pregunta hasta que el webhook cierre la orden. */
  private esperarWebhook(id: number): void {
    this.esperando.set(true);
    interval(SONDEO_MS)
      .pipe(
        take(SONDEO_MAX),
        switchMap(() => this.pagoService.orden(id)),
        takeWhile((orden) => !ESTADOS_FINALES.includes(orden.estado), true),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (orden) => {
          this.orden.set(orden);
          if (orden.estado === 'PAGADO') {
            this.notification.success('orden.pagada');
            this.pagoService.suscripcion().subscribe();
          }
        },
        complete: () => this.esperando.set(false),
      });
  }

  protected reportarTransferencia(): void {
    const orden = this.orden();
    this.claveRastreo.markAsTouched();
    if (!orden || this.claveRastreo.invalid) {
      return;
    }
    this.enviando.set(true);
    this.pagoService.reportarTransferencia(orden.id, this.claveRastreo.value.trim()).subscribe({
      next: (actualizada) => {
        this.enviando.set(false);
        this.orden.set(actualizada);
        this.notification.success('orden.transferenciaReportada');
      },
      error: (error: unknown) => {
        this.enviando.set(false);
        if (error instanceof ApiError && error.status < 500 && error.status > 0) {
          this.notification.raw(error.message);
        }
      },
    });
  }

  protected descargarXml(): void {
    const orden = this.orden();
    if (!orden?.factura) {
      return;
    }
    this.pagoService
      .facturaXml(orden.factura.id)
      .pipe(filter((blob) => blob.size > 0))
      .subscribe((blob) => {
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `CFDI-${orden.folio}.xml`;
        enlace.click();
        URL.revokeObjectURL(url);
      });
  }

  protected imprimir(): void {
    window.print();
  }

  protected copiar(texto: string): void {
    void navigator.clipboard
      ?.writeText(texto)
      .then(() => this.notification.success('orden.copiado'));
  }
}
