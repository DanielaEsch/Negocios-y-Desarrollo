# Checklist de capas — TaskIn for Small Business

Revisión al 15-nov-2026 contra los apuntes de la materia: arquitectura web moderna,
pruebas y GitHub Actions, triángulo SCM / CRM / ERP y pagos y monetización.

Leyenda: **[x]** hecho · **[~]** parcial · **[ ]** pendiente · **N/A** no aplica (con motivo).

## 1. Arquitectura web moderna (desacoplamiento, REST, DTOs, contratos)

| | Punto de la lección | Dónde está |
|---|---|---|
| [x] | Capa de presentación separada de la de negocio | `frontend/` (Angular) y `backend/` (Spring Boot); se comunican solo por la API |
| [~] | Frontend y backend en repositorios separados | Mismo repositorio, pero cada uno con su build, sus pruebas y su job de CI |
| [x] | El cliente nunca toca la base de datos | El navegador solo conoce `/api/v1`; nginx hace de proxy |
| [x] | Recursos por entidad (Richardson nivel 1) | `/areas`, `/usuarios`, `/rutinas`, `/ejecuciones`, `/incidencias`, `/tablero`, `/pagos` |
| [x] | Verbos y códigos HTTP (nivel 2) | GET, POST, PUT, PATCH y DELETE; 201, 202, 400, 401, 403, 404, 409, 413 y 502 |
| N/A | HATEOAS (nivel 3) | Decisión del taller de contrato: nivel 2 es suficiente para un solo cliente |
| [x] | DTOs: solo lo necesario viaja por la red | `common/vo/*`; las entidades `*DO` nunca salen del servicio; el hash de la contraseña no se expone |
| [x] | Contrato antes de programar | Taller de contrato de API, Swagger UI en `/api/v1/api-docs`, ejemplos de error por endpoint |
| [x] | Errores uniformes | `GlobalExceptionHandler` con `ProblemDetail` (RFC 7807) y `code` propio |
| [~] | Merge protegido en GitHub | Ver la sección 8 |

## 2. Capas del backend

| | Capa | Paquete o módulo |
|---|---|---|
| [x] | Web (controladores) | `rutinas-service/.../web` |
| [x] | Fachada | `.../facade` |
| [x] | Servicio (reglas y transacciones) | `.../service` |
| [x] | Persistencia | `.../persistence` (Spring Data JPA) |
| [x] | Dominio puro | `rutinas-dominio` (sin Spring ni base de datos) |
| [x] | Modelo de datos | `rutinas-modelo` (entidades JPA con auditoría) |
| [x] | Integraciones externas | `.../integration` (pasarela de pagos, almacenamiento de fotos) |
| [x] | Esquema versionado | `database/01_schema.sql` y `02_datos_base.sql` |
| [x] | Trabajos programados | `ProgramacionJobs`: programa las rondas, marca las vencidas y vence las órdenes de pago |

## 3. Capas del frontend

| | Punto | Dónde está |
|---|---|---|
| [x] | Páginas por perfil | Operador: turno, ronda, reportes. Supervisor: tablero, incidencias. Admin: rutinas, áreas, equipo, suscripción. Cobranza: validación de pagos |
| [x] | Servicios HTTP centralizados | `services/*`, con interceptores de cabeceras, carga y errores |
| [x] | Guardas por perfil | `app.routes.ts` |
| [x] | Idiomas | ngx-translate, `public/i18n/es.json` y `en.json` |
| [x] | Seguridad del servidor web | nginx con CSP y cabeceras de seguridad |

## 4. Pruebas y calidad (lección de dominio y GitHub Actions)

| | Punto | Estado |
|---|---|---|
| [x] | Pruebas unitarias del dominio | 67 pruebas (`rutinas-dominio`) |
| [x] | Pruebas de integración de la API | 23 pruebas con H2 (`rutinas-service`) y 9 del *sandbox* de pagos |
| [x] | Pruebas del frontend | 40 pruebas con Vitest |
| [x] | CI en cada push y PR | `.github/workflows/ci.yml`: backend y frontend (lint, pruebas y build) |
| [x] | Prueba contra PostgreSQL real | Recorrido completo de pagos, cobranza y evidencias verificado el 15-nov |

## 5. Triángulo empresarial

| | Sistema | Cómo aplica a TaskIn |
|---|---|---|
| [x] | SCM: concurrencia | No hay inventario. La lección aplica al recurso único: una ronda que dos operadores quieren tomar. Bloqueo optimista con `@Version` y respuesta 409 |
| [x] | CRM: módulo en código | Usuarios y perfiles, regla `promoverASupervisor()`, consentimiento LFPDPPP y BCrypt |
| [x] | ERP: flujo financiero | **Ahora sí aplica** (antes era N/A): cobro de la suscripción, IVA 16 %, CFDI 4.0 inmutable y máquinas de estado validadas por el servidor |

## 6. Pagos y monetización

| | Punto de la lección | Dónde está |
|---|---|---|
| [x] | PCI-DSS, *Trust Zero*: tokenización en el navegador | `pasarela.service.ts` habla directo con la pasarela; el backend recibe solo `tok_...` |
| [x] | Nunca guardar la tarjeta | `ORDEN_PAGO` guarda solo la marca y los últimos 4 dígitos |
| [x] | Ciclo universal: token → orden → cobro → confirmación asíncrona | Pantalla de pago (3 pasos) y recibo que espera al webhook |
| [x] | Webhook como fuente de verdad | `POST /pagos/webhook`; el navegador no puede marcar una orden como pagada |
| [x] | Firma criptográfica | HMAC-SHA256 con marca de tiempo (`VerificadorFirmaWebhook`) |
| [x] | Idempotencia | `EVENTO_PAGO` con id único; el *sandbox* duplica avisos para demostrarlo |
| [x] | Máquina de estados de la transacción | `OrdenPago` en el dominio, con sus pruebas |
| [x] | Pasarela en *sandbox* | Módulo `pasarela-sandbox`, tarjetas de prueba 4242 / 0002 / 9995 |
| [x] | B2C con pasarela | Pago con tarjeta |
| [x] | B2B: orden de compra, transferencia, validación manual y CFDI | Orden con CLABE (imprimible a PDF), "Ya transferí", panel de Cobranza con "Validar pago" y CFDI automático |
| [x] | Control de vigencia de la suscripción | Prueba de 14 días, aviso en el menú y renovación |
| N/A | *Payment split* | Un solo vendedor |
| N/A | Publicidad (CPM / CPC) | SaaS B2B sin tráfico que monetizar |

## 7. Funcionalidad del producto

| | Módulo | Estado |
|---|---|---|
| [x] | Áreas, equipo y perfiles | CRUD completo |
| [x] | Rutinas con frecuencias (Strategy) | Diaria, semanal, cada 2 horas y por evento |
| [x] | Rondas (ejecuciones) | Programación diaria, tareas, vencimiento y concurrencia |
| [x] | Incidencias | Reporte, atención y resolución en tres columnas |
| [x] | Evidencias con foto | Hasta 6 fotos por ronda, JPEG/PNG/WEBP de hasta 5 MB |
| [x] | Tablero del supervisor | Rutinas por hora del día |
| [x] | Suscripción, pagos y facturas | Tarjeta, transferencia, cupón, cambio de plan y descarga del CFDI |
| [ ] | Notificaciones (correo o push) | Fuera del alcance del sprint 3 |
| [ ] | Cobro recurrente automático | La renovación se paga a mano (ADR 002) |

## 8. Pendientes de equipo

- [ ] **Merge protegido**: en el repositorio oficial, `main` no tiene regla de protección.
  Quien administra el repositorio (Daniela) debe activar *Require a pull request before
  merging* y *Require approvals* (mínimo 1). En este repositorio privado no se puede:
  GitHub Free no ofrece protección de ramas en repositorios privados.
- [ ] **CI del repositorio oficial**: hoy es de Node (`jest`) y solo corre en `main`;
  hay que cambiarlo por el de Java y Angular de este repositorio.
- [ ] **CFDI con validez fiscal**: requiere contratar un PAC.
