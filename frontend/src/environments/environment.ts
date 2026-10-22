/**
 * Configuracion de produccion. En desarrollo Angular la reemplaza por
 * environment.development.ts (fileReplacements en angular.json).
 *
 * La API se llama con ruta relativa: en desarrollo la redirige proxy.conf.json y
 * en el contenedor la redirige nginx, asi el navegador nunca hace CORS.
 */
export const environment = {
  production: true,
  appName: 'TaskIn',
  apiUrl: '/api/v1',
};
