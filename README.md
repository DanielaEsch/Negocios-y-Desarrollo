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
- Node.js y Angular CLI (para el frontend, cuando exista).

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
database/             DDL y datos base (la fuente de verdad del esquema)
docker-compose.yml    PostgreSQL 14 local
```

Regla: no se salta ninguna capa. El controlador nunca toca el servicio ni el repositorio.

## Como levantarlo

```bash
docker compose up -d          # PostgreSQL con el esquema y datos base
cd backend
./mvnw verify                 # compila y corre las pruebas (usan H2, no necesitan Docker)
./mvnw -pl rutinas-service -am spring-boot:run
```

- API: <http://localhost:8080/areas>
- Swagger: <http://localhost:8080/swagger-ui.html>
- Salud: <http://localhost:8080/actuator/health>

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
- Error: `{ "success": false, "code": 1002, "message": "Ya existe un área activa con ese nombre.", "timestamp": "..." }`

## Ramas

`main` (estable) ← `develop` (integración) ← `feature/<módulo>`.
Se trabaja sobre `develop`; a `main` solo entra lo que ya funciona.
