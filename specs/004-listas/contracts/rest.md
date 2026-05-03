# API Contracts: Listas del Super

Base URL: `/api`

## POST /api/familias/{familiaId}/listas

Crear nueva lista de compras.

**Headers:** `Authorization: Bearer <jwt>`

**Request:**
```json
{
  "nombre": "Compra semanal",
  "supermercado": "Walmart"
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "familia_id": "uuid",
  "nombre": "Compra semanal",
  "supermercado": "Walmart",
  "estado": "PENDIENTE",
  "total_estimado": 0.00,
  "items_count": 0,
  "creado_por": "uuid",
  "fecha_creacion": "2025-01-01T00:00:00Z"
}
```

## GET /api/familias/{familiaId}/listas

Listar listas. Filtro opcional `estado`.

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:** Array de listas con items_count e items_completados.

## GET /api/listas/{id}

Detalle de lista con todos sus items.

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:** Lista con items (articulo_nombre, area_super_nombre, area_casa_nombre, responsable_nombre).

## POST /api/listas/{listaId}/items

Agregar item a la lista.

**Headers:** `Authorization: Bearer <jwt>`

**Request (catálogo):**
```json
{
  "articulo_id": "uuid",
  "cantidad": 2.0,
  "area_super_id": "uuid",
  "area_casa_id": "uuid",
  "responsable_id": "uuid",
  "notas": "La de la bolsa azul"
}
```

**Request (manual):**
```json
{
  "nombre_manual": "Jabón de manos",
  "cantidad": 1.0,
  "area_super_id": "uuid",
  "area_casa_id": "uuid",
  "responsable_id": "uuid",
  "notas": "Aroma vainilla"
}
```

**Response 201:** Item creado con orden asignado.

## PUT /api/items/{id}

Actualizar item.

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:** Item actualizado.

## PATCH /api/items/{id}/estado

Cambiar estado del item.

**Headers:** `Authorization: Bearer <jwt>`

**Request:**
```json
{ "estado": "COMPRADO" }
{ "estado": "COMPRADO", "cantidad_comprada": 2.0 }
{ "estado": "NO_HAY" }
{ "estado": "PENDIENTE" }
```

**Response 200:** `{ "id": "uuid", "estado": "COMPRADO" }`

## DELETE /api/items/{id}

Eliminar item (solo si lista no está COMPLETADA/CANCELADA).

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:** `{ "mensaje": "Item eliminado" }`

## PATCH /api/listas/{id}/estado

Cambiar estado de la lista.

**Headers:** `Authorization: Bearer <jwt>`

**Request:**
```json
{ "estado": "EN_CURSO" }
{ "estado": "COMPLETADA" }
{ "estado": "CANCELADA" }
```

**Response 200:** `{ "id": "uuid", "estado": "EN_CURSO" }`

## PUT /api/listas/{id}/items/reordenar

Reordenar items.

**Headers:** `Authorization: Bearer <jwt>`

**Request:**
```json
{
  "orden_items": ["item-id-1", "item-id-2", "item-id-3"]
}
```

**Response 200:** Items reordenados.

## GET /api/listas/{id}/total

Obtener resumen de la lista.

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:**
```json
{
  "total_items": 15,
  "pendientes": 8,
  "comprados": 5,
  "no_hay": 2,
  "progreso": 33.33
}
```
