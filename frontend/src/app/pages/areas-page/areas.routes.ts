import { Routes } from '@angular/router';

export const AREAS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./areas-page').then((m) => m.AreasPage),
  },
];
