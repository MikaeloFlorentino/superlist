# Tasks: Artículos y Catálogo

**Input**: Design documents from `/specs/003-articulos-catalogo/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/

## Path Conventions

- **Backend**: `backend/src/main/java/com/superlist/`
- **Frontend**: `frontend/SuperListApp/app/src/main/java/com/superlist/app/`

---

## Phase 1: Backend

- [ ] T001 Create Flyway migration `V3__create_articulos.sql`
- [ ] T002 Create entities: `Articulo.java`, `AreaSuper.java`, `AreaCasa.java`
- [ ] T003 Create repositories: `ArticuloRepository.java`, `AreaSuperRepository.java`, `AreaCasaRepository.java`
- [ ] T004 Create DTOs for article CRUD (CrearArticuloRequest, ArticuloResponse, etc.)
- [ ] T005 Create `CatalogoService.java` (crear, listar, buscar, actualizar, desactivar, importar, gestionar áreas)
- [ ] T006 Create `ArticuloController.java` (7 endpoints)
- [ ] T007 Create `AreaController.java` (6 endpoints: CRUD for Super and Home areas)
- [ ] T008 Add default area seeding logic in `FamiliaService.java` (seed 9 super + 6 home areas on family creation)

## Phase 2: Frontend Android

- [ ] T009 Create `CatalogoApi.kt` (Retrofit interface)
- [ ] T010 Create `CatalogoRepository.kt`
- [ ] T011 Create `CatalogoActivity.kt` + layout (list/buscar artículos)
- [ ] T012 Create `CrearArticuloActivity.kt` + layout
- [ ] T013 Create `EditarArticuloActivity.kt` + layout
- [ ] T014 Create `AreasActivity.kt` + layout
- [ ] T015 Create `ImportarArticuloActivity.kt` + layout
- [ ] T016 Update AndroidManifest.xml
