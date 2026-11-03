import { inject, Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';

/**
 * Mensajes breves al usuario. Recibe claves de traduccion o, con `raw`, el texto que
 * mando el backend (ya viene en espanol).
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);
  private readonly translate = inject(TranslateService);

  success(key: string, params?: Record<string, unknown>): void {
    this.open(this.translate.instant(key, params), 'snack-success');
  }

  error(key: string, params?: Record<string, unknown>): void {
    this.open(this.translate.instant(key, params), 'snack-error');
  }

  raw(message: string): void {
    this.open(message, 'snack-error');
  }

  private open(message: string, panelClass: string): void {
    this.snackBar.open(message, this.translate.instant('actions.close'), {
      duration: 4000,
      panelClass,
    });
  }
}
