# Tasks: Usuarios y Autenticación

**Input**: Design documents from `/specs/001-auth/`
**Prerequisites**: plan.md (required), spec.md (required), data-model.md, contracts/

## Path Conventions

- **Backend**: `backend/src/main/java/com/superlist/`
- **Frontend**: `frontend/SuperListApp/app/src/main/java/com/superlist/app/`
- **Tests**: `backend/src/test/java/com/superlist/`
- **Resources**: `backend/src/main/resources/`

---

## Phase 1: Foundation

**Purpose**: Project structure and shared infrastructure

- [ ] T001 Create Spring Boot project with `pom.xml` (Spring Boot 3.4.4, Java 17, dependencies: spring-boot-starter-web, spring-boot-starter-security, spring-boot-starter-data-jpa, flyway-core, flyway-database-postgresql, jjwt-api/impl/jackson 0.12.x, lombok, postgresql, h2 for tests)
- [ ] T002 Create directory structure (controller, model, repository, service, security, config packages)
- [ ] T003 Configure `application.yml` (PostgreSQL datasource, Flyway, JWT secret, server port)
- [ ] T004 Create Flyway migration `V1__create_usuarios.sql`
- [ ] T005 Create Android project structure (gradle, manifest, packages)

---

## Phase 2: Backend

### User Story 1 - Registro y verificación

- [ ] T006 Create `Usuario.java` entity (JPA, Lombok)
- [ ] T007 Create `UsuarioRepository.java` (findByTelefono)
- [ ] T008 Create DTOs: `SolicitarCodigoRequest.java`, `VerificarCodigoRequest.java`, `AuthResponse.java`
- [ ] T009 Create `CodeRateLimiter.java` (in-memory, 3 requests/5min per phone)
- [ ] T010 Create `JwtTokenProvider.java` (generate, validate, extract claims, jjwt 0.12.x, HMAC-SHA256)
- [ ] T011 Create `JwtAuthenticationFilter.java` (OncePerRequestFilter, extract token, validate, set auth)
- [ ] T012 Create `SecurityConfig.java` (stateless, permit auth endpoints, require auth for rest)
- [ ] T013 Create `AuthService.java` (solicitarCodigo, verificarCodigo, getPerfil, updatePerfil)
- [ ] T014 Create `AuthController.java` (solicitar-codigo, verificar-codigo, perfil GET/PUT)
- [ ] T015 Create tests for AuthService (code generation, verification, expiration, rate limiting)

### User Story 2 - Perfil

- [ ] T016 Create DTOs: `UsuarioPerfilRequest.java`, `UsuarioPerfilResponse.java`
- [ ] T017 Add validation for nombre (not empty)
- [ ] T018 Create tests for profile endpoints

---

## Phase 3: Frontend Android

- [ ] T019 Create `AuthApi.kt` (Retrofit interface: solicitarCodigo, verificarCodigo, getPerfil, updatePerfil)
- [ ] T020 Create `TokenManager.kt` (save/load/clear JWT from SharedPreferences)
- [ ] T021 Create `AuthRepository.kt` (wraps AuthApi + TokenManager, exposes Result-based methods)
- [ ] T022 Create `NetworkModule.kt` (Retrofit singleton + OkHttp JWT interceptor)
- [ ] T023 Create `LoginViewModel.kt` (states: phone entry, code entry, success/error)
- [ ] T024 Create `LoginActivity.kt` (phone input → solicitar código)
- [ ] T025 Create `VerificationActivity.kt` (code input → verificar → save token → navigate)
- [ ] T026 Create `ProfileActivity.kt` (show/edit nombre, logout)
- [ ] T027 Create XML layouts: `activity_login.xml`, `activity_verification.xml`, `activity_profile.xml`
- [ ] T028 Update `AndroidManifest.xml` (activities, permissions, no splash)
