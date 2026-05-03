# Implementation Plan: Familias

**Branch**: `002-familias` | **Date**: 2025-01-01 | **Spec**: `specs/002-familias/spec.md`
**Input**: Feature specification from `/specs/002-familias/spec.md`

## Summary

Sistema de gestión de familias: CRUD básico de familias, invitación de miembros por teléfono, aceptación/rechazo de invitaciones, y unión por código de invitación. Las familias son el contenedor principal para el catálogo de artículos y listas de compras.

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3.4.4, Spring Security 6, JPA, Lombok, Flyway
**Storage**: PostgreSQL 15+ (con tipos ENUM)
**Testing**: JUnit 5, Mockito
**Target Platform**: Android (frontend) + Spring Boot REST API (backend)
**Project Type**: Web API (backend) + Mobile App (frontend)
**Performance Goals**: < 500ms response time
**Constraints**: JWT auth required for all endpoints. Solo ADMIN puede invitar.
**Scale/Scope**: MVP — sin editar/eliminar familias, sin promoción de roles

## Constitution Check

- ✅ JWT stateless auth
- ✅ Roles ADMIN/MIEMBRO
- ✅ Invitaciones con historial (no se eliminan)
- ✅ Código de invitación único global
- ✅ Unión por código sin invitación previa

## Project Structure

### Documentation (this feature)

```text
specs/002-familias/
├── spec.md
├── plan.md
├── data-model.md
├── quickstart.md
├── contracts/rest.md
└── tasks.md
```

### Source Code

```text
backend/src/main/java/com/superlist/
├── controller/
│   └── FamiliaController.java
├── model/
│   ├── Familia.java
│   ├── MiembroFamilia.java
│   ├── Invitacion.java
│   └── dto/
│       ├── CrearFamiliaRequest.java
│       ├── FamiliaResponse.java
│       ├── InvitarRequest.java
│       ├── InvitacionResponse.java
│       ├── UnirseRequest.java
│       └── ...
├── repository/
│   ├── FamiliaRepository.java
│   ├── MiembroFamiliaRepository.java
│   └── InvitacionRepository.java
└── service/
    └── FamiliaService.java

frontend/SuperListApp/
└── app/src/main/java/com/superlist/app/
    ├── data/api/
    │   └── FamiliaApi.kt
    ├── data/repository/
    │   └── FamiliaRepository.kt
    └── ui/familia/
        ├── FamiliasListActivity.kt
        ├── CrearFamiliaActivity.kt
        ├── DetalleFamiliaActivity.kt
        ├── InvitacionesActivity.kt
        └── UnirseFamiliaActivity.kt
```

## Complexity Tracking

N/A.
