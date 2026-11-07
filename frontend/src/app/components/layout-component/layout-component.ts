import { Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { AppConfig } from '../../constants/app-config';
import { MENU_ITEMS } from '../../constants/menu';
import { AuthService } from '../../services/auth/auth.service';
import { LanguageService } from '../../services/language/language.service';
import { LoadingService } from '../../services/loading/loading.service';
import { ThemeService } from '../../services/theme/theme.service';
import { iniciales } from '../../utils/format.util';
import { BrandComponent } from '../brand-component/brand-component';

/**
 * Marco de las pantallas privadas. En escritorio, un riel lateral con la navegacion del
 * perfil; en el telefono, barra superior y pestanas abajo, al alcance del pulgar.
 */
@Component({
  selector: 'app-layout-component',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
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
  protected readonly langs = AppConfig.langs;

  protected readonly allItems = computed(() => {
    const perfil = this.auth.usuario()?.perfil;
    return MENU_ITEMS.filter((item) => perfil !== undefined && item.roles.includes(perfil));
  });
  protected readonly mainItems = computed(() => this.allItems().filter((item) => !item.group));
  protected readonly configItems = computed(() =>
    this.allItems().filter((item) => item.group === 'config'),
  );
  protected readonly initials = computed(() => iniciales(this.auth.usuario()?.nombre));
}
