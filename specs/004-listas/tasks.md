# Tasks: Listas del Super

**Input**: Design documents from `/specs/004-listas/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/

---

## Phase 1: Backend

- [ ] T001 Create Flyway migration `V4__create_listas.sql`
- [ ] T002 Create entities: `Lista.java`, `ItemLista.java`
- [ ] T003 Create repositories: `ListaRepository.java`, `ItemListaRepository.java`
- [ ] T004 Create DTOs (CrearListaRequest, ListaResponse, AgregarItemRequest, ItemResponse, CambiarEstadoRequest, ReordenarRequest, etc.)
- [ ] T005 Create `ListaService.java` (create, list, detail, addItem, updateItem, deleteItem, changeItemState, changeListState, reorder, getTotal)
- [ ] T006 Create `ListaController.java` (10 endpoints)
- [ ] T007 Add item state validation (transiciones permitidas)
- [ ] T008 Add list state validation (read-only after COMPLETADA/CANCELADA)

## Phase 2: Frontend Android

- [ ] T009 Create `ListaApi.kt` (Retrofit interface)
- [ ] T010 Create `ListaRepository.kt`
- [ ] T011 Create `ListasActivity.kt` + layout
- [ ] T012 Create `CrearListaActivity.kt` + layout
- [ ] T013 Create `DetalleListaActivity.kt` + layout (show items, check/uncheck)
- [ ] T014 Create `AgregarItemActivity.kt` + layout
- [ ] T015 Update AndroidManifest.xml
