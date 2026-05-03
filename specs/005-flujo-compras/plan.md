# Implementation Plan: Flujo de Compras y Pendientes

**Branch**: `005-flujo-compras` | **Date**: 2025-01-01 | **Spec**: `specs/005-flujo-compras/spec.md`
**Input**: Feature specification from `/specs/005-flujo-compras/spec.md`

## Summary

Modo compra (vista agrupada por área del super), manejo automático de items NO_HAY que se duplican a una lista de pendientes por familia, historial de compras completadas, y gestión de pendientes (resolver, mover a lista activa).

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3.4.4, JPA, Lombok, Flyway
**Storage**: PostgreSQL 15+ (con tipos ENUM)
**Testing**: JUnit 5, Mockito
**Target Platform**: Android + Spring Boot
**Performance Goals**: < 500ms for grouped views
**Constraints**: JWT required. Solo miembros de familia. Read-only para historial.
**Scale/Scope**: MVP — sin precios, sin editar items del historial

## Constitution Check

- ✅ ListaPendiente única por familia (UNIQUE constraint)
- ✅ NO_HAY → auto-clone a pendientes
- ✅ Acumulación de cantidades para mismo artículo
- ✅ Historial read-only

## Project Structure

### Documentation

```text
specs/005-flujo-compras/
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
│   └── CompraController.java (modo-compra, historial)
│   └── PendienteController.java (pendientes CRUD)
├── model/
│   ├── ListaPendiente.java
│   ├── ItemPendiente.java
│   └── dto/
├── repository/
│   ├── ListaPendienteRepository.java
│   └── ItemPendienteRepository.java
└── service/
    ├── CompraService.java (modo-compra, historial)
    └── PendienteService.java (pendientes CRUD)

frontend/SuperListApp/
└── app/src/main/java/com/superlist/app/
    ├── data/api/
    │   └── CompraApi.kt
    │   └── PendienteApi.kt
    └── ui/compra/
        ├── ModoCompraActivity.kt
        ├── PendientesActivity.kt
        ├── HistorialListasActivity.kt
        └── DetalleHistorialActivity.kt
```

## Complexity Tracking

N/A.
