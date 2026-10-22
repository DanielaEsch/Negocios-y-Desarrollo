import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { map } from 'rxjs';
import { AppConfig } from '../../constants/app-config';
import { MENU_ITEMS } from '../../constants/menu';
import { AuthService } from '../../services/auth/auth.service';
import { LanguageService } from '../../services/language/language.service';
import { LoadingService } from '../../services/loading/loading.service';
import { ThemeService } from '../../services/theme/theme.service';
import { BrandComponent } from '../brand-component/brand-component';

/**
 * Marco de las pantallas privadas: barra superior, menu lateral y el contenido de la ruta.
 * En pantallas chicas el menu se vuelve un panel que se abre con el boton de hamburguesa.
 */
@Component({
  selector: 'app-layout-component',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatDividerModule,
    MatTooltipModule,
    MatProgressBarModule,
    TranslatePipe,
    BrandComponent,
  ],
  templateUrl: './layout-component.html',
  styleUrl: './layout-component.scss',
})
export class LayoutComponent {
  protected readonly auth = inject(AuthService);
  protected readonly theme = inject(ThemeService);
  protected readonly language = inject(LanguageService);
  protected readonly loading = inject(LoadingService);

  protected readonly menu = MENU_ITEMS;
  protected readonly langs = AppConfig.langs;

  protected readonly isHandset = toSignal(
    inject(BreakpointObserver)
      .observe([Breakpoints.XSmall, Breakpoints.Small])
      .pipe(map((result) => result.matches)),
    { initialValue: false },
  );
  /** Solo aplica en pantallas chicas; en escritorio el menu siempre esta abierto. */
  protected readonly opened = signal(false);
  protected readonly sidenavOpened = computed(() => !this.isHandset() || this.opened());

  protected readonly initials = computed(() => {
    const nombre = this.auth.session()?.nombre ?? '';
    return nombre
      .split(' ')
      .map((part) => part.charAt(0))
      .join('')
      .slice(0, 2)
      .toUpperCase();
  });

  protected toggleMenu(): void {
    this.opened.update((value) => !value);
  }

  protected closeOnHandset(): void {
    if (this.isHandset()) {
      this.opened.set(false);
    }
  }
}
