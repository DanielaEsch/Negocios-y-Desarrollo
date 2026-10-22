import { Component, input } from '@angular/core';

/** Marca de la aplicacion: nombre y eslogan. */
@Component({
  selector: 'app-brand-component',
  template: `
    <span class="brand" [class.brand--large]="size() === 'large'">
      <span class="brand__mark" aria-hidden="true">
        <svg viewBox="0 0 24 24" width="100%" height="100%">
          <rect x="2" y="2" width="20" height="20" rx="6" fill="currentColor" />
          <path
            d="M7 12.5l3.2 3.2L17 9"
            fill="none"
            stroke="var(--mat-sys-on-primary)"
            stroke-width="2.4"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </span>
      <span class="brand__text">
        <span class="brand__name">TaskIn</span>
        <span class="brand__slogan">for Small Business</span>
      </span>
    </span>
  `,
  styles: `
    .brand {
      display: inline-flex;
      align-items: center;
      gap: 10px;
      color: var(--mat-sys-primary);
    }
    .brand__mark {
      width: 32px;
      height: 32px;
      flex: none;
    }
    .brand__text {
      display: flex;
      flex-direction: column;
      line-height: 1.05;
    }
    .brand__name {
      font: var(--mat-sys-title-large);
      font-weight: 700;
      color: var(--mat-sys-on-surface);
      letter-spacing: -0.02em;
    }
    .brand__slogan {
      font: var(--mat-sys-label-small);
      color: var(--mat-sys-on-surface-variant);
      font-style: italic;
    }
    .brand--large .brand__mark {
      width: 48px;
      height: 48px;
    }
    .brand--large .brand__name {
      font: var(--mat-sys-headline-medium);
      font-weight: 700;
    }
    .brand--large .brand__slogan {
      font: var(--mat-sys-label-large);
      font-style: italic;
    }
  `,
})
export class BrandComponent {
  readonly size = input<'normal' | 'large'>('normal');
}
