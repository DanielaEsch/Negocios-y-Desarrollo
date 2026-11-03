import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ApiError } from '../../models/api/problem-detail.model';
import { AreaService } from '../../services/area/area.service';
import { AreasStore } from './areas.store';

describe('AreasStore', () => {
  const findList = vi.fn();

  beforeEach(() => {
    findList.mockReset();
    TestBed.configureTestingModule({
      providers: [AreasStore, { provide: AreaService, useValue: { findList } }],
    });
  });

  it('carga la pagina 1 y expone items y total', () => {
    findList.mockReturnValue(
      of({
        success: true,
        data: [{ id: 1, nombre: 'COCINA', descripcion: null, fechaCreacion: '' }],
        page: 1,
        size: 10,
        totalElements: 1,
        totalPages: 1,
      }),
    );
    const store = TestBed.inject(AreasStore);

    store.load();

    expect(findList).toHaveBeenCalledWith(
      {},
      { page: 1, size: 10, orderBy: 'nombre', orderType: 'ASC' },
    );
    expect(store.items().length).toBe(1);
    expect(store.total()).toBe(1);
    expect(store.loading()).toBe(false);
  });

  it('buscar regresa a la primera pagina', () => {
    findList.mockReturnValue(
      of({ success: true, data: [], page: 1, size: 10, totalElements: 0, totalPages: 0 }),
    );
    const store = TestBed.inject(AreasStore);
    store.changePage(3, 20);

    store.search({ nombre: 'cocina' });

    expect(store.pageIndex()).toBe(0);
    expect(findList).toHaveBeenLastCalledWith(
      { nombre: 'cocina' },
      { page: 1, size: 20, orderBy: 'nombre', orderType: 'ASC' },
    );
  });

  it('guarda el mensaje del ProblemDetail si falla', () => {
    findList.mockReturnValue(
      throwError(
        () =>
          new ApiError({
            type: 'x',
            title: 'Bad Request',
            status: 400,
            detail: 'La petición no es válida.',
          }),
      ),
    );
    const store = TestBed.inject(AreasStore);

    store.load();

    expect(store.error()).toBe('La petición no es válida.');
    expect(store.loading()).toBe(false);
  });
});
