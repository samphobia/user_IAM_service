# User IAM Service

Production-oriented IAM service built with Spring Boot, PostgreSQL, JWT, RBAC, and merchant multi-tenancy.

## Stack

- Java 21
- Spring Boot 3.3.5
- Spring Security (stateless JWT)
- Spring Data JPA (Hibernate)
- PostgreSQL
- Flyway
- Maven

## Architecture

Layered architecture is enforced:

- Controller -> Service -> Repository
- No business logic in controllers
- No direct database access outside repositories

Package layout:

- `com.company.iam.config`
- `com.company.iam.auth`
- `com.company.iam.controllers`
- `com.company.iam.entities`
- `com.company.iam.entities.enums`
- `com.company.iam.user`
- `com.company.iam.merchant`
- `com.company.iam.role`
- `com.company.iam.security`
- `com.company.iam.dto`
- `com.company.iam.repository`
- `com.company.iam.service`
- `com.company.iam.exception`
- `com.company.iam.util`

## Security Model

- BCrypt password hashing with strength 12
- JWT includes `sub` (user_id), `role`, and `merchant_id`
- Stateless security (`SessionCreationPolicy.STATELESS`)
- RBAC via `@PreAuthorize`

## Multi-Tenancy

- `merchant_id` claim is extracted in `JwtAuthenticationFilter`
- `MerchantContextHolder` carries user and merchant context per request
- Merchant-scoped queries use repository filters (for example `findByIdAndMerchant_MerchantId`)

## AAAS Integration

This service can run in two modes:

- Independent mode: local IAM only
- Integrated mode: local IAM + outbound sync calls to KeyAuth AAAS for register/login

Properties:

- `APP_AAAS_ENABLED` (`true|false`)
- `APP_AAAS_FAIL_OPEN` (`true|false`)
- `APP_AAAS_BASE_URL`
- `APP_AAAS_API_KEY`

When `APP_AAAS_ENABLED=true`, register/login invoke AAAS endpoints and continue locally if `APP_AAAS_FAIL_OPEN=true`.

## Required Environment Variables

- `APP_JWT_SECRET` (Base64-encoded secret, mandatory)
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Optional:

- `SERVER_PORT` (default `8090`)
- `APP_JWT_EXPIRATION_MS` (default `3600000`)

## Endpoints

### Auth

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/verify-email`
- `POST /verify-email`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`

### Merchant

- `POST /api/v1/merchants/register`
- `GET /api/v1/merchants/me` (MERCHANT_ADMIN, MERCHANT_MANAGER, MERCHANT_CASHIER)
- `GET /api/v1/merchants/{merchantId}` (OPERATOR_ADMIN)

### User

- `GET /api/v1/users/{userId}` (merchant-scoped, MERCHANT_ADMIN, MERCHANT_MANAGER, MERCHANT_CASHIER)

## Build and Run

```bash
mvn clean test
mvn spring-boot:run
```
