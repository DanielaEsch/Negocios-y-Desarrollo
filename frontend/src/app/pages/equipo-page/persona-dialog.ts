import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslatePipe } from '@ngx-translate/core';
import { ApiError } from '../../models/api/problem-detail.model';
import { AreaResumen, Usuario } from '../../models/auth/session.model';
import { UsuarioService } from '../../services/usuario/usuario.service';
import { oracion } from '../../utils/format.util';

export interface PersonaDialogData {
  /** Con usuario solo se cambian sus areas; sin el, es un alta. */
  usuario: Usuario | null;
  areas: AreaResumen[];
}

/**
 * Alta de una persona o cambio de sus areas. En el alta, el consentimiento del aviso de
 * privacidad (LFPDPPP) es obligatorio: sin el, el dominio no registra a nadie.
 */
@Component({
  selector: 'app-persona-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    TranslatePipe,
  ],
  template: `
    <h2 mat-dialog-title>
      @if (data.usuario) {
        {{ 'equipo.areasDe' | translate: { name: data.usuario.nombre } }}
      } @else {
        {{ 'equipo.agregar' | translate }}
      }
    </h2>
    <mat-dialog-content>
      <form [formGroup]="form" id="persona-form" class="form" (ngSubmit)="guardar()">
        @if (error(); as message) {
          <p class="form-error" role="alert">{{ message }}</p>
        }
        @if (!data.usuario) {
          <mat-form-field appearance="outline">
            <mat-label>{{ 'equipo.nombre' | translate }}</mat-label>
            <input matInput formControlName="nombre" maxlength="150" cdkFocusInitial />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>{{ 'equipo.correo' | translate }}</mat-label>
            <input matInput type="email" formControlName="correo" maxlength="150" />
            @if (form.controls.correo.hasError('email')) {
              <mat-error>{{ 'equipo.correoInvalido' | translate }}</mat-error>
            }
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>{{ 'equipo.contrasena' | translate }}</mat-label>
            <input
              matInput
              type="text"
              formControlName="contrasena"
              maxlength="72"
              autocomplete="off"
            />
            <mat-hint>{{ 'equipo.contrasenaHint' | translate }}</mat-hint>
          </mat-form-field>
        }

        <fieldset class="areas">
          <legend>{{ 'equipo.queAreas' | translate }}</legend>
          @for (area of data.areas; track area.id) {
            <mat-checkbox [checked]="elegidas().has(area.id)" (change)="alternar(area.id)">
              {{ oracion(area.nombre) }}
            </mat-checkbox>
          }
        </fieldset>

        @if (!data.usuario) {
          <div class="consent">
            <mat-checkbox formControlName="aceptaAvisoPrivacidad">
              {{ 'equipo.consentimiento' | translate }}
            </mat-checkbox>
            <p class="form-note">{{ 'equipo.consentimientoNota' | translate }}</p>
          </div>
        }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" mat-dialog-close>{{ 'actions.cancel' | translate }}</button>
      <button mat-flat-button type="submit" form="persona-form" [disabled]="guardando()">
        {{ (data.usuario ? 'actions.save' : 'equipo.agregarBoton') | translate }}
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    .form {
      display: flex;
      flex-direction: column;
      padding-top: 4px;
    }
    .areas {
      display: flex;
      flex-direction: column;
      margin: 8px 0 12px;
      padding: 0;
      border: 0;
    }
    .areas legend {
      margin-bottom: 4px;
      padding: 0;
      font-weight: 700;
    }
    .consent {
      padding: 10px 12px;
      border-radius: var(--tk-radius-s);
      background: var(--tk-surface-sunk);
    }
  `,
})
export class PersonaDialog {
  protected readonly data = inject<PersonaDialogData>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<PersonaDialog, Usuario>);
  private readonly usuarioService = inject(UsuarioService);
  protected readonly oracion = oracion;
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly elegidas = signal(
    new Set(this.data.usuario?.areas.map((area) => area.id) ?? []),
  );

  protected readonly form = new FormGroup({
    nombre: new FormControl('', {
      nonNullable: true,
      validators: this.data.usuario ? [] : [Validators.required],
    }),
    correo: new FormControl('', {
      nonNullable: true,
      validators: this.data.usuario ? [] : [Validators.required, Validators.email],
    }),
    contrasena: new FormControl('', {
      nonNullable: true,
      validators: this.data.usuario ? [] : [Validators.required, Validators.minLength(8)],
    }),
    aceptaAvisoPrivacidad: new FormControl(false, { nonNullable: true }),
  });

  protected alternar(id: number): void {
    this.elegidas.update((actual) => {
      const nuevas = new Set(actual);
      if (nuevas.has(id)) {
        nuevas.delete(id);
      } else {
        nuevas.add(id);
      }
      return nuevas;
    });
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const areas = [...this.elegidas()];
    const valores = this.form.getRawValue();
    const peticion = this.data.usuario
      ? this.usuarioService.updateAreas(this.data.usuario.id, areas)
      : this.usuarioService.create({
          nombre: valores.nombre.trim(),
          correo: valores.correo.trim(),
          contrasena: valores.contrasena,
          aceptaAvisoPrivacidad: valores.aceptaAvisoPrivacidad,
          areas,
        });
    this.guardando.set(true);
    this.error.set(null);
    peticion.subscribe({
      next: (usuario) => this.ref.close(usuario),
      error: (error: unknown) => {
        this.guardando.set(false);
        if (error instanceof ApiError && error.status < 500) {
          this.error.set(error.problem.invalid_params?.[0]?.reason ?? error.message);
        }
      },
    });
  }
}
