/**
 * Constantes generales de la aplicacion.
 */
export const AppConfig = {
  /** Idioma por defecto y los disponibles en public/i18n. */
  defaultLang: 'es',
  langs: ['es', 'en'],

  /** Paginacion: el backend acepta size de 1 a 100 y page empieza en 1. */
  pageSizeOptions: [10, 20, 50],
  defaultPageSize: 10,

  /** Claves de almacenamiento del navegador. */
  storage: {
    session: 'taskin.session',
    theme: 'taskin.theme',
    lang: 'taskin.lang',
  },

  /** Retardo de busqueda al escribir en un filtro (ms). */
  searchDebounce: 350,
} as const;
