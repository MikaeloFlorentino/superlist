# API Contracts: Familias

Base URL: `/api`

## POST /api/familias

Crear una nueva familia. El creador se agrega automáticamente como ADMIN.

### Headers

```
Authorization: Bearer <jwt>
```

### Request

```json
{
  "nombre": "Casa de Juan"
}
```

### Response 201

```json
{
  "id": "uuid",
  "nombre": "Casa de Juan",
  "codigo_invitacion": "ABC123XY",
  "miembros": 1,
  "fecha_creacion": "2025-01-01T00:00:00Z"
}
```

---

## GET /api/familias

Listar familias del usuario autenticado.

### Headers

```
Authorization: Bearer <jwt>
```

### Response 200

```json
[
  {
    "id": "uuid",
    "nombre": "Casa de Juan",
    "codigo_invitacion": "ABC123XY",
    "miembros": 3,
    "rol": "ADMIN",
    "fecha_creacion": "2025-01-01T00:00:00Z"
  },
  {
    "id": "uuid",
    "nombre": "Familia Pérez",
    "codigo_invitacion": "DEF456WZ",
    "miembros": 2,
    "rol": "MIEMBRO",
    "fecha_creacion": "2025-02-15T00:00:00Z"
  }
]
```

---

## GET /api/familias/{id}

Obtener detalle de una familia con sus miembros.

### Headers

```
Authorization: Bearer <jwt>
```

### Response 200

```json
{
  "id": "uuid",
  "nombre": "Casa de Juan",
  "codigo_invitacion": "ABC123XY",
  "miembros": [
    {
      "usuario_id": "uuid",
      "nombre": "Juan Pérez",
      "telefono": "+521234567890",
      "rol": "ADMIN"
    },
    {
      "usuario_id": "uuid",
      "nombre": "María López",
      "telefono": "+521234567891",
      "rol": "MIEMBRO"
    }
  ],
  "fecha_creacion": "2025-01-01T00:00:00Z"
}
```

### Errors

| Status | Body |
|--------|------|
| 403 | `{ "error": "No eres miembro de esta familia" }` |
| 404 | `{ "error": "Familia no encontrada" }` |

---

## POST /api/familias/{id}/invitar

Invitar a un usuario por teléfono. Solo ADMIN puede invitar.

### Headers

```
Authorization: Bearer <jwt>
```

### Request

```json
{
  "telefono": "+521234567891"
}
```

### Response 200

```json
{
  "mensaje": "Invitación enviada a +521234567891"
}
```

### Errors

| Status | Body |
|--------|------|
| 400 | `{ "error": "El usuario no está registrado" }` |
| 400 | `{ "error": "El usuario ya es miembro de esta familia" }` |
| 400 | `{ "error": "Ya existe una invitación pendiente para este usuario" }` |
| 403 | `{ "error": "Solo los ADMIN pueden invitar" }` |

---

## POST /api/familias/unirse

Unirse a una familia usando su código de invitación.

### Headers

```
Authorization: Bearer <jwt>
```

### Request

```json
{
  "codigo_invitacion": "ABC123XY"
}
```

### Response 200

```json
{
  "mensaje": "Te has unido a la familia",
  "familia_id": "uuid",
  "familia_nombre": "Casa de Juan"
}
```

### Errors

| Status | Body |
|--------|------|
| 400 | `{ "error": "Código de invitación inválido" }` |
| 400 | `{ "error": "Ya eres miembro de esta familia" }` |

---

## GET /api/invitaciones

Ver invitaciones pendientes del usuario autenticado.

### Headers

```
Authorization: Bearer <jwt>
```

### Response 200

```json
[
  {
    "id": "uuid",
    "familia_id": "uuid",
    "familia_nombre": "Casa de Juan",
    "invitado_por": "Juan Pérez",
    "fecha_creacion": "2025-03-01T00:00:00Z"
  }
]
```

---

## POST /api/invitaciones/{id}/aceptar

Aceptar una invitación.

### Headers

```
Authorization: Bearer <jwt>
```

### Response 200

```json
{
  "mensaje": "Invitación aceptada",
  "familia_id": "uuid",
  "familia_nombre": "Casa de Juan"
}
```

### Errors

| Status | Body |
|--------|------|
| 403 | `{ "error": "Esta invitación no es para ti" }` |

---

## POST /api/invitaciones/{id}/rechazar

Rechazar una invitación.

### Headers

```
Authorization: Bearer <jwt>
```

### Response 200

```json
{
  "mensaje": "Invitación rechazada"
}
```

### Errors

| Status | Body |
|--------|------|
| 403 | `{ "error": "Esta invitación no es para ti" }` |
