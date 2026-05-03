# Tasks: Familias

**Input**: Design documents from `/specs/002-familias/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/

## Path Conventions

- **Backend**: `backend/src/main/java/com/superlist/`
- **Frontend**: `frontend/SuperListApp/app/src/main/java/com/superlist/app/`
- **Resources**: `backend/src/main/resources/db/migration/`

---

## Phase 1: Backend

- [ ] T001 Create Flyway migration `V2__create_familias.sql`
- [ ] T002 Create `Familia.java` entity
- [ ] T003 Create `MiembroFamilia.java` entity
- [ ] T004 Create `Invitacion.java` entity
- [ ] T005 Create `FamiliaRepository.java` (findByMiembro, findByCodigoInvitacion)
- [ ] T006 Create `MiembroFamiliaRepository.java` (findByUsuarioId, findByFamiliaIdAndUsuarioId, countByFamiliaId)
- [ ] T007 Create `InvitacionRepository.java` (findByUsuarioIdAndEstado, findByFamiliaIdAndUsuarioIdAndEstado)
- [ ] T008 Create DTOs: `CrearFamiliaRequest`, `FamiliaResponse`, `FamiliaDetalleResponse`, `InvitarRequest`, `InvitacionResponse`, `UnirseRequest`
- [ ] T009 Create `FamiliaService.java` (crear, listar, detalle, invitar, unirsePorCodigo, aceptarInvitacion, rechazarInvitacion, listarInvitaciones)
- [ ] T010 Create `FamiliaController.java` (8 endpoints)

## Phase 2: Frontend Android

- [ ] T011 Create `FamiliaApi.kt` (Retrofit interface for all familia endpoints)
- [ ] T012 Create `FamiliaRepository.kt`
- [ ] T013 Create `FamiliasListActivity.kt` + layout
- [ ] T014 Create `CrearFamiliaActivity.kt` + layout
- [ ] T015 Create `DetalleFamiliaActivity.kt` + layout
- [ ] T016 Create `InvitacionesActivity.kt` + layout
- [ ] T017 Create `UnirseFamiliaActivity.kt` + layout
- [ ] T018 Update AndroidManifest.xml with new activities
