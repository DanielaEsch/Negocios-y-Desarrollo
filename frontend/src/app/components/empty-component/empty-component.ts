import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/** Estado vacio: icono y mensaje cuando una lista no tiene resultados. */
@Component({
  selector: 'app-empty-component',
  imports: [MatIconModule],
  template: `
    <div class="empty" role="status">
      <mat-icon class="empty__icon" aria-hidden="true">{{ icon() }}</mat-icon>
      <p class="empty__message">{{ message() }}</p>
    </div>
  `,
  styles: `
    .empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 48px 16px;
      color: var(--mat-sys-on-surface-variant);
      text-align: center;
    }
    .empty__icon {
      width: 48px;
      height: 48px;
      font-size: 48px;
      opacity: 0.6;
    }
    .empty__message {
      margin: 12px 0 0;
      font: var(--mat-sys-body-large);
    }
  `,
})
export class EmptyComponent {
  readonly icon = input('inbox');
  readonly message = input.required<string>();
}
