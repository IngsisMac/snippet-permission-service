# snippet-permission-service

Microservicio del TP2 (**Snippet Playground**) responsable de la gestión de usuarios, ownership, compartir snippets y administración de reglas de linteo y formateo por usuario.

## Responsabilidades

- Control de acceso y propiedad (`Ownership`: `OWNER`, `WRITE`, `READ`).
- Compartir snippets con otros usuarios y revocar accesos.
- `RulesService`: CRUD de reglas de formateo y linteo con versionado estricto (`rules_version`) para fan-out asincrónico.
- OAuth2 Resource Server descentralizado validando JWTs emitidos por Auth0.

## Tecnologías

- **Kotlin 2.0** + **Java 21**.
- **Spring Boot 3.3** (Web, Data JPA, Security OAuth2 Resource Server).
- **PostgreSQL** + **Flyway** (migraciones de schema).
- **SpringDoc OpenAPI 3** (Swagger UI).
- **Testcontainers** (PostgreSQL) para tests de integración.

## Ejecución Local

```bash
./gradlew bootRun
```

Para correr las pruebas unitarias y de integración:

```bash
./gradlew check
```
