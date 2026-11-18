import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { finalize, switchMap } from 'rxjs';
import { ApiError } from '../../models/api/problem-detail.model';
import { Cotizacion, DatosFiscales, MetodoPago, Periodo, Plan } from '../../models/pago/pago.model';
import { NotificationService } from '../../services/notification/notification.service';
import { PagoService } from '../../services/pago/pago.service';
import { PasarelaError, PasarelaService } from '../../services/pasarela/pasarela.service';
import {
  formatearExpiracion,
  formatearNumero,
  leerExpiracion,
  marca,
  pasaLuhn,
  pesos,
  soloDigitos,
} from '../../utils/tarjeta.util';

/** Tarjetas publicadas por el sandbox: el numero decide el resultado. */
const TARJETAS_PRUEBA = [
  { numero: '4242 4242 4242 4242', resultadoKey: 'pago.prueba.aprobada' },
  { numero: '4000 0000 0000 0002', resultadoKey: 'pago.prueba.declinada' },
  { numero: '4000 0000 0000 9995', resultadoKey: 'pago.prueba.fondos' },
];

/** Claves del SAT mas comunes para un negocio pequeno. */
const REGIMENES = [
  { clave: '601', nombre: 'General de Ley Personas Morales' },
  { clave: '612', nombre: 'Personas Fisicas con Actividades Empresariales' },
  { clave: '626', nombre: 'Regimen Simplificado de Confianza' },
];
const USOS_CFDI = [
  { clave: 'G03', nombre: 'Gastos en general' },
  { clave: 'I04', nombre: 'Equipo de computo y accesorios' },
  { clave: 'S01', nombre: 'Sin efectos fiscales' },
];

/**
 * La pantalla de pago. Sigue el ciclo de la leccion de pagos:
 * <ol>
 *   <li>El precio lo calcula el servidor (cotizacion); el navegador solo dice que quiere.</li>
 *   <li>Tarjeta: el formulario de la pasarela tokeniza directo con ella; a TaskIn solo
 *       llega el token. Transferencia: se genera la orden de compra con CLABE.</li>
 *   <li>La orden queda en PROCESANDO y el recibo espera al webhook.</li>
 * </ol>
 */
@Component({
  selector: 'app-pago-page',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    TranslatePipe,
  ],
  templateUrl: './pago-page.html',
  styleUrl: './pago-page.scss',
})
export class PagoPage implements OnInit {
  private readonly pagoService = inject(PagoService);
  private readonly pasarela = inject(PasarelaService);
  private readonly notification = inject(NotificationService);
  private readonly translate = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);

  protected readonly planes = signal<Plan[]>([]);
  protected readonly plan = signal<string>('NEGOCIO');
  protected readonly periodo = signal<Periodo>('MENSUAL');
  protected readonly metodo = signal<MetodoPago>('TARJETA');
  protected readonly cupon = signal<string | null>(null);
  protected readonly cotizacion = signal<Cotizacion | null>(null);
  protected readonly errorCupon = signal<string | null>(null);
  protected readonly errorTarjeta = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly paso = signal<'tokenizando' | 'creando' | 'cobrando' | null>(null);

  protected readonly tarjetasPrueba = TARJETAS_PRUEBA;
  protected readonly regimenes = REGIMENES;
  protected readonly usos = USOS_CFDI;
  protected readonly pesos = pesos;

  protected readonly cuponInput = this.fb.nonNullable.control('');

  protected readonly fiscales = this.fb.nonNullable.group({
    rfc: ['', [Validators.required, Validators.pattern(/^[A-Z&Ñ]{3,4}\d{6}[A-Z0-9]{3}$/)]],
    razonSocial: ['', [Validators.required, Validators.maxLength(300)]],
    codigoPostal: ['', [Validators.required, Validators.pattern(/^\d{5}$/)]],
    regimenFiscal: ['601', Validators.required],
    usoCfdi: ['G03', Validators.required],
  });

  protected readonly tarjeta = this.fb.nonNullable.group({
    numero: ['', [Validators.required]],
    expiracion: ['', [Validators.required]],
    cvc: ['', [Validators.required, Validators.pattern(/^\d{3,4}$/)]],
    titular: ['', [Validators.required, Validators.maxLength(80)]],
  });

  private readonly numeroTarjeta = signal('');
  protected readonly marcaTarjeta = computed(() => {
    const numero = this.numeroTarjeta();
    return soloDigitos(numero).length >= 2 ? marca(numero) : null;
  });

  protected readonly planElegido = computed(
    () => this.planes().find((p) => p.clave === this.plan()) ?? null,
  );

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    this.plan.set((params.get('plan') ?? 'NEGOCIO').toUpperCase());
    this.periodo.set(params.get('periodo') === 'ANUAL' ? 'ANUAL' : 'MENSUAL');

    this.pagoService.planes().subscribe((planes) => this.planes.set(planes));
    this.pagoService.suscripcion().subscribe((s) => {
      if (s.datosFiscales) {
        this.fiscales.patchValue(s.datosFiscales);
      }
    });
    this.tarjeta.controls.numero.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((v) => this.numeroTarjeta.set(v));
    this.cotizar();
  }

  protected elegirPlan(clave: string): void {
    this.plan.set(clave);
    this.cotizar();
  }

  protected elegirPeriodo(periodo: Periodo): void {
    this.periodo.set(periodo);
    this.cotizar();
  }

  protected aplicarCupon(): void {
    const codigo = this.cuponInput.value.trim().toUpperCase();
    this.cupon.set(codigo || null);
    this.cotizar();
  }

  protected quitarCupon(): void {
    this.cupon.set(null);
    this.cuponInput.setValue('');
    this.errorCupon.set(null);
    this.cotizar();
  }

  /** Cada cambio pide el desglose al servidor: el navegador nunca suma precios. */
  private cotizar(): void {
    this.pagoService
      .cotizar({ plan: this.plan(), periodo: this.periodo(), cupon: this.cupon() })
      .subscribe({
        next: (cotizacion) => {
          this.cotizacion.set(cotizacion);
          this.errorCupon.set(null);
        },
        error: (error: unknown) => {
          if (error instanceof ApiError && error.problem.code === 1602) {
            this.errorCupon.set(error.message);
            this.cupon.set(null);
            this.cotizar();
          }
        },
      });
  }

  protected usarTarjeta(numero: string): void {
    this.tarjeta.patchValue({
      numero,
      expiracion: '12/' + String((new Date().getFullYear() + 2) % 100).padStart(2, '0'),
      cvc: '123',
      titular: this.tarjeta.controls.titular.value || 'Laura Mendez',
    });
    this.errorTarjeta.set(null);
  }

  protected alEscribirNumero(): void {
    const control = this.tarjeta.controls.numero;
    control.setValue(formatearNumero(control.value), { emitEvent: true });
  }

  protected alEscribirExpiracion(): void {
    const control = this.tarjeta.controls.expiracion;
    control.setValue(formatearExpiracion(control.value));
  }

  protected rfcEnMayusculas(): void {
    const control = this.fiscales.controls.rfc;
    control.setValue(control.value.toUpperCase().replace(/\s/g, ''));
  }

  protected pagar(): void {
    this.errorTarjeta.set(null);
    this.fiscales.markAllAsTouched();
    if (this.fiscales.invalid || !this.cotizacion()) {
      return;
    }
    if (this.metodo() === 'TARJETA') {
      this.pagarConTarjeta();
    } else {
      this.generarOrdenDeTransferencia();
    }
  }

  private pagarConTarjeta(): void {
    this.tarjeta.markAllAsTouched();
    const valor = this.tarjeta.getRawValue();
    const expiracion = leerExpiracion(valor.expiracion);
    if (!pasaLuhn(valor.numero)) {
      this.errorTarjeta.set(this.translate.instant('pago.tarjeta.numeroInvalido'));
      return;
    }
    if (!expiracion) {
      this.errorTarjeta.set(this.translate.instant('pago.tarjeta.expiracionInvalida'));
      return;
    }
    if (this.tarjeta.invalid) {
      return;
    }

    this.enviando.set(true);
    this.paso.set('tokenizando');
    // 1) La tarjeta va a la pasarela; vuelve un token de un solo uso
    this.pasarela
      .tokenizar({
        numero: soloDigitos(valor.numero),
        mesExpiracion: expiracion.mes,
        anioExpiracion: expiracion.anio,
        cvc: valor.cvc,
        titular: valor.titular.trim(),
      })
      .pipe(
        // 2) TaskIn crea la orden con el precio que el mismo calcula
        switchMap((token) => {
          this.tarjeta.reset();
          this.paso.set('creando');
          return this.pagoService.crearOrden(this.ordenRequest('TARJETA')).pipe(
            switchMap((orden) => {
              // 3) Cobra con el token; el resultado llega por webhook
              this.paso.set('cobrando');
              return this.pagoService.cobrar(orden.id, token.id);
            }),
          );
        }),
        finalize(() => {
          this.enviando.set(false);
          this.paso.set(null);
        }),
      )
      .subscribe({
        next: (orden) => void this.router.navigate(['/suscripcion/ordenes', orden.id]),
        error: (error: unknown) => this.mostrarError(error),
      });
  }

  private generarOrdenDeTransferencia(): void {
    this.enviando.set(true);
    this.paso.set('creando');
    this.pagoService
      .crearOrden(this.ordenRequest('TRANSFERENCIA'))
      .pipe(
        finalize(() => {
          this.enviando.set(false);
          this.paso.set(null);
        }),
      )
      .subscribe({
        next: (orden) => void this.router.navigate(['/suscripcion/ordenes', orden.id]),
        error: (error: unknown) => this.mostrarError(error),
      });
  }

  private ordenRequest(metodo: MetodoPago) {
    const fiscales = this.fiscales.getRawValue() as DatosFiscales;
    return {
      plan: this.plan(),
      periodo: this.periodo(),
      cupon: this.cupon(),
      metodo,
      datosFiscales: { ...fiscales, razonSocial: fiscales.razonSocial.trim() },
    };
  }

  private mostrarError(error: unknown): void {
    if (error instanceof PasarelaError) {
      this.errorTarjeta.set(error.message);
    } else if (error instanceof ApiError && error.status > 0 && error.status < 500) {
      this.notification.raw(error.message);
    }
  }
}
