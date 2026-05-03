# Feature Specification: Usuarios y Autenticación

**Feature Branch**: `001-auth`
**Created**: 2025-01-01
**Status**: Draft
**Input**: User description: "Registro e inicio de sesión mediante número de teléfono con verificación SMS"

## User Scenarios & Testing

### User Story 1 - Registro con teléfono y verificación SMS (Priority: P1)

Como usuario nuevo, quiero registrarme con mi número de teléfono para poder usar la aplicación.

**Why this priority**: Sin registro no hay aplicación. Es el punto de entrada obligatorio para todo el sistema.

**Independent Test**: Se puede probar enviando un número de teléfono al endpoint de solicitar código y verificando que se recibe un código de 6 dígitos en consola.

**Acceptance Scenarios**:

1. **Given** un teléfono no registrado, **When** el usuario solicita un código de verificación, **Then** se crea un usuario pendiente y se envía un código de 6 dígitos
2. **Given** un teléfono ya registrado, **When** el usuario solicita un código de verificación, **Then** se reenvía un nuevo código de 6 dígitos
3. **Given** un código de verificación válido, **When** el usuario lo envía con su teléfono, **Then** el usuario queda verificado y recibe un JWT
4. **Given** un código inválido, **When** el usuario intenta verificarlo, **Then** recibe error 401
5. **Given** 5 intentos fallidos con el mismo código, **When** el usuario intenta de nuevo, **Then** el código se invalida automáticamente
6. **Given** un código expirado (> 10 minutos), **When** el usuario intenta verificarlo, **Then** recibe error 401

---

### User Story 2 - Gestión de perfil (Priority: P2)

Como usuario registrado, quiero ver y actualizar mi perfil para mantener mis datos personales.

**Why this priority**: El perfil es importante pero no bloquea el uso básico (las familias y listas necesitan el nombre del usuario).

**Independent Test**: Se puede probar obteniendo el perfil con un JWT válido y actualizando el nombre.

**Acceptance Scenarios**:

1. **Given** un JWT válido, **When** el usuario solicita su perfil, **Then** recibe sus datos (id, nombre, teléfono, fecha_creación)
2. **Given** un JWT válido y un nombre nuevo, **When** el usuario actualiza su perfil, **Then** el nombre se actualiza correctamente
3. **Given** un intento de dejar el nombre vacío, **When** el usuario actualiza su perfil, **Then** recibe error de validación
4. **Given** un JWT inválido/expirado, **When** el usuario solicita su perfil, **Then** recibe error 401

---

### Edge Cases

- ¿Qué pasa si el teléfono tiene formato incorrecto (no E.164)?
- ¿Cómo maneja el sistema si se solicitan más de 3 códigos en 5 minutos?
- ¿Qué pasa si el JWT expira durante una sesión activa?
- ¿Cómo se maneja si dos usuarios intentan registrarse con el mismo teléfono simultáneamente?

## Requirements

### Functional Requirements

- **FR-001**: System MUST allow users to request a verification code via phone number (E.164 format)
- **FR-002**: System MUST create a pending user if the phone number doesn't exist
- **FR-003**: System MUST generate a random 6-digit verification code
- **FR-004**: System MUST allow users to verify their code and receive a JWT
- **FR-005**: System MUST mark users as verified upon successful code verification
- **FR-006**: System MUST clear the verification code after successful verification
- **FR-007**: System MUST return user profile when a valid JWT is provided
- **FR-008**: System MUST allow users to update their name
- **FR-009**: System MUST enforce rate limiting: max 3 code requests per 5 minutes per phone
- **FR-010**: System MUST invalidate verification code after 5 failed attempts
- **FR-011**: System MUST expire verification codes after 10 minutes
- **FR-012**: System MUST issue JWTs with 7-day expiration
- **FR-013**: System MUST log verification codes to console (MVP) instead of sending SMS
- **FR-014**: System MUST validate that name is not empty on profile update

### Key Entities

- **Usuario**: Representa un usuario registrado. Tiene teléfono (único), nombre, estado de verificación y datos de verificación temporal (código, expiración, intentos).

## Success Criteria

### Measurable Outcomes

- **SC-001**: Users can complete registration (phone → code → verify → JWT) in under 30 seconds
- **SC-002**: System handles 100 concurrent code requests without degradation
- **SC-003**: Rate limiting prevents abuse (>3 requests/5min)
- **SC-004**: Invalid codes are rejected within 500ms

## Assumptions

- Los códigos de verificación se loguean en consola en MVP (sin integración SMS real)
- Formato de teléfono E.164 (+521234567890)
- HMAC-SHA256 para firma JWT con secret key configurable
