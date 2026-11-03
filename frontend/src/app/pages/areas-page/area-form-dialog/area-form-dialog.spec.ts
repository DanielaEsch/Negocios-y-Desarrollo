import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { ApiError } from '../../../models/api/problem-detail.model';
import { AreaService } from '../../../services/area/area.service';
import { AreaFormDialog } from './area-form-dialog';

describe('AreaFormDialog', () => {
  const create = vi.fn();
  const update = vi.fn();
  const close = vi.fn();

  beforeEach(() => {
    create.mockReset();
    update.mockReset();
    close.mockReset();
  });

  function setup(data = {}) {
    TestBed.configureTestingModule({
      imports: [AreaFormDialog],
      providers: [
        provideTranslateService(),
        { provide: AreaService, useValue: { create, update } },
        { provide: MatDialogRef, useValue: { close } },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    });
    const fixture = TestBed.createComponent(AreaFormDialog);
    fixture.detectChanges();
    return fixture;
  }

  function fill(fixture: ReturnType<typeof setup>, nombre: string): void {
    const input: HTMLInputElement = fixture.nativeElement.querySelector(
      'input[formcontrolname="nombre"]',
    );
    input.value = nombre;
    input.dispatchEvent(new Event('input'));
  }

  function submit(fixture: ReturnType<typeof setup>): void {
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('no llama a la API si el nombre esta en blanco', () => {
    const fixture = setup();
    fill(fixture, '   ');
    submit(fixture);

    expect(create).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('mat-error')).not.toBeNull();
  });

  it('crea con el nombre recortado y cierra con true', () => {
    create.mockReturnValue(of({ id: 1 }));
    const fixture = setup();
    fill(fixture, '  Cocina ');
    submit(fixture);

    expect(create).toHaveBeenCalledWith({ nombre: 'Cocina', descripcion: null });
    expect(close).toHaveBeenCalledWith(true);
  });

  it('muestra en el campo el 409 de nombre duplicado', () => {
    create.mockReturnValue(
      throwError(
        () =>
          new ApiError({
            type: 'urn:buzz-rutinas:problema:negocio',
            title: 'Conflict',
            status: 409,
            detail: 'Ya existe un área activa con ese nombre.',
            code: 1002,
          }),
      ),
    );
    const fixture = setup();
    fill(fixture, 'Cocina');
    submit(fixture);

    expect(close).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Ya existe un área activa con ese nombre.');
  });

  it('en edicion usa update con el id del area', () => {
    update.mockReturnValue(of(true));
    const fixture = setup({
      area: { id: 9, nombre: 'BODEGA', descripcion: null, fechaCreacion: '' },
    });
    submit(fixture);

    expect(update).toHaveBeenCalledWith(9, { nombre: 'BODEGA', descripcion: null });
  });
});
