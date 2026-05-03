# Implementation Plan: Artículos y Catálogo

**Branch**: `003-articulos-catalogo` | **Date**: 2025-01-01 | **Spec**: `specs/003-articulos-catalogo/spec.md`
**Input**: Feature specification from `/specs/003-articulos-catalogo/spec.md`

## Summary

Catálogo de artículos por familia con SKU único, código de barras opcional, y áreas de organización (supermercado y casa). Cada artículo pertenece a una familia y las áreas por defecto se crean automáticamente al crear la familia. Incluye importación entre familias y búsqueda por código de barras.

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3.4.4, JPA, Lombok, Flyway
**Storage**: PostgreSQL 15+
**Testing**: JUnit 5, Mockito
**Target Platform**: Android (frontend) + Spring Boot REST API (backend)
**Project Type**: Web API (backend) + Mobile App (frontend)
**Performance Goals**: < 500ms for CRUD operations, < 300ms for search
**Constraints**: JWT auth required. Solo miembros de la familia pueden gestionar artículos/áreas.
**Scale/Scope**: MVP — sin importación masiva, sin editar áreas

## Constitution Check

- ✅ JWT stateless auth
- ✅ Todos los endpoints validan membresía
- ✅ Soft delete para artículos
- ✅ Áreas por defecto seedeadas al crear familia

## Project Structure

### Documentation

```text
specs/003-articulos-catalogo/
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
│   ├── ArticuloController.java
│   └── AreaController.java
├── model/
│   ├── Articulo.java
│   ├── AreaSuper.java
│   ├── AreaCasa.java
│   └── dto/ (CrearArticuloRequest, ArticuloResponse, etc.)
├── repository/
│   ├── ArticuloRepository.java
│   ├── AreaSuperRepository.java
│   └── AreaCasaRepository.java
└── service/
    └── CatalogoService.java

frontend/SuperListApp/
└── app/src/main/java/com/superlist/app/
    ├── data/api/
    │   └── CatalogoApi.kt
    ├── data/repository/
    │   └── CatalogoRepository.kt
    └── ui/catalogo/
        ├── CatalogoActivity.kt
        ├── CrearArticuloActivity.kt
        ├── EditarArticuloActivity.kt
        ├── AreasActivity.kt
        └── ImportarArticuloActivity.kt
```

## Complexity Tracking

N/A.
