# Database Security Architecture

## Overview

This document describes the database security model, role separation, and secret management approach for the Mediscan backend application.

## Role Separation Model

The system enforces strict separation between **migration** and **runtime** database identities:

### Migration User (`app_migrator`)

**Purpose**: Executes Flyway schema migrations during deployment

**Permissions**:
- `CREATE`, `ALTER`, `DROP` (DDL operations)
- `CREATE INDEX`, `DROP INDEX`
- `SELECT`, `INSERT`, `UPDATE`, `DELETE` (for migration data seeding if needed)

**Usage**:
- Used only during deployment/migration runs
- Never used by runtime application
- Credentials injected via `MYSQL_MIGRATOR_USER` and `MYSQL_MIGRATOR_PASSWORD`

**Security Posture**:
- Higher privilege required for schema evolution
- Isolated to deployment pipeline
- Cannot be accessed by runtime application code

### Runtime Application User (`app_runtime`)

**Purpose**: Executes application business logic and data operations

**Permissions**:
- `SELECT`, `INSERT`, `UPDATE`, `DELETE` (DML operations only)
- **Cannot** `CREATE`, `ALTER`, `DROP`, `GRANT`, `REVOKE`

**Usage**:
- Used by Spring Boot application at runtime
- Credentials injected via `MYSQL_USER` and `MYSQL_PASSWORD`

**Security Posture**:
- Least privilege principle enforced
- Cannot mutate schema structure
- Cannot escalate privileges
- Cannot create or modify database users

## Secret Injection

### Environment Variables

All database credentials are injected via environment variables. **No hardcoded secrets exist in the repository.**

#### Required Runtime Variables:
- `MYSQL_URL` - MySQL connection URL
- `MYSQL_USER` - Runtime database username
- `MYSQL_PASSWORD` - Runtime database password
- `MONGO_URI` - MongoDB connection URI
- `JWT_SECRET` - JWT signing secret

#### Required Migration Variables:
- `MYSQL_URL` - MySQL connection URL (same as runtime)
- `MYSQL_MIGRATOR_USER` - Migration database username
- `MYSQL_MIGRATOR_PASSWORD` - Migration database password

### Secret Sources

Secrets are injected via:

1. **CI/CD Pipeline**: Secrets stored in pipeline secret management (GitHub Secrets, GitLab CI/CD variables, etc.)
2. **Docker Runtime**: Via `docker run -e` or `docker-compose` environment variables
3. **Kubernetes**: Via `Secret` resources mounted as environment variables
4. **Cloud Platforms**: Via managed secret services (AWS Secrets Manager, Azure Key Vault, GCP Secret Manager)

### Development Environment

For local development, use a `.env` file (not committed to repository):

```bash
MYSQL_URL=jdbc:mysql://localhost:3306/mediscan
MYSQL_USER=app_runtime
MYSQL_PASSWORD=<local-dev-password>
MYSQL_MIGRATOR_USER=app_migrator
MYSQL_MIGRATOR_PASSWORD=<local-dev-password>
MONGO_URI=mongodb://user:password@localhost:27017/mediscan?authSource=admin
JWT_SECRET=<generate-strong-secret>
```

**Never commit `.env` files or hardcode secrets in `docker-compose.yml` for production.**

## Configuration Profiles

### Runtime Profile (default)

**File**: `application.yml`

- `spring.flyway.enabled=false` - Flyway disabled at runtime
- `spring.jpa.hibernate.ddl-auto=validate` - Hibernate validates schema only
- Uses runtime database credentials

**Purpose**: Prevents schema mutation during application execution

### Migration Profile

**File**: `application-migration.yml`

- `spring.flyway.enabled=true` - Flyway enabled for migrations
- `spring.jpa.hibernate.ddl-auto=none` - Hibernate does not interact with schema
- Uses migration database credentials

**Usage**: Activated via `--spring.profiles.active=migration` during migration runs

## Execution Separation

### Option A: Separate Migration Job (Recommended)

**Migration Run**:
```bash
java -jar app.jar --spring.profiles.active=migration \
  -DMYSQL_MIGRATOR_USER=app_migrator \
  -DMYSQL_MIGRATOR_PASSWORD=<secret>
```

**Runtime Application**:
```bash
java -jar app.jar \
  -DMYSQL_USER=app_runtime \
  -DMYSQL_PASSWORD=<secret>
```

### Option B: Docker Compose Separation

**Migration Container**:
```yaml
migration:
  image: mediscan-backend:latest
  profiles: ["migration"]
  environment:
    SPRING_PROFILES_ACTIVE: migration
    MYSQL_MIGRATOR_USER: ${MYSQL_MIGRATOR_USER}
    MYSQL_MIGRATOR_PASSWORD: ${MYSQL_MIGRATOR_PASSWORD}
```

**Runtime Container**:
```yaml
backend:
  image: mediscan-backend:latest
  environment:
    MYSQL_USER: ${MYSQL_USER}
    MYSQL_PASSWORD: ${MYSQL_PASSWORD}
```

## Security Benefits

### 1. Least Privilege Enforcement

Runtime application cannot:
- Modify schema structure
- Create or drop tables
- Escalate database privileges
- Create new database users

**Impact**: Even if application code is compromised, attacker cannot mutate database structure.

### 2. Privilege Escalation Prevention

- Migration credentials are not accessible to runtime application
- Runtime credentials cannot execute DDL operations
- Clear separation of concerns prevents privilege confusion

**Impact**: Attack surface is minimized to data operations only.

### 3. Audit Trail Clarity

- Migration operations are isolated and traceable
- Runtime operations use restricted credentials
- Database audit logs can distinguish migration vs runtime activity

**Impact**: Security incidents are easier to investigate and attribute.

### 4. Compliance Alignment

- Follows principle of least privilege (required by many compliance frameworks)
- Separation of duties (migration vs runtime)
- No hardcoded secrets (required by security standards)

**Impact**: System aligns with SOC 2, ISO 27001, HIPAA (if applicable) requirements.

## Production Safety Defaults

### Flyway Clean Disabled

```yaml
spring.flyway.clean-disabled=true
```

Prevents accidental database wipe via `flyway clean` command.

### Hibernate Validation Mode

```yaml
spring.jpa.hibernate.ddl-auto=validate
```

Application fails at startup if schema does not match entities. Prevents silent schema drift.

### No Default Credentials

All credentials must be provided via environment variables. Application will fail to start if secrets are missing.

## Database User Creation

**Database users are NOT created by the application.**

Users must be created by:
- Database administrator (DBA)
- Infrastructure as Code (Terraform, CloudFormation, etc.)
- Cloud platform managed databases (RDS, Cloud SQL, etc.)
- Initialization scripts run by infrastructure team

Example SQL (run by DBA/infrastructure):

```sql
-- Migration user
CREATE USER 'app_migrator'@'%' IDENTIFIED BY '<strong-password>';
GRANT CREATE, ALTER, DROP, INDEX, SELECT, INSERT, UPDATE, DELETE ON mediscan.* TO 'app_migrator'@'%';

-- Runtime user
CREATE USER 'app_runtime'@'%' IDENTIFIED BY '<strong-password>';
GRANT SELECT, INSERT, UPDATE, DELETE ON mediscan.* TO 'app_runtime'@'%';

FLUSH PRIVILEGES;
```

## Verification Checklist

- [ ] No `CREATE USER` statements in Flyway migrations
- [ ] No `GRANT` statements in Flyway migrations
- [ ] No hardcoded passwords in `application.yml`
- [ ] No hardcoded passwords in `docker-compose.yml` (production)
- [ ] `spring.flyway.enabled=false` in runtime config
- [ ] `spring.jpa.hibernate.ddl-auto=validate` in runtime config
- [ ] `spring.flyway.clean-disabled=true` in all configs
- [ ] Migration and runtime credentials are separate
- [ ] All secrets injected via environment variables
- [ ] Application fails to start if secrets are missing

## Migration Files

All Flyway migrations contain **only** schema definitions:
- `CREATE TABLE`
- `ALTER TABLE`
- `ADD CONSTRAINT`
- `CREATE INDEX`

No user management or privilege operations exist in migration files.
