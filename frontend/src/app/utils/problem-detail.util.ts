import { AbstractControl, FormGroup } from '@angular/forms';
import { ProblemDetail } from '../models/api/problem-detail.model';

export function isProblemDetail(value: unknown): value is ProblemDetail {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as ProblemDetail).status === 'number' &&
    typeof (value as ProblemDetail).title === 'string'
  );
}

/**
 * Pinta en el formulario los errores por campo que mando el backend (invalid_params).
 * Cada control recibe el error `server` con el mensaje tal cual. Regresa true si al menos
 * un campo coincidio; si no, la pagina debe mostrar `detail` de forma general.
 */
export function applyServerErrors(form: FormGroup, problem: ProblemDetail): boolean {
  let applied = false;
  for (const param of problem.invalid_params ?? []) {
    const control: AbstractControl | null = form.get(param.name);
    if (control) {
      control.setErrors({ ...(control.errors ?? {}), server: param.reason });
      control.markAsTouched();
      applied = true;
    }
  }
  return applied;
}
