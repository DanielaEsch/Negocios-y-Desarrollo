import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { TranslatePipe } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { AreaLimits } from '../../../constants/validation';
import { ApiError } from '../../../models/api/problem-detail.model';
import { Area, AreaRequest } from '../../../models/area/area.model';
import { AreaService } from '../../../services/area/area.service';
import { applyServerErrors } from '../../../utils/problem-detail.util';

export interface AreaFormDialogData {
  area?: Area;
}

/** Al menos un caracter que no sea espacio, igual que @NotBlank. */
const NOT_BLANK = /\S/;

/**
 * Alta y edicion de un area. Valida lo mismo que el DTO del backend; si aun asi la API
 * responde 400 o 409, el error se pinta en el campo que corresponde.
 */
@Component({
  selector: 'app-area-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    TranslatePipe,
  ],
  templateUrl: './area-form-dialog.html',
  styleUrl: './area-form-dialog.scss',
})
export class AreaFormDialog {
  private readonly areaService = inject(AreaService);
  private readonly dialogRef = inject<MatDialogRef<AreaFormDialog, boolean>>(MatDialogRef);
  protected readonly data = inject<AreaFormDialogData>(MAT_DIALOG_DATA);

  protected readonly limits = AreaLimits;
  protected readonly saving = signal(false);
  protected readonly generalError = signal<string | null>(null);

  protected readonly form = inject(NonNullableFormBuilder).group({
    nombre: [
      this.data.area?.nombre ?? '',
      [Validators.required, Validators.pattern(NOT_BLANK), Validators.maxLength(AreaLimits.nombre)],
    ],
    descripcion: [
      this.data.area?.descripcion ?? '',
      [Validators.maxLength(AreaLimits.descripcion)],
    ],
  });

  protected get isEdit(): boolean {
    return this.data.area !== undefined;
  }

  protected submit(): void {
    this.generalError.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const body: AreaRequest = {
      nombre: value.nombre.trim(),
      descripcion: value.descripcion.trim() || null,
    };
    const area = this.data.area;
    const request$: Observable<unknown> = area
      ? this.areaService.update(area.id, body)
      : this.areaService.create(body);

    this.saving.set(true);
    request$.subscribe({
      next: () => this.dialogRef.close(true),
      error: (error: unknown) => {
        this.saving.set(false);
        this.handleError(error);
      },
    });
  }

  private handleError(error: unknown): void {
    if (!(error instanceof ApiError)) {
      this.generalError.set(String(error));
      return;
    }
    if (error.status === 400 && applyServerErrors(this.form, error.problem)) {
      return;
    }
    if (error.status === 409) {
      // Nombre duplicado: el mensaje del backend va en el campo nombre.
      const control = this.form.controls.nombre;
      control.setErrors({ server: error.message });
      control.markAsTouched();
      return;
    }
    if (error.status >= 500 || error.status === 0) {
      return; // Ya lo aviso el errorInterceptor.
    }
    this.generalError.set(error.message);
  }
}
