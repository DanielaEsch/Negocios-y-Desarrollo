# Control de Rutinas — Equipo Buzz

Sistema de control de rutinas y actividades periódicas para micro y pequeñas
empresas, con seguimiento de incidencias y evidencias.

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
    config/           Auditoria, mapeador, Swagger
frontend/             TaskIn: Angular 20 + Material (ver frontend/README.md)
database/             DDL y datos base (la fuente de verdad del esquema)
docker-compose.yml    PostgreSQL 14 local
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

- API: <http://localhost:8080/api/v1/areas>
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
