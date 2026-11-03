import { Component, computed, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { PageEvent } from '@angular/material/paginator';
import { TranslatePipe } from '@ngx-translate/core';
import { debounceTime, distinctUntilChanged, filter } from 'rxjs';
import {
  ConfirmDialogComponent,
  ConfirmDialogData,
} from '../../components/confirm-dialog-component/confirm-dialog-component';
import { PageHeaderComponent } from '../../components/page-header-component/page-header-component';
import { TableComponent } from '../../components/table-component/table-component';
import { AppConfig } from '../../constants/app-config';
import { AreasStore } from '../../core/store/areas.store';
import { ApiError } from '../../models/api/problem-detail.model';
import { Area } from '../../models/area/area.model';
import { LayoutConfig, TableActionEvent } from '../../models/layout/layout.types';
import { AreaService } from '../../services/area/area.service';
import { AuthService } from '../../services/auth/auth.service';
import { NotificationService } from '../../services/notification/notification.service';
import { AreaFormDialog, AreaFormDialogData } from './area-form-dialog/area-form-dialog';

/**
 * Catalogo de areas. Todos los perfiles consultan; supervisor y administrador crean y
 * editan; solo el administrador elimina.
 */
@Component({
  selector: 'app-areas-page',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    TranslatePipe,
    PageHeaderComponent,
    TableComponent,
  ],
  providers: [AreasStore],
  templateUrl: './areas-page.html',
  styleUrl: './areas-page.scss',
})
export class AreasPage implements OnInit {
  protected readonly store = inject(AreasStore);
  private readonly areaService = inject(AreaService);
  private readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly notification = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly canEdit = computed(() => this.auth.hasRole('SUPERVISOR', 'ADMINISTRADOR'));
  protected readonly canDelete = computed(() => this.auth.hasRole('ADMINISTRADOR'));
  protected readonly search = new FormControl('', { nonNullable: true });

  protected readonly config = computed<LayoutConfig<Area>>(() => ({
    idField: 'id',
    fields: [
      { key: 'nombre', labelKey: 'areas.fields.nombre', type: 'text', sortable: true },
      { key: 'descripcion', labelKey: 'areas.fields.descripcion', type: 'text' },
      {
        key: 'fechaCreacion',
        labelKey: 'areas.fields.fechaCreacion',
        type: 'date',
        sortable: true,
      },
    ],
    actions: [
      ...(this.canEdit() ? [{ id: 'edit', labelKey: 'actions.edit', icon: 'edit' }] : []),
      ...(this.canDelete()
        ? [{ id: 'delete', labelKey: 'actions.delete', icon: 'delete', color: 'warn' as const }]
        : []),
    ],
  }));

  ngOnInit(): void {
    this.store.load();
    this.search.valueChanges
      .pipe(
        debounceTime(AppConfig.searchDebounce),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((nombre) => this.store.search({ nombre }));
  }

  protected onPage(event: PageEvent): void {
    this.store.changePage(event.pageIndex, event.pageSize);
  }

  protected onAction(event: TableActionEvent<Area>): void {
    if (event.action === 'edit') {
      this.openForm(event.row);
    } else if (event.action === 'delete') {
      this.confirmDelete(event.row);
    }
  }

  protected openForm(area?: Area): void {
    this.dialog
      .open<AreaFormDialog, AreaFormDialogData, boolean>(AreaFormDialog, {
        data: { area },
        width: '520px',
        maxWidth: 'calc(100vw - 32px)',
        autoFocus: 'first-tabbable',
      })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe(() => {
        this.notification.success(area ? 'areas.updated' : 'areas.created');
        this.store.load();
      });
  }

  private confirmDelete(area: Area): void {
    this.dialog
      .open<ConfirmDialogComponent, ConfirmDialogData, boolean>(ConfirmDialogComponent, {
        data: {
          titleKey: 'areas.deleteTitle',
          messageKey: 'areas.deleteMessage',
          params: { name: area.nombre },
        },
      })
      .afterClosed()
      .pipe(filter(Boolean))
      .subscribe(() =>
        this.areaService.delete(area.id).subscribe({
          next: () => {
            this.notification.success('areas.deleted');
            this.store.load();
          },
          error: (error: unknown) => {
            if (error instanceof ApiError && error.status < 500) {
              this.notification.raw(error.message);
            }
          },
        }),
      );
  }
}
