# Feature Specification: Artículos y Catálogo

**Feature Branch**: `003-articulos-catalogo`
**Created**: 2025-01-01
**Status**: Draft
**Input**: User description: "Catálogo de artículos por familia con SKU, código de barras, áreas del super y de la casa"

## User Scenarios & Testing

### User Story 1 - Gestionar catálogo de artículos (Priority: P1)

Como miembro de una familia, quiero agregar artículos al catálogo de mi familia para tener una lista de productos que compramos regularmente.

**Why this priority**: El catálogo es la base de las listas de compras. Sin artículos no se pueden crear listas.

**Independent Test**: Crear un artículo, buscarlo, actualizarlo y desactivarlo.

**Acceptance Scenarios**:

1. **Given** un usuario miembro de una familia, **When** crea un artículo con nombre, SKU y cantidad por defecto, **Then** el artículo se guarda en el catálogo de la familia
2. **Given** un artículo existente, **When** se busca por nombre (q), **Then** se encuentran los artículos que coinciden
3. **Given** un artículo existente, **When** se actualiza su información, **Then** los cambios se guardan
4. **Given** un artículo existente, **When** se desactiva, **Then** el artículo queda con activo=false
5. **Given** un SKU duplicado en la misma familia, **When** se intenta crear, **Then** recibe error

---

### User Story 2 - Buscar por código de barras (Priority: P2)

Como usuario en la tienda, quiero buscar un artículo por su código de barras para encontrarlo rápido en el catálogo.

**Why this priority**: Útil pero no crítico para el MVP. Se puede agregar el artículo manualmente.

**Independent Test**: Escanear un código de barras y encontrar el artículo.

**Acceptance Scenarios**:

1. **Given** un código de barras registrado, **When** se busca, **Then** devuelve el artículo con el nombre de su familia
2. **Given** un código de barras no registrado, **When** se busca, **Then** devuelve 404

---

### User Story 3 - Importar artículos de otras familias (Priority: P2)

Como miembro de múltiples familias, quiero importar artículos del catálogo de otra familia para no tener que crearlos desde cero.

**Why this priority**: Ahorra tiempo pero no es esencial.

**Independent Test**: Tener un artículo en la Familia A, importarlo a la Familia B.

**Acceptance Scenarios**:

1. **Given** un artículo en la Familia A donde el usuario es miembro, **When** lo importa a la Familia B, **Then** se copia el artículo a la Familia B
2. **Given** un SKU que ya existe en la familia destino, **When** se importa, **Then** se agrega sufijo "-copia" al SKU

---

### User Story 4 - Gestionar áreas (Priority: P2)

Como miembro de una familia, quiero gestionar las áreas del super y de la casa para organizar mejor los artículos.

**Why this priority**: Las áreas por defecto son suficientes para empezar. La personalización es un plus.

**Independent Test**: Crear una nueva área del super, listar áreas, eliminar una.

**Acceptance Scenarios**:

1. **Given** una familia recién creada, **When** se listan las áreas del super, **Then** aparecen las 9 áreas por defecto (Frutas y Verduras, Carnes, Lácteos, Panadería, Limpieza, Bebidas, Despensa, Congelados, Otros)
2. **Given** una familia recién creada, **When** se listan las áreas de la casa, **Then** aparecen las 6 áreas por defecto (Cocina, Baño, Recámara, Sala, Despensa, Otros)
3. **Given** un área existente, **When** un miembro la elimina, **Then** el área desaparece

---

### Edge Cases

- ¿Qué pasa si se desactiva un artículo que está en una lista activa?
- ¿Se puede reactivar un artículo desactivado?
- ¿Qué pasa si se elimina un área que tiene artículos asociados?

## Requirements

### Functional Requirements

- **FR-001**: System MUST allow family members to create articles with name, SKU, and default quantity
- **FR-002**: System MUST enforce SKU uniqueness per family
- **FR-003**: System MUST allow optional barcode per article
- **FR-004**: System MUST list articles by family with optional filters (active, search query)
- **FR-005**: System MUST allow searching articles by barcode across user's families
- **FR-006**: System MUST support searching articles by name across user's families
- **FR-007**: System MUST allow updating article details
- **FR-008**: System MUST support soft-delete (active=false) for articles
- **FR-009**: System MUST allow importing articles from other families (with SKU conflict resolution)
- **FR-010**: System MUST create default areas (super and home) when a family is created
- **FR-011**: System MUST allow family members to create/list/delete custom areas
- **FR-012**: System MUST include area ordering (integer field)

### Key Entities

- **Articulo**: Producto en el catálogo de una familia. SKU único por familia, barcode opcional, soft-delete.
- **AreaSuper**: Área del supermercado para agrupar artículos (Lácteos, Carnes, etc.)
- **AreaCasa**: Área de la casa donde se usa el producto (Cocina, Baño, etc.)

## Success Criteria

- **SC-001**: Usuario puede crear un artículo en < 2 segundos
- **SC-002**: Búsqueda por nombre devuelve resultados en < 500ms
- **SC-003**: Áreas por defecto se crean automáticamente al crear la familia
- **SC-004**: SKU duplicado es rechazado con error claro

## Assumptions

- Los artículos pertenecen a una familia (no son globales)
- Código de barras no es único (pueden haber duplicados entre familias)
- Soft delete mantiene histórico de compras
- Al importar, SKU se modifica si hay conflicto (sufijo "-copia")
