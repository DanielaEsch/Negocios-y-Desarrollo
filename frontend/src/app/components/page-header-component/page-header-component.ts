import { Component, input } from '@angular/core';

/** Encabezado de pagina: titulo, descripcion y, a la derecha, las acciones que se proyecten. */
@Component({
  selector: 'app-page-header-component',
  template: `
    <header class="page-header">
      <div>
        <h1 class="page-header__title">{{ title() }}</h1>
        @if (subtitle()) {
          <p class="page-header__subtitle">{{ subtitle() }}</p>
        }
      </div>
      <div class="page-header__actions">
        <ng-content />
      </div>
    </header>
  `,
  styles: `
    .page-header {
      display: flex;
      flex-wrap: wrap;
      align-items: flex-end;
      justify-content: space-between;
      gap: 16px;
      margin-bottom: 24px;
    }
    .page-header__title {
      margin: 0;
      font: var(--mat-sys-headline-small);
      color: var(--mat-sys-on-surface);
    }
    .page-header__subtitle {
      margin: 4px 0 0;
      font: var(--mat-sys-body-medium);
      color: var(--mat-sys-on-surface-variant);
    }
    .page-header__actions {
      display: flex;
      gap: 8px;
    }
  `,
})
export class PageHeaderComponent {
  readonly title = input.required<string>();
  readonly subtitle = input<string>();
}
