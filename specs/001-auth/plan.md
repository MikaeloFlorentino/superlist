# Implementation Plan: Usuarios y Autenticación

**Branch**: `001-auth` | **Date**: 2025-01-01 | **Spec**: `specs/001-auth/spec.md`
**Input**: Feature specification from `/specs/001-auth/spec.md`

## Summary

Sistema de autenticación por teléfono con verificación SMS. Los usuarios se registran proporcionando su número, reciben un código de 6 dígitos (logueado en consola en MVP), lo verifican y obtienen un JWT para autenticación stateless. Incluye rate limiting, expiración de códigos y JWT, y gestión básica de perfil.

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3.4.4, Spring Security 6, jjwt 0.12.x, Lombok, Flyway
**Storage**: PostgreSQL 15+
**Testing**: JUnit 5, Mockito
**Target Platform**: Android (frontend) + Spring Boot REST API (backend)
**Project Type**: Web API (backend) + Mobile App (frontend)
**Performance Goals**: < 500ms response time for auth endpoints
**Constraints**: Rate limiting (3 requests/5min per phone), code expiration (10 min), JWT expiration (7 days)
**Scale/Scope**: MVP — single API, no SSO, no OAuth

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- ✅ JWT stateless auth (no session state on server)
- ✅ Phone as unique identifier (E.164 format)
- ✅ Rate limiting on code requests
- ✅ All protected endpoints require JWT via Authorization header
- ✅ No password storage needed (code-based auth only)

## Project Structure

### Documentation (this feature)

```text
specs/001-auth/
├── spec.md              # Feature specification
├── plan.md              # This file
├── data-model.md        # Entities
├── quickstart.md        # Validation scenarios
├── contracts/
│   └── rest.md          # API contracts
└── tasks.md             # Executable tasks
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/superlist/
│   ├── controller/
│   │   └── AuthController.java
│   ├── model/
│   │   ├── Usuario.java
│   │   └── dto/
│   │       ├── SolicitarCodigoRequest.java
│   │       ├── VerificarCodigoRequest.java
│   │       ├── AuthResponse.java
│   │       ├── UsuarioPerfilRequest.java
│   │       └── UsuarioPerfilResponse.java
│   ├── repository/
│   │   └── UsuarioRepository.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── SecurityConfig.java
│   └── service/
│       └── AuthService.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       └── V1__create_usuarios.sql
└── pom.xml

frontend/SuperListApp/
└── app/src/main/java/com/superlist/app/
    ├── data/api/
    │   └── AuthApi.kt
    ├── data/local/
    │   └── TokenManager.kt
    ├── data/repository/
    │   └── AuthRepository.kt
    ├── ui/login/
    │   ├── LoginActivity.kt
    │   ├── VerificationActivity.kt
    │   ├── ProfileActivity.kt
    │   └── LoginViewModel.kt
    └── util/
        └── NetworkModule.kt
```

**Structure Decision**: Backend (Spring Boot) + Frontend (Android). Auth feature is self-contained in both layers. The JWT filter intercepts all `/api/**` requests except auth endpoints.

## Complexity Tracking

N/A — No constitution violations.
