# Feature Specification: Flujo de Compras y Pendientes

**Feature Branch**: `005-flujo-compras`
**Created**: 2025-01-01
**Status**: Draft
**Input**: User description: "Vista de compra agrupada por área del super, manejo de items 'no hay', historial de compras y lista de pendientes"

## User Scenarios & Testing

### User Story 1 - Modo compra agrupado (Priority: P1)

Como usuario en el supermercado, quiero ver mis items agrupados por área del super para comprar de manera ordenada.

**Why this priority**: Es la vista principal durante la compra física en la tienda.

**Independent Test**: Crear items en distintas áreas y ver la vista modo-compra agrupada.

**Acceptance Scenarios**:

1. **Given** una lista con items en varias áreas del super, **When** se consulta el modo compra, **Then** los items se agrupan por área_super ordenadas por el campo orden
2. **Given** items sin área asignada, **When** se consulta el modo compra, **Then** aparecen en un grupo "Sin área" al final
3. **Given** una lista en modo compra, **When** se marca un item COMPRADO, **Then** se refleja inmediatamente

---

### User Story 2 - Manejo de "No Hay" con pendientes (Priority: P1)

Como usuario en la tienda, cuando un producto no está disponible quiero marcarlo como NO_HAY y que se guarde automáticamente en una lista de pendientes para la próxima compra.

**Why this priority**: Automatiza el seguimiento de productos faltantes, una necesidad real de las compras.

**Independent Test**: Marcar un item como NO_HAY en una lista y verificar que aparece en la lista de pendientes de la familia.

**Acceptance Scenarios**:

1. **Given** un item en una lista activa, **When** se marca como NO_HAY, **Then** se crea automáticamente un item en la lista de pendientes de la familia
2. **Given** un item ya pendiente (mismo artículo), **When** se marca NO_HAY otra vez, **Then** se suma la cantidad al pendiente existente
3. **Given** la respuesta del endpoint NO_HAY, **When** se procesa, **Then** incluye indicador de que se agregó a pendientes

---

### User Story 3 - Gestionar pendientes (Priority: P2)

Como usuario, quiero ver los items pendientes de compras anteriores y resolverlos cuando los encuentre.

**Why this priority**: Completa el ciclo de los NO_HAY. Sin esto, los pendientes se acumulan sin poder cerrarlos.

**Independent Test**: Ver pendientes, marcarlos como resueltos, moverlos a una lista activa.

**Acceptance Scenarios**:

1. **Given** una familia con items pendientes, **When** se consultan, **Then** se listan los pendientes activos
2. **Given** un item pendiente, **When** se resuelve, **Then** queda como RESUELTO con fecha
3. **Given** un item pendiente, **When** se mueve a una lista activa, **Then** se crea un ItemLista en la lista destino y el pendiente sigue PENDIENTE
4. **Given** un item pendiente, **When** se resuelve con lista activa, **Then** se crea ItemLista y el pendiente queda RESUELTO

---

### User Story 4 - Historial de compras (Priority: P3)

Como usuario, quiero ver el historial de compras completadas para consultar compras anteriores.

**Why this priority**: Útil para referencia pero no crítico para el MVP.

**Independent Test**: Completar una lista y verla en el historial.

**Acceptance Scenarios**:

1. **Given** listas completadas, **When** se consulta el historial, **Then** se muestran ordenadas por fecha DESC con resumen
2. **Given** una lista completada, **When** se consulta su detalle histórico, **Then** se muestran todos los items con su estado final y cantidades

---

### Edge Cases

- ¿Qué pasa si se marcan NO_HAY items repetidos del mismo artículo? Se suman cantidades.
- ¿Los pendientes persisten aunque se elimine la lista de origen?
- ¿Se puede marcar un pendiente como resuelto sin haberlo comprado?

## Requirements

### Functional Requirements

- **FR-001**: System MUST group items by super area with ordering in shopping mode view
- **FR-002**: System MUST show items without area in a "Sin área" group
- **FR-003**: System MUST calculate shopping mode summary (total, comprados, pendientes, no_hay, progreso)
- **FR-004**: System MUST create a persistent "Pendientes" list per family on family creation
- **FR-005**: System MUST auto-create pendiente item when an item is marked NO_HAY
- **FR-006**: System MUST accumulate quantities when same article is marked NO_HAY again
- **FR-007**: System MUST allow viewing pendientes (filterable by state)
- **FR-008**: System MUST allow marking pendientes as RESUELTO (with optional date and resolver)
- **FR-009**: System MUST allow moving pendiente to an active shopping list
- **FR-010**: System MUST provide a pendientes count endpoint
- **FR-011**: System MUST show completed shopping lists history ordered by completion date
- **FR-012**: System MUST show detailed history of a completed list with item states and quantities
- **FR-013**: System MUST record which list an item came from (lista_origen_id)

### Key Entities

- **ListaPendiente**: Lista persistente por familia que acumula items NO_HAY. Una por familia, creada automáticamente.
- **ItemPendiente**: Item en la lista de pendientes. Referencia a la lista de origen donde se marcó NO_HAY.

## Success Criteria

- **SC-001**: Vista modo compra agrupa items correctamente por área
- **SC-002**: Al marcar NO_HAY, el pendiente aparece en < 1 segundo
- **SC-003**: Items del mismo artículo se suman en pendientes
- **SC-004**: Historial muestra compras completadas ordenadas por fecha

## Assumptions

- ListaPendiente se crea automáticamente al crear la familia (en FamiliaService)
- Al marcar NO_HAY se clona a items_pendientes con referencia a la lista origen
- Al resolver, no se borra el item — queda como histórico
- El historial no permite modificar nada (read-only)
