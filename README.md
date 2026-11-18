# Control de Rutinas — Equipo Buzz

Sistema de control de rutinas y actividades periódicas para micro y pequeñas
empresas, con seguimiento de incidencias, evidencias con foto y cobro de la suscripcion.

Proyecto de la materia **Negocios Electrónicos y Desarrollo Web** (UNAM).

## Equipo

Rojas Cruz Carlo Emir · Escamilla Champala Daniela · Alessandre Ibañez Contreras ·
Bahena Gorostieta Alejandro · Rodríguez Ruiz Diana Carolina

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 21 · Spring Boot 3.5 · Maven |
| Frontend | Angular 20 + Angular Material |
| Base de datos | PostgreSQL 14 (Docker en local) |

## Prerrequisitos

- **JDK 21, no otro.** Lombok falla en silencio con un JDK más nuevo
  (cientos de `cannot find symbol` que no son errores tuyos).
- Docker (para PostgreSQL). Maven no hace falta: se usa `./mvnw`.
- Node.js 22 (para el frontend; Angular CLI viene en sus dependencias).

## Estructura

```
backend/
  rutinas-modelo/     Entidades JPA (libreria aparte, el nucleo)
  rutinas-service/    API REST (Spring Boot)
    web/              Controladores: traducen HTTP a RequestVO, no deciden nada
    facade/           Fachada por dominio: el controlador solo habla con ella
    service/          Reglas de negocio y transacciones
    persistence/      Repositorios Spring Data
    common/           VOs, constantes, codigos de error, excepciones
    config/           Auditoria, mapeador, Swagger, sesion, tareas programadas
    integration/      Pasarela de pagos y almacenamiento de fotos, detras de una interfaz
backend/rutinas-dominio/  Reglas puras: maquinas de estado, frecuencias (Strategy), usuario, pagos
backend/pasarela-sandbox/ Pasarela de pagos simulada (tokens, cargos y webhooks firmados)
frontend/             TaskIn: Angular 20 + Material (ver frontend/README.md)
database/             DDL y datos base (la fuente de verdad del esquema)
docker-compose.yml    PostgreSQL 14 local
docs/                 ADR 001 y 002, checklist de capas
```

Regla: no se salta ninguna capa. El controlador nunca toca el servicio ni el repositorio.

## Como levantarlo

```bash
docker compose up -d          # PostgreSQL con el esquema y datos base
cd backend
./mvnw verify                 # compila y corre las pruebas (usan H2, no necesitan Docker)
./mvnw install -DskipTests   # instala los modulos para que el servicio los encuentre
./mvnw -pl rutinas-service spring-boot:run
```

Todas las rutas cuelgan de `/api/v1`:

- API: <http://localhost:8080/api/v1/areas> (pide token, ver [Demo](#demo))
- Swagger UI: <http://localhost:8080/api/v1/api-docs>
- Contrato OpenAPI (JSON): <http://localhost:8080/api/v1/api-docs/openapi.json>
- Salud: <http://localhost:8080/api/v1/actuator/health>

Frontend (con el backend arriba):

```bash
cd frontend
npm install
npm start                     # http://localhost:4200, /api va al backend por proxy
```

Para reiniciar la base desde cero: `docker compose down -v && docker compose up -d`.
**Hazlo una vez si ya tenias la base de antes del 15-nov**: el esquema crecio (evidencias, planes,
suscripcion, ordenes de pago y facturas) y PostgreSQL solo corre `database/` cuando el volumen
esta vacio.

### En Windows

Los mismos pasos, en PowerShell y con Docker Desktop abierto:

```powershell
docker compose up -d
cd backend
.\mvnw.cmd install -DskipTests
.\mvnw.cmd -pl rutinas-service spring-boot:run
```

Y en otra terminal: `cd frontend`, `npm install`, `npm start`. Si `mvnw.cmd` dice que no
encuentra Java, revisa que `JAVA_HOME` apunte al JDK 21.

## Demo

Con el perfil `local` (el de siempre) el backend llena la base al arrancar: programa las rondas
de hoy y simula los 6 dias anteriores (rondas hechas, algunas vencidas, incidencias resueltas),
para que el tablero tenga historia. Es idempotente: si el dia ya tiene rondas no lo toca.
Se apaga con `TASKIN_DEMO=false`.

Todas las cuentas usan la contraseña `TaskIn2026` (tambien hay botones en el login):

| Cuenta | Perfil | Que ve |
|---|---|---|
| `ana@taskin.mx` | Operador (Mantenimiento, Recepcion) | Su turno, la lista de cada ronda, sus reportes |
| `luis@taskin.mx` | Operador (Cocina) | Igual que Ana, en su area |
| `jorge@taskin.mx` | Supervisor (las tres areas) | Tablero del dia (hoja de control) e incidencias |
| `laura@taskin.mx` | Administrador | Lo del supervisor mas rutinas, areas, equipo y suscripcion |
| `cobranza@taskin.mx` | Cobranza (Sofia) | Transferencias por validar |

Recorrido sugerido: Ana empieza la ronda que toca, marca las tareas y reporta un problema;
Jorge ve la marca en el tablero, atiende la incidencia y la resuelve; Ana ve la respuesta en
"Mis reportes". En una ronda empezada o hecha, Ana tambien puede subir fotos de evidencia.

### Pagos

El negocio arranca con 14 dias de prueba del plan Negocio. Laura entra a **Suscripcion**, elige
un plan y paga. La pasarela es un *sandbox* que corre dentro del mismo backend
(`/api/v1/sandbox/pasarela`), asi que no hace falta ninguna cuenta ni llave externa.

| Tarjeta de prueba | Resultado |
|---|---|
| `4242 4242 4242 4242` | Aprobada |
| `4000 0000 0000 0002` | Declinada por el banco |
| `4000 0000 0000 9995` | Fondos insuficientes |

Cualquier fecha futura y cualquier CVC. El cupon `BUZZ20` descuenta 20 %.

- **Tarjeta**: el navegador tokeniza con la pasarela, TaskIn crea la orden y cobra con el token. El
  recibo queda en "Procesando" hasta que llega el webhook firmado (1.5 s) y entonces pasa a
  "Pagado" con su CFDI de demostracion (se descarga en XML).
- **Transferencia**: TaskIn genera la orden con CLABE y referencia; Laura pulsa "Ya transferi" y
  Sofia (`cobranza@taskin.mx`) la valida. Ahi se activa el plan y se emite la factura.

En el perfil `local` el *sandbox* manda cada webhook dos veces, para que se vea en el log que el
segundo se ignora (idempotencia). Las decisiones estan en [ADR 002](docs/adr/ADR-002-pagos-y-suscripciones.md)
y el estado de cada capa en el [checklist](docs/checklist-capas.md).

Seguridad: login con JWT (`POST /api/v1/auth/login`, cabecera `Authorization: Bearer`). En
Swagger, el boton *Authorize* recibe el token. El secreto se cambia con `TASKIN_JWT_SECRET`.

## Como agregar un modulo

El modulo de **areas** es el molde. Para uno nuevo (por ejemplo, rutinas):

1. Tabla en `database/01_schema.sql` con las columnas de auditoria y `DN_ACTIVO`.
2. Entidad `RutinaDO extends AuditableDO` en `rutinas-modelo`.
3. `IRutinaRepository` en `persistence/`.
4. VOs en `common/vo/rutina/`, `RutinaConstant` y `RutinaErrorCode` (con su rango de codigos).
5. Mensajes de error en `messages.properties`.
6. `IRutinaService` + `RutinaServiceImpl`, `IRutinaFacade` + `RutinaFacadeImpl`, `RutinaController`.
7. Prueba en `src/test` siguiendo `AreaControllerTest`.

## Contrato de la API

- Un objeto: `{ "success": true, "data": { ... } }`
- Listado: `{ "success": true, "data": [ ... ], "page": 1, "size": 50, "totalElements": 3, "totalPages": 1 }`
- Error: formato RFC 7807 (`application/problem+json`), lo arma `GlobalExceptionHandler`:

```json
{
  "type": "urn:buzz-rutinas:problema:negocio",
  "title": "Conflict",
  "status": 409,
  "detail": "Ya existe un área activa con ese nombre.",
  "instance": "/api/v1/areas",
  "code": 1002,
  "timestamp": "2026-10-22T10:15:00"
}
```

  Si falla la validacion de un DTO, `type` termina en `validacion`, el `title` es
  "Error de Validación en los Datos de Entrada" y se agrega
  `invalid_params: [{ "name": "nombre", "reason": "El nombre es obligatorio." }]`.
- Entrada: los DTOs se validan con Bean Validation (`@NotBlank`, `@Size`, `@Valid`) antes de llegar
  a la fachada, y un campo que el DTO no declara se rechaza con 400.

## Ramas

`main` (estable) ← `develop` (integración) ← `feature/<módulo>`.
Se trabaja sobre `develop`; a `main` solo entra lo que ya funciona.
