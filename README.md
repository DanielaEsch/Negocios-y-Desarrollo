# TaskIn — for Small Business

Sistema de control de rutinas e incidencias para micro y pequeñas empresas. Equipo Buzz.

## Backend

Java 21 + Spring Boot 3.5 (ver `docs/adr/ADR-001-stack-y-arquitectura.md`). Requiere **JDK 21** y Docker.

```
backend/
  rutinas-dominio/    Capa de dominio pura (Usuario, reglas de negocio)
  rutinas-modelo/     Entidades JPA
  rutinas-service/    API REST: web -> facade -> service -> persistence
frontend/             Angular 20 + Angular Material (ver frontend/README.md)
database/             DDL y datos base
docker-compose.yml    PostgreSQL 14 local
```

```bash
docker compose up -d
cd backend
./mvnw verify                                   # compila y corre las pruebas
./mvnw install -DskipTests
./mvnw -pl rutinas-service spring-boot:run      # API en http://localhost:8080/api/v1
```

- Swagger UI: <http://localhost:8080/api/v1/api-docs>
- Errores en formato RFC 7807 (`application/problem+json`).

## Frontend

Node.js 22, con el backend arriba:

```bash
cd frontend
npm install
npm start                                       # http://localhost:4200
```

El inicio de sesión es simulado mientras el backend no tenga seguridad.
