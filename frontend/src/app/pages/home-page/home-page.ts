import { Component, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { PageHeaderComponent } from '../../components/page-header-component/page-header-component';
import { MENU_ITEMS } from '../../constants/menu';
import { AuthService } from '../../services/auth/auth.service';

/** Inicio: saludo y accesos a los modulos (los que no tienen API aparecen como proximos). */
@Component({
  selector: 'app-home-page',
  imports: [RouterLink, MatIconModule, TranslatePipe, PageHeaderComponent],
  templateUrl: './home-page.html',
  styleUrl: './home-page.scss',
})
export class HomePage {
  protected readonly auth = inject(AuthService);
  protected readonly modules = MENU_ITEMS.filter((item) => item.route !== '/inicio');
}
