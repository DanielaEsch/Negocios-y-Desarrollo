# ADR 001: Selección del Stack Tecnológico y Patrón de Arquitectura

* **Estado**: Aceptado (actualizado el 2026-11-15 con lo construido en los sprints 2 y 3)
* **Fecha**: 2026-09-17
* **Squad**: Equipo Buzz
* **Autores**: Alessandre Ibañez Contreras · Rojas Cruz Carlo Emir · Escamilla Champala Daniela · Bahena Gorostieta Alejandro · Rodríguez Ruiz Diana Carolina

## 1. Contexto y Problema
Tras las entrevistas de validación de mercado, identificamos que nuestra plataforma necesita resolver el control de las rutinas y actividades periódicas por área en micro y pequeñas empresas, y el seguimiento de las incidencias que ocurren en ellas. Requerimos una arquitectura web que nos permita desarrollar ágilmente, garantizar el desacoplamiento entre la lógica de negocio y la infraestructura, y facilitar la integración con servicios externos (pasarela de pagos y almacenamiento de archivos de evidencia).

## 2. Decisión
Hemos decidido adoptar las siguientes tecnologías y patrones para el desarrollo del proyecto:

* **Lenguaje y Framework Backend**: Java 21 con Spring Boot 3.5
* **Framework Frontend**: Angular 20 con Angular Material (componentes *standalone* y *signals*)
* **Base de Datos Principal**: PostgreSQL 14
* **Patrón de Arquitectura**: MVC desacoplado por capas (web, facade, service, persistence), con el modelo de datos en una librería aparte
* **Capa de dominio pura** (`rutinas-dominio`): las reglas de negocio y las máquinas de estado (ronda, incidencia, orden de pago) viven en clases sin Spring ni base de datos, y se prueban solas.
* **Contrato de la API**: REST nivel 2 de Richardson bajo `/api/v1`, DTOs validados con Bean Validation, errores en formato RFC 7807 y contrato publicado con OpenAPI (Swagger UI).
* **Seguridad**: inicio de sesión con JWT propio y perfiles por endpoint (`OPERADOR`, `SUPERVISOR`, `ADMINISTRADOR`, `COBRANZA`); contraseñas con BCrypt.
* **Integraciones**: detrás de una interfaz en el paquete `integration/` (pasarela de pagos, almacenamiento de fotos), para cambiar de proveedor sin tocar los servicios. Los pagos se deciden en el [ADR 002](ADR-002-pagos-y-suscripciones.md).
* **Calidad**: pruebas automáticas (dominio, integración con H2 y frontend con Vitest) en GitHub Actions en cada *push* a `develop` y en cada *pull request*.

## 3. Alternativas Consideradas
* **Opción A (Descartada)**: Monolito con Django o Laravel — *Motivo de descarte*: acopla la vista al modelo y dificulta exponer APIs para la app móvil en sprints posteriores.
* **Opción B (Descartada)**: MySQL — *Motivo de descarte*: requeríamos tipos JSON nativos, índices parciales y mejor soporte para concurrencia, que ofrece PostgreSQL.
* **Opción C (Descartada)**: Spring Security con Keycloak — *Motivo de descarte*: un servidor de identidad aparte es demasiada infraestructura para una MiPyME; un JWT propio con un interceptor cubre los cuatro perfiles.

## 4. Consecuencias
* **Positivas**:
  * Separación clara de responsabilidades entre capas.
  * Curva de aprendizaje alineada con la experiencia previa del *squad*.
  * Facilidad para crear pruebas unitarias sobre el dominio sin depender de la base de datos (67 pruebas de dominio al cierre del sprint 3).
  * El frontend y el backend se construyen, prueban y despliegan por separado.
* **Riesgos / Limitaciones**:
  * Mayor tiempo inicial invertido en la configuración de la estructura (*scaffolding*).
  * Necesidad de mantener disciplinadamente las reglas de importación entre capas.
  * La seguridad propia no tiene renovación de token ni cierre de sesión en el servidor; si el producto crece, conviene migrar a Spring Security.
