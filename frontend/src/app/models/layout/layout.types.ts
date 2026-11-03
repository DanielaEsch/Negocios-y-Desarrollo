/**
 * Configuracion declarativa de una tabla: cada pagina describe sus columnas y acciones
 * y app-table-component las pinta.
 */
export type FieldType = 'text' | 'date' | 'number';

export interface LayoutField<T> {
  key: keyof T & string;
  labelKey: string;
  type: FieldType;
  sortable?: boolean;
  /** Texto cuando el valor viene vacio. */
  emptyValue?: string;
}

export interface TableAction<T> {
  id: string;
  labelKey: string;
  icon: string;
  color?: 'primary' | 'warn';
  /** Si regresa false, la accion no se pinta en esa fila. */
  visible?: (row: T) => boolean;
}

export interface LayoutConfig<T> {
  fields: LayoutField<T>[];
  actions?: TableAction<T>[];
  /** Campo con el identificador; se usa como trackBy. */
  idField: keyof T & string;
}

export interface TableActionEvent<T> {
  action: string;
  row: T;
}

export interface TableSortEvent {
  active: string;
  direction: 'ASC' | 'DESC';
}
