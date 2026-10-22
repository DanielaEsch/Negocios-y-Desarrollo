import { registerLocaleData } from '@angular/common';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import localeEs from '@angular/common/locales/es';
import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
  provideZoneChangeDetection,
} from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { routes } from './app.routes';
import { AppConfig } from './constants/app-config';
import { errorInterceptor } from './services/interceptor/error.interceptor';
import { headersInterceptor } from './services/interceptor/headers.interceptor';
import { loadingInterceptor } from './services/interceptor/loading.interceptor';
import { LanguageService } from './services/language/language.service';
import { PaginatorIntlService } from './services/paginator/paginator-intl.service';
import { ThemeService } from './services/theme/theme.service';

registerLocaleData(localeEs);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    // Orden de los interceptores: headers de trazabilidad, barra de carga y al final el
    // manejo de errores, para que reciba la respuesta antes que nadie.
    provideHttpClient(
      withFetch(),
      withInterceptors([headersInterceptor, loadingInterceptor, errorInterceptor]),
    ),
    provideTranslateService({
      loader: provideTranslateHttpLoader({ prefix: '/i18n/', suffix: '.json' }),
      fallbackLang: AppConfig.defaultLang,
      lang: AppConfig.defaultLang,
    }),
    { provide: MatPaginatorIntl, useClass: PaginatorIntlService },
    provideAppInitializer(() => {
      inject(LanguageService).init();
      inject(ThemeService);
      // Iconos con Material Symbols (la fuente se carga en index.html).
      inject(MatIconRegistry).setDefaultFontSetClass('material-symbols-outlined');
    }),
  ],
};
