import { signal } from '@angular/core';

/**
 * Estado de una pantalla con signals: datos, carga y error. Cada store concreto define
 * como se cargan sus datos en `load()`.
 */
export abstract class BaseStore<T> {
  protected readonly state = signal<T | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  get snapshot(): T | null {
    return this.state();
  }

  setData(value: T): void {
    this.state.set(value);
  }

  setLoading(value: boolean): void {
    this.loading.set(value);
  }

  setError(message: string | null): void {
    this.error.set(message);
  }

  abstract load(): void;

  clear(): void {
    this.state.set(null);
    this.error.set(null);
  }
}
