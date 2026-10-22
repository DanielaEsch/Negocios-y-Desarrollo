import { effect, Injectable, signal } from '@angular/core';
import { AppConfig } from '../../constants/app-config';

export type Theme = 'light' | 'dark';

/** Tema claro/oscuro. Se guarda por navegador y se aplica como clase en <html>. */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly theme = signal<Theme>(this.initial());

  constructor() {
    effect(() => {
      const theme = this.theme();
      document.documentElement.classList.toggle('dark-theme', theme === 'dark');
      try {
        localStorage.setItem(AppConfig.storage.theme, theme);
      } catch {
        // Sin almacenamiento: el tema dura lo que la pestana.
      }
    });
  }

  toggle(): void {
    this.theme.update((theme) => (theme === 'dark' ? 'light' : 'dark'));
  }

  private initial(): Theme {
    try {
      const saved = localStorage.getItem(AppConfig.storage.theme);
      if (saved === 'light' || saved === 'dark') {
        return saved;
      }
    } catch {
      // Se usa la preferencia del sistema.
    }
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
}
