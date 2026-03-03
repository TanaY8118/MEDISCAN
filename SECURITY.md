# Security Model

This document outlines the security architecture and best practices for the Medicine Management System.

## 1. Schema Management & Database Security

### Schema Authority
- **Flyway** is the single source of truth for the database schema.
- Hibernate/JPA schema mutation (`ddl-auto`) is set to `validate` in production to prevent silent schema drift.
- Migrations are versioned (e.g., `V001__baseline_schema.sql`) and stored in `src/main/resources/db/migration`.
- Runtime application does **not** have permission to alter the schema.
- **Legacy Migrations**: Older migrations have been consolidated into `V001` to ensure a clean, deterministic baseline.

### Migration Isolation
- Migrations should run in a separate process or init container using the `migration` profile.
- Defined in `application-migration.yml`.
- Migrations use elevated privileges (CREATE, ALTER, DROP), while the runtime application uses a least-privilege user (INSERT, UPDATE, DELETE, SELECT).

## 2. Secrets Management

### Environment Variables
- No hardcoded credentials in source control (`application.yml`).
- Secrets are injected via environment variables (e.g., `MYSQL_PASSWORD`, `JWT_SECRET`).
- See `.env.example` for required variables.

### Credentials
- **Runtime User**: Limited permissions (CRUD only).
- **Migration User**: Owner permissions (DDL allowed).

## 3. Data Ownership & Isolation

### Multi-Tenancy
- Data is isolated logically at the application level.
- All core entities (`Inventory`, `InventoryLog`, etc.) are owned by a `User` entity via database foreign keys.
- **Enforcement**:
    - **Database**: Foreign Key constraints (`user_id`) ensure referential integrity.
    - **JPA/Hibernate**: Entities use `@ManyToOne(optional = false)` to mandate ownership.
    - **Service Layer**: Operations are scoped to the authenticated user's ID or Username.
    - **Access Control**: Queries filter by `userId` to prevent horizontal privilege escalation (IDOR).

## 4. Authentication & Authorization

- **JWT**: Stateless authentication using JSON Web Tokens.
- **Context**: `SecurityContextHolder` maintains the authenticated user's identity.
- **Authorization**: API endpoints are secured, requiring valid tokens.

## 5. Audit & Logging

- **Inventory Logs**: Immutable, append-only logs track all stock changes (`InventoryLog` entity).
- **Traceability**: Logs include `performedBy` (username), timestamp, and reason.
- **Invariants**: `PreUpdate` and `PreRemove` hooks prevent modification or deletion of logs.
