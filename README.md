Proyecto Negocios

## Backend

Java 21 + Spring Boot 3.5 (ver `docs/adr/ADR-001-stack-y-arquitectura.md`). Requiere **JDK 21** y Docker.

```
backend/
  rutinas-dominio/    Capa de dominio pura (Usuario, reglas de negocio)
  rutinas-modelo/     Entidades JPA
  rutinas-service/    API REST: web -> facade -> service -> persistence
database/             DDL y datos base
docker-compose.yml    PostgreSQL 14 local
```

```bash
docker compose up -d
cd backend
./mvnw verify                                   # compila y corre las pruebas
./mvnw -pl rutinas-service -am spring-boot:run  # Swagger: http://localhost:8080/swagger-ui.html
```
