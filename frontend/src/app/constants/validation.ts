/**
 * Largos maximos por campo. Coinciden con el DDL y con las anotaciones @Size del backend,
 * asi el formulario avisa antes de que la API responda 400.
 */
export const AreaLimits = {
  nombre: 100,
  descripcion: 250,
} as const;
