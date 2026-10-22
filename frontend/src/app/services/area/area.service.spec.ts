import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AreaService } from './area.service';

describe('AreaService', () => {
  let service: AreaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AreaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista con paginacion y omite el filtro vacio', () => {
    service
      .findList({ nombre: '  ' }, { page: 1, size: 10, orderBy: 'nombre', orderType: 'ASC' })
      .subscribe((response) => expect(response.totalElements).toBe(1));

    const req = http.expectOne((r) => r.url === '/api/v1/areas');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.has('nombre')).toBe(false);
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('orderBy')).toBe('nombre');
    req.flush({ success: true, data: [], page: 1, size: 10, totalElements: 1, totalPages: 1 });
  });

  it('crea y desenvuelve data de SingleResponse', () => {
    let nombre = '';
    service
      .create({ nombre: 'Cocina', descripcion: null })
      .subscribe((area) => (nombre = area.nombre));

    const req = http.expectOne('/api/v1/areas');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ nombre: 'Cocina', descripcion: null });
    req.flush({
      success: true,
      data: { id: 1, nombre: 'COCINA', descripcion: null, fechaCreacion: '' },
    });

    expect(nombre).toBe('COCINA');
  });

  it('actualiza y elimina por id', () => {
    service.update(7, { nombre: 'Bodega', descripcion: null }).subscribe();
    http.expectOne({ method: 'PUT', url: '/api/v1/areas/7' }).flush({ success: true, data: true });

    service.delete(7).subscribe();
    http
      .expectOne({ method: 'DELETE', url: '/api/v1/areas/7' })
      .flush({ success: true, data: true });
  });
});
