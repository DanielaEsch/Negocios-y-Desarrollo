# ADR 001: Selección del Stack Tecnológico y Patrón de Arquitectura

* **Estado**: Aceptado
* **Fecha**: 2026-09-17
* **Squad**: Equipo Buzz
* **Autores**: Alessandre Ibañez Contreras

## 1. Contexto y Problema
Tras las entrevistas de validación de mercado, identificamos que nuestra plataforma necesita resolver el control de las rutinas y actividades periódicas por área en micro y pequeñas empresas, y el seguimiento de las incidencias que ocurren en ellas. Requerimos una arquitectura web que nos permita desarrollar ágilmente, garantizar el desacoplamiento entre la lógica de negocio y la infraestructura, y facilitar la integración con servicios externos (notificaciones y almacenamiento de archivos de evidencia).

## 2. Decisión
Hemos decidido adoptar las siguientes tecnologías y patrones para el desarrollo del proyecto:

* **Lenguaje y Framework Backend**: Java 21 con Spring Boot 3.5
* **Framework Frontend**: Angular 20 con Angular Material
* **Base de Datos Principal**: PostgreSQL 14
* **Patrón de Arquitectura**: MVC desacoplado por capas (web, facade, service, persistence), con el modelo de datos en una librería aparte

## 3. Alternativas Consideradas
* **Opción A (Descartada)**: Monolito con Django o Laravel — *Motivo de descarte*: acopla la vista al modelo y dificulta exponer APIs para la app móvil en sprints posteriores.
* **Opción B (Descartada)**: MySQL — *Motivo de descarte*: requeríamos tipos JSON nativos, índices parciales y mejor soporte para concurrencia, que ofrece PostgreSQL.

## 4. Consecuencias
* **Positivas**:
  * Separación clara de responsabilidades entre capas.
  * Curva de aprendizaje alineada con la experiencia previa del *squad*.
  * Facilidad para crear pruebas unitarias sobre el dominio sin depender de la base de datos.
* **Riesgos / Limitaciones**:
  * Mayor tiempo inicial invertido en la configuración de la estructura (*scaffolding*).
  * Necesidad de mantener disciplinadamente las reglas de importación entre capas.
