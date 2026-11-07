import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ApiError } from '../../../models/api/problem-detail.model';
import { RondaService } from '../../../services/ronda/ronda.service';

export interface ReportarDialogData {
  idRonda: number;
  rutina: string;
}

/** Limite de TX_DESCRIPCION en el DDL. */
const MAX_DESCRIPCION = 500;

/**
 * Reportar un problema durante la ronda. Los atajos dejan el inicio de la frase escrito
 * para que el operador solo complete el detalle.
 */
@Component({
  selector: 'app-reportar-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    TranslatePipe,
  ],
  template: `
    <h2 mat-dialog-title>{{ 'reportar.title' | translate }}</h2>
    <mat-dialog-content>
      <p class="hint">{{ 'reportar.hint' | translate: { rutina: data.rutina } }}</p>
      <div class="quick" role="group" [attr.aria-label]="'reportar.atajos' | translate">
        @for (atajo of atajos; track atajo) {
          <button type="button" class="quick__chip" (click)="usar(atajo)">
            {{ 'reportar.atajo.' + atajo | translate }}
          </button>
        }
      </div>
      @if (error(); as message) {
        <p class="form-error" role="alert">{{ message }}</p>
      }
      <mat-form-field appearance="outline" class="field">
        <mat-label>{{ 'reportar.descripcion' | translate }}</mat-label>
        <textarea
          matInput
          rows="4"
          #campo
          [formControl]="descripcion"
          [maxlength]="max"
          cdkFocusInitial
        ></textarea>
        <mat-hint align="end">{{ descripcion.value.length }}/{{ max }}</mat-hint>
        @if (descripcion.hasError('required')) {
          <mat-error>{{ 'reportar.obligatorio' | translate }}</mat-error>
        }
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button type="button" mat-dialog-close>{{ 'actions.cancel' | translate }}</button>
      <button mat-flat-button type="button" [disabled]="enviando()" (click)="enviar()">
        {{ 'reportar.enviar' | translate }}
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    .hint {
      margin: 0 0 12px;
      color: var(--tk-ink-soft);
    }
    .quick {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 16px;
    }
    .quick__chip {
      min-height: 40px;
      padding: 6px 14px;
      border: 1px solid var(--tk-line);
      border-radius: 999px;
      background: var(--tk-paper);
      color: var(--tk-ink);
      font: inherit;
      cursor: pointer;
    }
    .quick__chip:hover {
      border-color: var(--tk-action);
    }
    .field {
      width: 100%;
    }
  `,
})
export class ReportarDialog {
  protected readonly data = inject<ReportarDialogData>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<ReportarDialog, boolean>);
  private readonly rondaService = inject(RondaService);
  private readonly translate = inject(TranslateService);

  private readonly campo = viewChild.required<ElementRef<HTMLTextAreaElement>>('campo');
  protected readonly max = MAX_DESCRIPCION;
  protected readonly atajos = ['falta', 'roto', 'sucio', 'otro'];
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly descripcion = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(MAX_DESCRIPCION)],
  });

  protected usar(atajo: string): void {
    this.descripcion.setValue(this.translate.instant('reportar.inicio.' + atajo));
    // El atajo solo escribe el inicio: el cursor queda al final para completar la frase
    const campo = this.campo().nativeElement;
    campo.focus();
    campo.setSelectionRange(campo.value.length, campo.value.length);
  }

  protected enviar(): void {
    const texto = this.descripcion.value.trim();
    if (!texto) {
      this.descripcion.setValue('');
      this.descripcion.markAsTouched();
      return;
    }
    this.enviando.set(true);
    this.rondaService.reportar(this.data.idRonda, texto).subscribe({
      next: () => this.ref.close(true),
      error: (error: unknown) => {
        this.enviando.set(false);
        if (error instanceof ApiError && error.status < 500) {
          this.error.set(error.problem.invalid_params?.[0]?.reason ?? error.message);
        }
      },
    });
  }
}
