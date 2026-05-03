# API Contracts: Artículos y Catálogo

Base URL: `/api`

## Artículos

### POST /api/familias/{familiaId}/articulos

Crear un artículo en el catálogo de la familia.

**Headers:** `Authorization: Bearer <jwt>`

**Request:**
```json
{
  "nombre": "Leche Lala 1L",
  "sku": "LEC-001",
  "codigo_barras": "7501234567890",
  "cantidad_defecto": 2.0
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "nombre": "Leche Lala 1L",
  "sku": "LEC-001",
  "codigo_barras": "7501234567890",
  "cantidad_defecto": 2.0,
  "activo": true,
  "fecha_creacion": "2025-01-01T00:00:00Z"
}
```

### GET /api/familias/{familiaId}/articulos

Listar artículos del catálogo.

**Headers:** `Authorization: Bearer <jwt>`

**Query params:** `activos=true` (default), `q=leche` (búsqueda por nombre)

**Response 200:** Array de artículos.

### GET /api/articulos/{id}

Obtener artículo por ID.

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:** Artículo completo.

### GET /api/articulos/buscar/codigo?codigo=7501234567890

Buscar por código de barras en todas las familias del usuario.

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:**
```json
{
  "id": "uuid",
  "familia_id": "uuid",
  "nombre": "Leche Lala 1L",
  "sku": "LEC-001",
  "codigo_barras": "7501234567890",
  "cantidad_defecto": 2.0,
  "activo": true,
  "familia_nombre": "Casa de Juan"
}
```

**Response 404:** `{ "error": "Artículo no encontrado" }`

### GET /api/articulos/mis-familias?q=leche

Buscar artículos en todas las familias del usuario.

**Headers:** `Authorization: Bearer <jwt>`

**Query params:** `q` (búsqueda), `excluir_familia_id` (opcional)

**Response 200:** Array de artículos con `familia_nombre`.

### PUT /api/articulos/{id}

Actualizar artículo.

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:** Artículo actualizado.

### DELETE /api/articulos/{id}

Desactivar artículo (soft delete).

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:** `{ "mensaje": "Artículo desactivado" }`

### POST /api/familias/{familiaId}/articulos/importar

Importar artículo desde otra familia.

**Headers:** `Authorization: Bearer <jwt>`

**Request:**
```json
{
  "articulo_origen_id": "uuid",
  "familia_origen_id": "uuid"
}
```

**Response 201:** Artículo copiado con posible sufijo "-copia" en SKU.

---

## Áreas

### GET /api/familias/{familiaId}/areas-super
### POST /api/familias/{familiaId}/areas-super
### DELETE /api/familias/{familiaId}/areas-super/{id}

**Headers:** `Authorization: Bearer <jwt>`

**POST data:** `{ "nombre": "Carnes", "orden": 1 }`

### GET /api/familias/{familiaId}/areas-casa
### POST /api/familias/{familiaId}/areas-casa
### DELETE /api/familias/{familiaId}/areas-casa/{id}

**Headers:** `Authorization: Bearer <jwt>`

**POST data:** `{ "nombre": "Cocina", "orden": 1 }`
