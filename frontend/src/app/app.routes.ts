import { Routes } from '@angular/router';
import { LayoutComponent } from './components/layout-component/layout-component';
import { authGuard, guestGuard } from './services/guards/auth.guard';

/**
 * Rutas: el login va solo; todo lo demas vive dentro del layout y requiere sesion.
 * Cada modulo se carga bajo demanda.
 */
export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    title: 'TaskIn',
    loadComponent: () => import('./pages/login-page/login-page').then((m) => m.LoginPage),
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      {
        path: 'inicio',
        title: 'TaskIn',
        loadComponent: () => import('./pages/home-page/home-page').then((m) => m.HomePage),
      },
      {
        path: 'areas',
        title: 'Areas | TaskIn',
        loadChildren: () => import('./pages/areas-page/areas.routes').then((m) => m.AREAS_ROUTES),
      },
      {
        path: '**',
        loadComponent: () =>
          import('./pages/not-found-page/not-found-page').then((m) => m.NotFoundPage),
      },
    ],
  },
];
