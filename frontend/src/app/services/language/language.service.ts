import { inject, Injectable, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { AppConfig } from '../../constants/app-config';

/** Idioma de la interfaz (es/en). Los textos viven en public/i18n/<idioma>.json. */
@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly translate = inject(TranslateService);
  readonly current = signal<string>(AppConfig.defaultLang);

  init(): void {
    this.use(this.saved() ?? AppConfig.defaultLang);
  }

  use(lang: string): void {
    const value = (AppConfig.langs as readonly string[]).includes(lang)
      ? lang
      : AppConfig.defaultLang;
    this.translate.use(value);
    this.current.set(value);
    document.documentElement.lang = value;
    try {
      localStorage.setItem(AppConfig.storage.lang, value);
    } catch {
      // Sin almacenamiento: vuelve al idioma por defecto al recargar.
    }
  }

  private saved(): string | null {
    try {
      return localStorage.getItem(AppConfig.storage.lang);
    } catch {
      return null;
    }
  }
}
