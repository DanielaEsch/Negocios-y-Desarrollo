import { MenuItem } from '../models/menu/menu.model';

/**
 * Opciones del menu lateral. Las que tienen `soon` se muestran deshabilitadas hasta
 * que exista su API.
 */
export const MENU_ITEMS: MenuItem[] = [
  { labelKey: 'menu.home', icon: 'home', route: '/inicio' },
  { labelKey: 'menu.areas', icon: 'domain', route: '/areas' },
  { labelKey: 'menu.routines', icon: 'event_repeat', route: '/rutinas', soon: true },
  { labelKey: 'menu.executions', icon: 'task_alt', route: '/ejecuciones', soon: true },
  { labelKey: 'menu.incidents', icon: 'report', route: '/incidencias', soon: true },
];
