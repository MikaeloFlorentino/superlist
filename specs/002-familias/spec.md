# Feature Specification: Familias

**Feature Branch**: `002-familias`
**Created**: 2025-01-01
**Status**: Draft
**Input**: User description: "Gestión de familias: crear, invitar miembros, aceptar/rechazar invitaciones, unirse por código"

## User Scenarios & Testing

### User Story 1 - Crear familia y ser ADMIN (Priority: P1)

Como usuario verificado, quiero crear una familia para gestionar las compras del hogar con otras personas.

**Why this priority**: Las familias son el contenedor principal de la app. Sin familia no hay catálogo, listas ni compras compartidas.

**Independent Test**: Crear una familia y verificar que el creador queda como ADMIN y se genera un código de invitación.

**Acceptance Scenarios**:

1. **Given** un usuario autenticado, **When** crea una familia con nombre válido, **Then** la familia se crea con el usuario como ADMIN y se genera un código de invitación de 8 caracteres
2. **Given** un usuario autenticado, **When** consulta sus familias, **Then** ve la lista de familias donde es miembro con su rol

---

### User Story 2 - Invitar miembros (Priority: P1)

Como ADMIN de una familia, quiero invitar a otros usuarios por teléfono para compartir las listas de compras.

**Why this priority**: La colaboración es el core de la app. Sin poder invitar, las listas son individuales.

**Independent Test**: Invitar a un usuario registrado y verificar que recibe una invitación pendiente.

**Acceptance Scenarios**:

1. **Given** un ADMIN de una familia, **When** invita a un usuario registrado por teléfono, **Then** se crea una invitación PENDIENTE
2. **Given** un ADMIN de una familia, **When** invita a un teléfono no registrado, **Then** recibe error
3. **Given** un ADMIN de una familia, **When** invita a alguien que ya es miembro, **Then** recibe error
4. **Given** un ADMIN de una familia, **When** invita a alguien que ya tiene invitación PENDIENTE, **Then** recibe error
5. **Given** un MIEMBRO (no ADMIN), **When** intenta invitar, **Then** recibe error

---

### User Story 3 - Aceptar/Rechazar invitaciones (Priority: P2)

Como usuario invitado, quiero aceptar o rechazar invitaciones para unirme a las familias que me interesan.

**Why this priority**: Necesario para completar el flujo pero el usuario invitado puede usar la app sin aceptar inmediatamente.

**Independent Test**: Ver invitaciones pendientes, aceptar una y verificar que aparece como miembro.

**Acceptance Scenarios**:

1. **Given** un usuario con invitaciones pendientes, **When** consulta sus invitaciones, **Then** ve todas las invitaciones PENDIENTE
2. **Given** una invitación PENDIENTE, **When** el usuario la acepta, **Then** pasa a ACEPTADA y se crea el miembro con rol MIEMBRO
3. **Given** una invitación PENDIENTE, **When** el usuario la rechaza, **Then** pasa a RECHAZADA
4. **Given** un usuario que no es el destinatario, **When** intenta aceptar/rechazar, **Then** recibe error

---

### User Story 4 - Unirse por código (Priority: P2)

Como usuario, quiero unirme a una familia usando un código de invitación directo.

**Why this priority**: Alternativa útil a las invitaciones, pero no crítica para el MVP.

**Independent Test**: Tomar el código de invitación de una familia y usarlo para unirse desde otra cuenta.

**Acceptance Scenarios**:

1. **Given** un código de invitación válido, **When** un usuario lo usa para unirse, **Then** se agrega como MIEMBRO directamente
2. **Given** un código inválido, **When** un usuario intenta unirse, **Then** recibe error
3. **Given** un usuario que ya es miembro, **When** intenta unirse con el código, **Then** recibe error

---

### Edge Cases

- ¿Qué pasa si se elimina el ADMIN original de la familia?
- ¿Las invitaciones expiran después de 30 días?
- ¿Qué pasa si el usuario que invitó se da de baja?
- ¿Se puede tener el mismo código de invitación en dos familias?

## Requirements

### Functional Requirements

- **FR-001**: System MUST allow verified users to create a family
- **FR-002**: System MUST auto-assign the creator as ADMIN
- **FR-003**: System MUST generate a unique 8-character alphanumeric invitation code per family
- **FR-004**: System MUST list families where the user is a member
- **FR-005**: System MUST show family details (members with roles) to members only
- **FR-006**: System MUST allow ADMINs to invite users by phone number
- **FR-007**: System MUST reject invitation if target user doesn't exist
- **FR-008**: System MUST reject invitation if target is already a member
- **FR-009**: System MUST reject invitation if there's already a pending invitation for same user/family
- **FR-010**: System MUST allow users to view their pending invitations
- **FR-011**: System MUST allow users to accept invitations and become MIEMBRO
- **FR-012**: System MUST allow users to reject invitations
- **FR-013**: System MUST allow users to join a family via invitation code
- **FR-014**: System MUST enforce (familia_id, usuario_id) uniqueness for membership
- **FR-015**: System MUST reject joining if user is already a member

### Key Entities

- **Familia**: Grupo de usuarios que comparten listas de compras. Tiene nombre único y código de invitación.
- **MiembroFamilia**: Relación usuario-familia con rol (ADMIN/MIEMBRO).
- **Invitacion**: Solicitud de ingreso a una familia con estado (PENDIENTE/ACEPTADA/RECHAZADA).

## Success Criteria

### Measurable Outcomes

- **SC-001**: Usuario puede crear una familia en < 2 segundos
- **SC-002**: ADMIN puede invitar miembros y estos ven la invitación inmediatamente
- **SC-003**: Usuarios pueden unirse por código sin errores
- **SC-004**: No se permiten duplicados (mismo usuario, misma familia)

## Assumptions

- Una familia puede tener múltiples ADMINs
- Un usuario puede pertenecer a muchas familias
- El código de invitación es único a nivel global
- Las invitaciones expiran después de 30 días sin respuesta
