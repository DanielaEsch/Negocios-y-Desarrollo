import { FormControl, FormGroup } from '@angular/forms';
import { applyServerErrors, isProblemDetail } from './problem-detail.util';

describe('problem-detail.util', () => {
  it('reconoce un ProblemDetail', () => {
    expect(isProblemDetail({ title: 'Conflict', status: 409 })).toBe(true);
    expect(isProblemDetail({ success: false })).toBe(false);
    expect(isProblemDetail(null)).toBe(false);
  });

  it('pinta invalid_params en los controles que existen', () => {
    const form = new FormGroup({ nombre: new FormControl(''), descripcion: new FormControl('') });

    const applied = applyServerErrors(form, {
      type: 'urn:buzz-rutinas:problema:validacion',
      title: 'Error de Validación en los Datos de Entrada',
      status: 400,
      invalid_params: [
        { name: 'nombre', reason: 'El nombre es obligatorio.' },
        { name: 'noExiste', reason: 'x' },
      ],
    });

    expect(applied).toBe(true);
    expect(form.controls.nombre.getError('server')).toBe('El nombre es obligatorio.');
    expect(form.controls.nombre.touched).toBe(true);
    expect(form.controls.descripcion.errors).toBeNull();
  });

  it('regresa false si ningun campo coincide', () => {
    const form = new FormGroup({ nombre: new FormControl('') });
    expect(applyServerErrors(form, { type: 'x', title: 'Bad Request', status: 400 })).toBe(false);
  });
});
