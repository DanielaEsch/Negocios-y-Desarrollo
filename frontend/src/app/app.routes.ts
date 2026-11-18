import { Routes } from '@angular/router';
import { LayoutComponent } from './components/layout-component/layout-component';
import { authGuard, guestGuard, homeGuard, roleGuard } from './services/guards/auth.guard';

/**
 * Rutas: el login va solo; todo lo demas vive dentro del layout y requiere sesion.
 * La raiz manda a la pantalla de inicio de cada perfil. Cada modulo se carga bajo demanda.
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
      { path: '', pathMatch: 'full', canActivate: [homeGuard], children: [] },
      { path: 'inicio', canActivate: [homeGuard], children: [] },
      {
        path: 'turno',
        title: 'Mi turno | TaskIn',
        loadComponent: () => import('./pages/turno-page/turno-page').then((m) => m.TurnoPage),
      },
      {
        path: 'rondas/:id',
        title: 'Ronda | TaskIn',
        loadComponent: () => import('./pages/ronda-page/ronda-page').then((m) => m.RondaPage),
      },
      {
        path: 'reportes',
        title: 'Mis reportes | TaskIn',
        loadComponent: () =>
          import('./pages/reportes-page/reportes-page').then((m) => m.ReportesPage),
      },
      {
        path: 'tablero',
        title: 'Tablero | TaskIn',
        canActivate: [roleGuard('SUPERVISOR', 'ADMINISTRADOR')],
        loadComponent: () => import('./pages/tablero-page/tablero-page').then((m) => m.TableroPage),
      },
      {
        path: 'incidencias',
        title: 'Incidencias | TaskIn',
        canActivate: [roleGuard('SUPERVISOR', 'ADMINISTRADOR')],
        loadComponent: () =>
          import('./pages/incidencias-page/incidencias-page').then((m) => m.IncidenciasPage),
      },
      {
        path: 'rutinas',
        title: 'Rutinas | TaskIn',
        canActivate: [roleGuard('ADMINISTRADOR')],
        loadComponent: () => import('./pages/rutinas-page/rutinas-page').then((m) => m.RutinasPage),
      },
      {
        path: 'areas',
        title: 'Areas | TaskIn',
        canActivate: [roleGuard('ADMINISTRADOR')],
        loadChildren: () => import('./pages/areas-page/areas.routes').then((m) => m.AREAS_ROUTES),
      },
      {
        path: 'equipo',
        title: 'Equipo | TaskIn',
        canActivate: [roleGuard('ADMINISTRADOR')],
        loadComponent: () => import('./pages/equipo-page/equipo-page').then((m) => m.EquipoPage),
      },
      {
        path: 'suscripcion',
        title: 'Suscripcion | TaskIn',
        canActivate: [roleGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./pages/suscripcion-page/suscripcion-page').then((m) => m.SuscripcionPage),
      },
      {
        path: 'suscripcion/pagar',
        title: 'Pagar | TaskIn',
        canActivate: [roleGuard('ADMINISTRADOR')],
        loadComponent: () => import('./pages/pago-page/pago-page').then((m) => m.PagoPage),
      },
      {
        path: 'suscripcion/ordenes/:id',
        title: 'Orden de pago | TaskIn',
        canActivate: [roleGuard('ADMINISTRADOR', 'COBRANZA')],
        loadComponent: () => import('./pages/orden-page/orden-page').then((m) => m.OrdenPage),
      },
      {
        path: 'cobranza',
        title: 'Cobranza | TaskIn',
        canActivate: [roleGuard('COBRANZA')],
        loadComponent: () =>
          import('./pages/cobranza-page/cobranza-page').then((m) => m.CobranzaPage),
      },
      {
        path: '**',
        loadComponent: () =>
          import('./pages/not-found-page/not-found-page').then((m) => m.NotFoundPage),
      },
    ],
  },
];
