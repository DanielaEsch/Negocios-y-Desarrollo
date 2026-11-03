# TaskIn — frontend

Angular 20 (componentes *standalone* y signals) con Angular Material 3. Consume la API
del backend en `/api/v1`.

## Cómo levantarlo

Requiere Node.js 22 y el backend corriendo en `localhost:8080` (ver el README raíz).

```bash
cd frontend
npm install
npm start            # http://localhost:4200
```

`proxy.conf.json` manda `/api` al backend, así que el navegador no hace CORS. El login es
**simulado** mientras el backend no tenga seguridad: cualquier correo y una contraseña de
8 caracteres entran con el perfil que se elija.

| Comando | Para qué |
|---|---|
| `npm test` | Pruebas unitarias con Vitest (builder oficial `@angular/build:unit-test`) |
| `npm run test:coverage` | Pruebas con reporte de cobertura en `coverage/` |
| `npm run lint` | ESLint (angular-eslint), incluye reglas de accesibilidad en plantillas |
| `npm run format` | Prettier sobre `src/` |
| `npm run build` | Compilación de producción en `dist/taskin/browser` |
| `npm run docs` | Documentación del código con Compodoc en `docs/compodoc` |

## Estructura

```
src/
  environments/        apiUrl y rutas de la API (endpoints.ts)
  app/
    constants/         Configuración, menú, límites de validación (iguales al DDL)
    models/            Interfaces: respuestas de la API, ProblemDetail, áreas, sesión, tablas
    services/
      consume/         Único punto de salida HTTP (limpia parámetros vacíos)
      interceptor/     Headers de trazabilidad, barra de carga y errores RFC 7807
      auth/ guards/    Sesión simulada, authGuard, guestGuard y roleGuard
      area/            Acceso a /api/v1/areas
    core/store/        Estado por pantalla con signals (BaseStore, AreasStore)
    components/        Piezas reutilizables: layout, tabla, diálogos, encabezado
    pages/             Una carpeta por pantalla, con sus rutas (carga diferida)
    utils/             Utilidades (aplicar invalid_params a un formulario)
public/i18n/           Textos en español e inglés (ngx-translate)
nginx/                 Configuración del contenedor
```

## Reglas

- **Capas.** La página no llama a `HttpClient`: usa su store o su servicio, y el servicio
  usa `ConsumeService`. Las URLs salen de `environments/endpoints.ts`.
- **Errores.** El `errorInterceptor` convierte toda respuesta de error en `ApiError` con su
  `ProblemDetail`. Los 400 con `invalid_params` se pintan en el campo correspondiente
  (`applyServerErrors`); los 5xx y la falta de conexión los avisa el interceptor.
- **Textos.** Ningún texto fijo en las plantillas: todo pasa por `| translate` y vive en
  `public/i18n/es.json` y `en.json`.
- **Tablas.** Cada página describe columnas y acciones con un `LayoutConfig` y las pinta
  `app-table-component`, con paginación y orden del lado del servidor.
- **Perfiles.** Operador consulta; supervisor además crea y edita; administrador además
  elimina. El menú y los botones leen el rol de `AuthService`.

## Agregar un módulo

El de **áreas** es el molde:

1. Modelo en `models/<modulo>/` y ruta en `environments/endpoints.ts`.
2. Servicio en `services/<modulo>/` con `ConsumeService`.
3. Store en `core/store/` si la pantalla lista y pagina.
4. Página en `pages/<modulo>-page/` con su `<modulo>.routes.ts`, y la ruta en `app.routes.ts`.
5. Textos en los dos JSON de `public/i18n/` y la opción en `constants/menu.ts`.
6. Pruebas `*.spec.ts` junto al código.

## Contenedor

```bash
docker build -t taskin-frontend .
docker run -p 8088:80 -e BACKEND_URL=http://host.docker.internal:8080 taskin-frontend
```

nginx sirve la aplicación, manda `/api/` a `BACKEND_URL` y agrega cabeceras de seguridad
(CSP, `X-Frame-Options`, `nosniff`).
