# Tasks: Flujo de Compras y Pendientes

**Input**: Design documents from `/specs/005-flujo-compras/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/

---

## Phase 1: Backend Foundation

- [ ] T001 Create Flyway migration `V5__create_pendientes.sql`
- [ ] T002 Create entities: `ListaPendiente.java`, `ItemPendiente.java`
- [ ] T003 Create repositories: `ListaPendienteRepository.java`, `ItemPendienteRepository.java`
- [ ] T004 Add ListaPendiente seeding logic in `FamiliaService.java` (create pendientes list on family creation)
- [ ] T005 Create DTOs (ModoCompraResponse, PendienteResponse, etc.)

## Phase 2: Backend Services

- [ ] T006 Create `PendienteService.java` (getPendientes, getTotal, resolver, moverALista)
- [ ] T007 Create `CompraService.java` (getModoCompra — group items by area_super with ordering)
- [ ] T008 Modify `ItemListaService.java` (when marking NO_HAY, auto-clone to pendientes)
- [ ] T009 Create `PendienteController.java` (4 endpoints)
- [ ] T010 Create `CompraController.java` (modo-compra, historial, detalle historial)

## Phase 3: Frontend Android

- [ ] T011 Create `CompraApi.kt`, `PendienteApi.kt` (Retrofit interfaces)
- [ ] T012 Create `CompraRepository.kt`, `PendienteRepository.kt`
- [ ] T013 Create `ModoCompraActivity.kt` + layout (grouped by area, checkboxes, NO_HAY buttons)
- [ ] T014 Create `PendientesActivity.kt` + layout
- [ ] T015 Create `HistorialListasActivity.kt` + layout
- [ ] T016 Create `DetalleHistorialActivity.kt` + layout
- [ ] T017 Update AndroidManifest.xml
