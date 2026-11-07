import { Rol } from '../auth/session.model';

export interface MenuItem {
  labelKey: string;
  icon: string;
  route: string;
  /** Perfiles que ven la opcion. */
  roles: Rol[];
  /** Las opciones de configuracion van en un grupo aparte. */
  group?: 'config';
}
