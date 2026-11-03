import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { BrandComponent } from '../../components/brand-component/brand-component';
import { Rol } from '../../models/auth/session.model';
import { AuthService } from '../../services/auth/auth.service';

/**
 * Inicio de sesion SIMULADO: valida el formulario y abre una sesion local con el rol elegido.
 */
@Component({
  selector: 'app-login-page',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
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

  protected readonly roles: Rol[] = ['OPERADOR', 'SUPERVISOR', 'ADMINISTRADOR'];
  protected readonly submitting = signal(false);
  protected readonly hidePassword = signal(true);

  protected readonly form = inject(NonNullableFormBuilder).group({
    correo: ['', [Validators.required, Validators.email]],
    contrasena: ['', [Validators.required, Validators.minLength(8)]],
    rol: ['SUPERVISOR' as Rol, Validators.required],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.auth.login(this.form.getRawValue()).subscribe(() => {
      const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/inicio';
      void this.router.navigateByUrl(returnUrl);
    });
  }
}
