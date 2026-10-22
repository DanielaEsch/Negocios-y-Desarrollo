import { computed, Injectable, signal } from '@angular/core';

/**
 * Cuenta las peticiones en curso. La barra de carga del layout se muestra mientras
 * haya al menos una.
 */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly pending = signal(0);

  readonly isLoading = computed(() => this.pending() > 0);

  start(): void {
    this.pending.update((count) => count + 1);
  }

  stop(): void {
    this.pending.update((count) => Math.max(count - 1, 0));
  }
}
