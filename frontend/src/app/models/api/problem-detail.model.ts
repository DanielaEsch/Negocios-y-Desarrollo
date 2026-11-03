/**
 * Error de la API en formato RFC 7807 (application/problem+json).
 */
export interface InvalidParam {
  name: string;
  reason: string;
}

export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail?: string;
  instance?: string;
  timestamp?: string;
  /** Codigo propio del backend (messages.properties), solo en errores de negocio. */
  code?: number;
  /** Un elemento por campo invalido, solo en errores de validacion. */
  invalid_params?: InvalidParam[];
}

/**
 * Error que reciben los servicios y paginas despues del errorInterceptor: siempre trae
 * un ProblemDetail, aunque el servidor no haya respondido.
 */
export class ApiError extends Error {
  constructor(readonly problem: ProblemDetail) {
    super(problem.detail ?? problem.title);
    this.name = 'ApiError';
  }

  get status(): number {
    return this.problem.status;
  }
}
