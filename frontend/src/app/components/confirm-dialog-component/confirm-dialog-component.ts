import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { TranslatePipe } from '@ngx-translate/core';

export interface ConfirmDialogData {
  titleKey: string;
  messageKey: string;
  params?: Record<string, unknown>;
  confirmKey?: string;
}

/** Confirmacion de acciones destructivas. Se cierra con true si el usuario acepta. */
@Component({
  selector: 'app-confirm-dialog-component',
  imports: [MatDialogModule, MatButtonModule, TranslatePipe],
  template: `
    <h2 mat-dialog-title>{{ data.titleKey | translate }}</h2>
    <mat-dialog-content>
      <p>{{ data.messageKey | translate: data.params }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false">{{ 'actions.cancel' | translate }}</button>
      <button mat-flat-button class="button-danger" [mat-dialog-close]="true" cdkFocusInitial>
        {{ data.confirmKey ?? 'actions.delete' | translate }}
      </button>
    </mat-dialog-actions>
  `,
})
export class ConfirmDialogComponent {
  readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
}
