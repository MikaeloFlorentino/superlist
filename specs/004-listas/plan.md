# Implementation Plan: Listas del Super

**Branch**: `004-listas` | **Date**: 2025-01-01 | **Spec**: `specs/004-listas/spec.md`
**Input**: Feature specification from `/specs/004-listas/spec.md`

## Summary

Sistema de listas de compras por familia. Soporta creación de listas con artículos del catálogo o items manuales, estados de items (PENDIENTE/COMPRADO/NO_HAY), estados de lista (PENDIENTE/EN_CURSO/COMPLETADA/CANCELADA), reordenamiento y cálculo de progreso.

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3.4.4, JPA, Lombok, Flyway
**Storage**: PostgreSQL 15+ (con tipos ENUM)
**Testing**: JUnit 5, Mockito
**Target Platform**: Android (frontend) + Spring Boot (backend)
**Project Type**: Web API (backend) + Mobile App (frontend)
**Performance Goals**: < 500ms for CRUD, < 300ms for state changes
**Constraints**: JWT auth required. Solo miembros de la familia.
**Scale/Scope**: MVP — sin precios reales, sin total_gastado

## Constitution Check

- ✅ Solo miembros de familia modifican
- ✅ Read-only tras completar
- ✅ Items pueden ser catálogo o manuales
- ✅ Estados con transiciones controladas

## Project Structure

### Documentation

```text
specs/004-listas/
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
│   └── ListaController.java
├── model/
│   ├── Lista.java
│   ├── ItemLista.java
│   └── dto/ (CrearListaRequest, ListaResponse, AgregarItemRequest, etc.)
├── repository/
│   ├── ListaRepository.java
│   └── ItemListaRepository.java
└── service/
    └── ListaService.java

frontend/SuperListApp/
└── app/src/main/java/com/superlist/app/
    ├── data/api/
    │   └── ListaApi.kt
    ├── data/repository/
    │   └── ListaRepository.kt
    └── ui/lista/
        ├── ListasActivity.kt
        ├── CrearListaActivity.kt
        ├── DetalleListaActivity.kt
        └── AgregarItemActivity.kt
```

## Complexity Tracking

N/A.
