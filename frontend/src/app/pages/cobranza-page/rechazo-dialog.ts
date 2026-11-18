import { Component, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslatePipe } from '@ngx-translate/core';
import { OrdenPago } from '../../models/pago/pago.model';

/** Limite de TX_MOTIVO_RECHAZO en el DDL. */
const MAX_MOTIVO = 300;

/** Rechazar una transferencia: el dominio exige decir por que. Devuelve el motivo. */
@Component({
  selector: 'app-rechazo-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    TranslatePipe,
  ],
  template: `
    <h2 mat-dialog-title>{{ 'cobranza.rechazarTitulo' | translate: { folio: data.folio } }}</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" class="field">
        <mat-label>{{ 'cobranza.motivo' | translate }}</mat-label>
        <textarea
          matInput
          rows="3"
          [formControl]="motivo"
          [maxlength]="max"
          cdkFocusInitial
        ></textarea>
        <mat-hint>{{ 'cobranza.motivoHint' | translate }}</mat-hint>
        @if (motivo.hasError('required')) {
          <mat-error>{{ 'cobranza.motivoObligatorio' | translate }}</mat-error>
        }
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" mat-dialog-close>{{ 'actions.cancel' | translate }}</button>
      <button mat-flat-button type="button" class="button-danger" (click)="confirmar()">
        {{ 'cobranza.rechazar' | translate }}
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    .field {
      width: 100%;
    }
  `,
})
export class RechazoDialog {
  protected readonly data = inject<OrdenPago>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<RechazoDialog, string>);
  protected readonly max = MAX_MOTIVO;
  protected readonly motivo = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(MAX_MOTIVO)],
  });

  protected confirmar(): void {
    const texto = this.motivo.value.trim();
    if (!texto) {
      this.motivo.setValue('');
      this.motivo.markAsTouched();
      return;
    }
    this.ref.close(texto);
  }
}
