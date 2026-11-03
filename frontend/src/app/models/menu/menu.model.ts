export interface MenuItem {
  labelKey: string;
  icon: string;
  route: string;
  /** Modulo planeado que aun no tiene API. */
  soon?: boolean;
}
