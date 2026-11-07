import { Component, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslatePipe } from '@ngx-translate/core';
import { Incidencia } from '../../models/incidencia/incidencia.model';

/** Limite de TX_COMENTARIO_RESOLUCION en el DDL. */
const MAX_COMENTARIO = 500;

/**
 * Cerrar una incidencia. El dominio no deja resolver sin explicar que se hizo, asi que
 * el comentario es obligatorio aqui tambien. Devuelve el comentario o nada si se cancela.
 */
@Component({
  selector: 'app-resolver-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    TranslatePipe,
  ],
  template: `
    <h2 mat-dialog-title>{{ 'incidencias.resolverTitulo' | translate }}</h2>
    <mat-dialog-content>
      <blockquote class="quote">{{ data.descripcion }}</blockquote>
      <mat-form-field appearance="outline" class="field">
        <mat-label>{{ 'incidencias.queSeHizo' | translate }}</mat-label>
        <textarea
          matInput
          rows="3"
          [formControl]="comentario"
          [maxlength]="max"
          cdkFocusInitial
        ></textarea>
        <mat-hint>{{ 'incidencias.queSeHizoHint' | translate }}</mat-hint>
        @if (comentario.hasError('required')) {
          <mat-error>{{ 'incidencias.comentarioObligatorio' | translate }}</mat-error>
        }
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" mat-dialog-close>{{ 'actions.cancel' | translate }}</button>
      <button mat-flat-button type="button" (click)="confirmar()">
        {{ 'incidencias.marcarResuelta' | translate }}
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    .quote {
      margin: 0 0 16px;
      padding: 8px 12px;
      border-left: 3px solid var(--tk-late);
      background: var(--tk-surface-sunk);
      border-radius: 0 var(--tk-radius-s) var(--tk-radius-s) 0;
    }
    .field {
      width: 100%;
    }
  `,
})
export class ResolverDialog {
  protected readonly data = inject<Incidencia>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<ResolverDialog, string>);
  protected readonly max = MAX_COMENTARIO;
  protected readonly comentario = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(MAX_COMENTARIO)],
  });

  protected confirmar(): void {
    const texto = this.comentario.value.trim();
    if (!texto) {
      this.comentario.setValue('');
      this.comentario.markAsTouched();
      return;
    }
    this.ref.close(texto);
  }
}
