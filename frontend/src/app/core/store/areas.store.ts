import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { AppConfig } from '../../constants/app-config';
import { PageResponse } from '../../models/api/api-response.model';
import { ApiError } from '../../models/api/problem-detail.model';
import { Area, AreaFilters } from '../../models/area/area.model';
import { TableSortEvent } from '../../models/layout/layout.types';
import { AreaService } from '../../services/area/area.service';
import { BaseStore } from './base.store';

/**
 * Estado del listado de areas: filtros, pagina, orden y resultado. Se provee en la pagina
 * (no en root) para que cada visita empiece limpia.
 */
@Injectable()
export class AreasStore extends BaseStore<PageResponse<Area>> {
  private readonly areaService = inject(AreaService);

  readonly filters = signal<AreaFilters>({});
  /** Indice de pagina como lo usa MatPaginator (empieza en 0). */
  readonly pageIndex = signal(0);
  readonly pageSize = signal<number>(AppConfig.defaultPageSize);
  readonly sort = signal<TableSortEvent>({ active: 'nombre', direction: 'ASC' });

  readonly items = computed(() => this.state()?.data ?? []);
  readonly total = computed(() => this.state()?.totalElements ?? 0);

  load(): void {
    this.setLoading(true);
    this.setError(null);
    const sort = this.sort();
    this.areaService
      .findList(this.filters(), {
        page: this.pageIndex() + 1,
        size: this.pageSize(),
        orderBy: sort.active,
        orderType: sort.direction,
      })
      .pipe(finalize(() => this.setLoading(false)))
      .subscribe({
        next: (response) => this.setData(response),
        error: (error: unknown) =>
          this.setError(error instanceof ApiError ? error.message : String(error)),
      });
  }

  search(filters: AreaFilters): void {
    this.filters.set(filters);
    this.pageIndex.set(0);
    this.load();
  }

  changePage(pageIndex: number, pageSize: number): void {
    this.pageIndex.set(pageIndex);
    this.pageSize.set(pageSize);
    this.load();
  }

  changeSort(sort: TableSortEvent): void {
    this.sort.set(sort);
    this.pageIndex.set(0);
    this.load();
  }
}
