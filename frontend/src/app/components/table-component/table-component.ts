import { DatePipe } from '@angular/common';
import { Component, inject, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslatePipe } from '@ngx-translate/core';
import { AppConfig } from '../../constants/app-config';
import {
  LayoutConfig,
  LayoutField,
  TableAction,
  TableActionEvent,
  TableSortEvent,
} from '../../models/layout/layout.types';
import { LanguageService } from '../../services/language/language.service';
import { EmptyComponent } from '../empty-component/empty-component';

const ACTIONS_COLUMN = '__actions';

/**
 * Tabla generica con paginacion y orden del lado del servidor. La pagina le pasa la
 * configuracion de columnas y acciones (LayoutConfig) y escucha los eventos.
 */
@Component({
  selector: 'app-table-component',
  imports: [
    DatePipe,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    TranslatePipe,
    EmptyComponent,
  ],
  templateUrl: './table-component.html',
  styleUrl: './table-component.scss',
})
export class TableComponent<T extends object> {
  protected readonly language = inject(LanguageService);

  readonly config = input.required<LayoutConfig<T>>();
  readonly data = input.required<T[]>();
  readonly total = input(0);
  readonly pageIndex = input(0);
  readonly pageSize = input<number>(AppConfig.defaultPageSize);
  readonly loading = input(false);
  readonly sortActive = input<string>('');
  readonly sortDirection = input<'asc' | 'desc'>('asc');
  readonly emptyMessage = input('table.empty');

  readonly action = output<TableActionEvent<T>>();
  readonly pageChange = output<PageEvent>();
  readonly sortChange = output<TableSortEvent>();

  protected readonly pageSizeOptions = AppConfig.pageSizeOptions;
  protected readonly actionsColumn = ACTIONS_COLUMN;

  protected columns(): string[] {
    const keys = this.config().fields.map((field) => field.key as string);
    return this.config().actions?.length ? [...keys, ACTIONS_COLUMN] : keys;
  }

  protected value(row: T, field: LayoutField<T>): unknown {
    const value = row[field.key as keyof T];
    return value === null || value === undefined || value === '' ? null : value;
  }

  protected visibleActions(row: T): TableAction<T>[] {
    return (this.config().actions ?? []).filter((item) => item.visible?.(row) ?? true);
  }

  protected trackRow = (_index: number, row: T): unknown => row[this.config().idField as keyof T];

  protected onSort(sort: Sort): void {
    if (!sort.direction) {
      return;
    }
    this.sortChange.emit({
      active: sort.active,
      direction: sort.direction === 'asc' ? 'ASC' : 'DESC',
    });
  }
}
