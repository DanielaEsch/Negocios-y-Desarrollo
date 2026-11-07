import { Component, input } from '@angular/core';

/** Marca de la aplicacion: la hoja de control en cuatro casillas, nombre y eslogan. */
@Component({
  selector: 'app-brand-component',
  template: `
    <span class="brand" [class.brand--large]="size() === 'large'">
      <span class="brand__mark" aria-hidden="true">
        <svg viewBox="0 0 24 24" width="100%" height="100%">
          <!-- Tres casillas de la hoja de control ya marcadas y la siguiente por hacer -->
          <rect x="2" y="2" width="9" height="9" rx="2.2" fill="currentColor" />
          <rect x="13" y="2" width="9" height="9" rx="2.2" fill="currentColor" />
          <rect x="2" y="13" width="9" height="9" rx="2.2" fill="currentColor" />
          <rect
            x="13.75"
            y="13.75"
            width="7.5"
            height="7.5"
            rx="1.8"
            fill="none"
            stroke="var(--tk-action)"
            stroke-width="1.6"
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
      color: var(--tk-ink);
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
      font-size: 1.3rem;
      font-weight: 800;
      color: var(--tk-ink);
      letter-spacing: -0.02em;
    }
    .brand__slogan {
      font-size: 0.72rem;
      color: var(--tk-ink-soft);
    }
    .brand--large .brand__mark {
      width: 48px;
      height: 48px;
    }
    .brand--large .brand__name {
      font-size: 2rem;
      font-weight: 800;
    }
    .brand--large .brand__slogan {
      font-size: 0.9rem;
    }
  `,
})
export class BrandComponent {
  readonly size = input<'normal' | 'large'>('normal');
}
