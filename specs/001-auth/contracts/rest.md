# API Contracts: Usuarios y Autenticación

Base URL: `/api/auth`

## POST /api/auth/solicitar-codigo

Solicita un código de verificación para el teléfono dado. Si el teléfono no existe, crea un usuario pendiente. Si ya existe verificado, reenvía un nuevo código.

### Request

```json
{
  "telefono": "+521234567890"
}
```

### Response 200

```json
{
  "mensaje": "Código enviado"
}
```

### Errors

| Status | Body |
|--------|------|
| 429 | `{ "error": "Demasiadas solicitudes. Intenta en 5 minutos." }` |

### Rules

- Si el teléfono no existe, crea usuario pendiente de verificación
- Si el usuario ya existe verificado, reenvía código
- Genera código aleatorio de 6 dígitos
- Loguea código en consola (MVP)
- Rate limit: 3 solicitudes por teléfono cada 5 minutos

---

## POST /api/auth/verificar-codigo

Verifica el código enviado al teléfono. Si es correcto, marca al usuario como verificado y devuelve un JWT.

### Request

```json
{
  "telefono": "+521234567890",
  "codigo": "123456"
}
```

### Response 200

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "usuario": {
    "id": "uuid",
    "nombre": null,
    "telefono": "+521234567890"
  }
}
```

### Errors

| Status | Body |
|--------|------|
| 401 | `{ "error": "Código inválido" }` |

### Rules

- Valida código contra el almacenado y su expiración
- Si es correcto, marca usuario como verificado, limpia código e intentos
- Devuelve JWT (HMAC-SHA256, 7 días de expiración)
- JWT contiene: usuario_id, telefono, fecha_emision
- Después de 5 intentos fallidos, el código se invalida
- Después de 10 minutos, el código expira

---

## GET /api/auth/perfil

Obtiene el perfil del usuario autenticado.

### Headers

```
Authorization: Bearer <jwt>
```

### Response 200

```json
{
  "id": "uuid",
  "nombre": "Juan Pérez",
  "telefono": "+521234567890",
  "fecha_creacion": "2025-01-01T00:00:00Z"
}
```

### Errors

| Status | Body |
|--------|------|
| 401 | `{ "error": "No autorizado" }` |

---

## PUT /api/auth/perfil

Actualiza el perfil del usuario autenticado. Solo se puede actualizar el nombre.

### Headers

```
Authorization: Bearer <jwt>
```

### Request

```json
{
  "nombre": "Juan Pérez"
}
```

### Response 200

```json
{
  "id": "uuid",
  "nombre": "Juan Pérez",
  "telefono": "+521234567890"
}
```

### Errors

| Status | Body |
|--------|------|
| 400 | `{ "error": "El nombre no puede estar vacío" }` |
| 401 | `{ "error": "No autorizado" }` |
