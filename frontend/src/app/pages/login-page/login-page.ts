import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { BrandComponent } from '../../components/brand-component/brand-component';
import { Rol } from '../../models/auth/session.model';
import { ApiError } from '../../models/api/problem-detail.model';
import { AuthService } from '../../services/auth/auth.service';

interface Cuenta {
  correo: string;
  nombre: string;
  rol: Rol;
}

/** Cuentas de los datos de demostracion (database/02_datos_base.sql). */
const CUENTAS_DEMO: Cuenta[] = [
  { correo: 'ana@taskin.mx', nombre: 'Ana', rol: 'OPERADOR' },
  { correo: 'jorge@taskin.mx', nombre: 'Jorge', rol: 'SUPERVISOR' },
  { correo: 'laura@taskin.mx', nombre: 'Laura', rol: 'ADMINISTRADOR' },
];
const CONTRASENA_DEMO = 'TaskIn2026';

/**
 * Inicio de sesion contra la API. A la izquierda, la hoja de control en papel que la app
 * sustituye; las cuentas de demostracion llenan el formulario con un toque.
 */
@Component({
  selector: 'app-login-page',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    TranslatePipe,
    BrandComponent,
  ],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly cuentas = CUENTAS_DEMO;
  protected readonly submitting = signal(false);
  protected readonly hidePassword = signal(true);
  protected readonly error = signal<string | null>(null);

  /** Casillas de la hoja de papel de fondo: hechas, a medias y sin marcar. */
  protected readonly hoja = [
    { hora: '08:00', marcas: ['ok', 'ok', 'ok', 'ok'] },
    { hora: '10:00', marcas: ['ok', 'ok', 'ok', 'ok'] },
    { hora: '12:00', marcas: ['ok', 'ok', 'no', 'ok'] },
    { hora: '14:00', marcas: ['ok', 'ok', '', ''] },
    { hora: '16:00', marcas: ['', '', '', ''] },
    { hora: '18:00', marcas: ['', '', '', ''] },
  ];
  protected readonly columnas = [
    'login.paper.jabon',
    'login.paper.lavabos',
    'login.paper.piso',
    'login.paper.basura',
  ];

  protected readonly form = inject(NonNullableFormBuilder).group({
    correo: ['', [Validators.required, Validators.email]],
    contrasena: ['', [Validators.required]],
  });

  protected usar(cuenta: Cuenta): void {
    this.form.setValue({ correo: cuenta.correo, contrasena: CONTRASENA_DEMO });
    this.submit();
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? this.auth.home();
        void this.router.navigateByUrl(returnUrl);
      },
      error: (error: unknown) => {
        this.submitting.set(false);
        if (error instanceof ApiError && error.status !== 0 && error.status < 500) {
          this.error.set(error.message);
        }
      },
    });
  }
}
