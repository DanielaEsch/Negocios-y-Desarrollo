import { MenuItem } from '../models/menu/menu.model';

/**
 * Navegacion por perfil. El operador solo ve lo suyo (su turno y sus reportes); quien
 * supervisa ve el tablero y las incidencias; el administrador ademas configura.
 */
export const MENU_ITEMS: MenuItem[] = [
  { labelKey: 'menu.turno', icon: 'checklist', route: '/turno', roles: ['OPERADOR'] },
  { labelKey: 'menu.reportes', icon: 'campaign', route: '/reportes', roles: ['OPERADOR'] },
  {
    labelKey: 'menu.tablero',
    icon: 'grid_view',
    route: '/tablero',
    roles: ['SUPERVISOR', 'ADMINISTRADOR'],
  },
  {
    labelKey: 'menu.incidencias',
    icon: 'report',
    route: '/incidencias',
    roles: ['SUPERVISOR', 'ADMINISTRADOR'],
  },
  {
    labelKey: 'menu.rutinas',
    icon: 'event_repeat',
    route: '/rutinas',
    roles: ['ADMINISTRADOR'],
    group: 'config',
  },
  {
    labelKey: 'menu.areas',
    icon: 'domain',
    route: '/areas',
    roles: ['ADMINISTRADOR'],
    group: 'config',
  },
  {
    labelKey: 'menu.equipo',
    icon: 'group',
    route: '/equipo',
    roles: ['ADMINISTRADOR'],
    group: 'config',
  },
  {
    labelKey: 'menu.suscripcion',
    icon: 'credit_card',
    route: '/suscripcion',
    roles: ['ADMINISTRADOR'],
    group: 'config',
  },
  { labelKey: 'menu.cobranza', icon: 'account_balance', route: '/cobranza', roles: ['COBRANZA'] },
];
