# Feature Specification: Listas del Super

**Feature Branch**: `004-listas`
**Created**: 2025-01-01
**Status**: Draft
**Input**: User description: "Crear y gestionar listas de compras por familia con items, estados y responsables"

## User Scenarios & Testing

### User Story 1 - Crear y gestionar lista de compras (Priority: P1)

Como miembro de una familia, quiero crear una lista de compras y agregar artículos del catálogo para planificar mis compras.

**Why this priority**: Las listas son el core de la aplicación. Sin listas no hay flujo de compras.

**Independent Test**: Crear una lista, agregar varios artículos, cambiar cantidades.

**Acceptance Scenarios**:

1. **Given** un miembro de una familia, **When** crea una lista con nombre y supermercado opcional, **Then** la lista se crea con estado PENDIENTE
2. **Given** una lista en PENDIENTE o EN_CURSO, **When** se agrega un item con articulo_id del catálogo, **Then** el item aparece con el nombre resuelto del catálogo
3. **Given** una lista en PENDIENTE o EN_CURSO, **When** se agrega un item manual (nombre_manual), **Then** el item se crea sin vínculo a catálogo
4. **Given** items en una lista, **When** se modifica su cantidad o notas, **Then** los cambios se guardan
5. **Given** un item existente, **When** se elimina, **Then** el item desaparece y se reordenan los restantes

---

### User Story 2 - Avanzar estados de items y lista (Priority: P1)

Como usuario en la tienda, quiero marcar items como COMPRADO o NO_HAY durante la compra.

**Why this priority**: Sin estado de items no hay sentido de progreso ni diferenciación entre lo comprado y lo pendiente.

**Independent Test**: Marcar items como COMPRADO y NO_HAY, ver el progreso actualizado.

**Acceptance Scenarios**:

1. **Given** un item PENDIENTE, **When** se marca como COMPRADO, **Then** el estado cambia y se guarda cantidad_comprada
2. **Given** un item PENDIENTE, **When** se marca como NO_HAY, **Then** el estado cambia
3. **Given** un item COMPRADO, **When** se desmarca a PENDIENTE, **Then** vuelve a pendiente
4. **Given** un item NO_HAY, **When** se reabre a PENDIENTE, **Then** vuelve a pendiente
5. **Given** una lista PENDIENTE, **When** se cambia a EN_CURSO, **Then** el estado avanza
6. **Given** una lista en cualquier estado, **When** se cambia a CANCELADA, **Then** la lista se cancela

---

### User Story 3 - Finalizar compra (Priority: P2)

Como usuario, quiero marcar la lista como COMPLETADA cuando termino la compra.

**Why this priority**: Necesario para el historial y para que los pendientes (NO_HAY) se procesen.

**Independent Test**: Completar una lista y verificar que se guarda la fecha y quién la completó.

**Acceptance Scenarios**:

1. **Given** una lista EN_CURSO, **When** se completa, **Then** se guarda fecha_completada y completada_por
2. **Given** una lista COMPLETADA o CANCELADA, **When** se intenta agregar/editar items, **Then** se rechaza la operación

---

### User Story 4 - Reordenar y ver progreso (Priority: P3)

Como usuario, quiero reordenar los items de la lista y ver cuánto progreso llevo.

**Why this priority**: Útil pero no crítico para la funcionalidad básica.

**Independent Test**: Reordenar items y verificar que el progreso se calcula correctamente.

**Acceptance Scenarios**:

1. **Given** una lista con items, **When** se reordenan, **Then** los items quedan en el nuevo orden
2. **Given** una lista con items, **When** se consulta el total, **Then** se devuelve el conteo de pendientes, comprados, no_hay y progreso porcentual

---

### Edge Cases

- ¿Qué pasa si el artículo del catálogo se desactiva mientras está en una lista activa?
- ¿Se puede cambiar la cantidad de un item a 0?
- ¿Qué pasa si una lista tiene items pero se cancela? Los items se mantienen como referencia.

## Requirements

### Functional Requirements

- **FR-001**: System MUST allow family members to create shopping lists with name and optional supermarket
- **FR-002**: System MUST initialize lists with PENDIENTE status
- **FR-003**: System MUST allow adding items from catalog (articulo_id) or manually (nombre_manual)
- **FR-004**: System MUST assign order automatically (last position)
- **FR-005**: System MUST support item states: PENDIENTE, COMPRADO, NO_HAY
- **FR-006**: System MUST allow toggling item states (PENDIENTE ↔ COMPRADO, PENDIENTE ↔ NO_HAY)
- **FR-007**: System MUST save cantidad_comprada when marking COMPRADO
- **FR-008**: System MUST support list states: PENDIENTE, EN_CURSO, COMPLETADA, CANCELADA
- **FR-009**: System MUST record who completed the list and when
- **FR-010**: System MUST make COMPLETADA/CANCELADA lists read-only (no item add/edit/delete)
- **FR-011**: System MUST allow reordering items
- **FR-012**: System MUST calculate progress (comprados / total * 100)
- **FR-013**: System MUST filter lists by status
- **FR-014**: System MUST show counts (items_count, items_completados) in list overview
- **FR-015**: System MUST delete items from active lists and reorder remaining

### Key Entities

- **Lista**: Lista de compras con nombre, supermercado opcional, estado y total estimado.
- **ItemLista**: Item dentro de una lista. Enlaza al catálogo o usa nombre_manual. Tiene estado, áreas, responsable.

## Success Criteria

- **SC-001**: Usuario puede crear una lista y agregar items en < 3 segundos
- **SC-002**: Cambios de estado de items se reflejan en < 500ms
- **SC-003**: Listas completadas no permiten modificaciones
- **SC-004**: Progreso se calcula correctamente siempre

## Assumptions

- Solo miembros de la familia pueden ver/editar listas e items
- Al agregar item con articulo_id, el nombre se resuelve del catálogo
- Items nuevos van al final (orden máximo + 1)
- Al completar lista, items NO_HAY quedan como referencia
