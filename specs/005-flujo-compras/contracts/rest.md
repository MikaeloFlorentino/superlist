# API Contracts: Flujo de Compras y Pendientes

Base URL: `/api`

## Modo Compra

### GET /api/listas/{id}/modo-compra

Vista de compra agrupada por área del supermercado.

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:**
```json
{
  "lista_id": "uuid",
  "lista_nombre": "Compra semanal",
  "supermercado": "Walmart",
  "estado": "EN_CURSO",
  "areas": [
    {
      "area_id": "uuid",
      "area_nombre": "Lácteos",
      "orden": 3,
      "items": [
        { "id": "uuid", "nombre": "Leche Lala 1L", "cantidad": 2.0, "estado": "PENDIENTE", "notas": "Bolsa azul" },
        { "id": "uuid", "nombre": "Yogurt Griego", "cantidad": 4.0, "estado": "COMPRADO" }
      ]
    },
    {
      "area_id": null,
      "area_nombre": "Sin área",
      "orden": 999,
      "items": [
        { "id": "uuid", "nombre": "Jabón de manos", "cantidad": 1.0, "estado": "PENDIENTE" }
      ]
    }
  ],
  "resumen": {
    "total": 18, "comprados": 5, "pendientes": 11, "no_hay": 2, "progreso": 27.78
  }
}
```

---

## Pendientes

### GET /api/familias/{familiaId}/pendientes

Ver items pendientes de la familia.

**Headers:** `Authorization: Bearer <jwt>`

**Query params:** `estado=PENDIENTE` (default)

**Response 200:**
```json
{
  "id": "uuid",
  "nombre": "Pendientes",
  "items": [
    {
      "id": "uuid",
      "articulo_id": "uuid",
      "articulo_nombre": "Leche Lala 1L",
      "nombre_manual": null,
      "cantidad": 2.0,
      "area_super_id": "uuid",
      "area_super_nombre": "Lácteos",
      "lista_origen_nombre": "Compra 02/05",
      "agregado_por_nombre": "Juan",
      "estado": "PENDIENTE",
      "fecha_creacion": "2025-01-01T00:00:00Z"
    }
  ],
  "total_pendientes": 5,
  "total_resueltos": 3
}
```

### GET /api/familias/{familiaId}/pendientes/total

Total de pendientes para la familia.

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:**
```json
{
  "total_pendientes": 5,
  "articulos_pendientes": ["Leche Lala 1L", "Jabón"]
}
```

### PATCH /api/pendientes/{id}/resolver

Marcar pendiente como resuelto.

**Headers:** `Authorization: Bearer <jwt>`

**Request:**
```json
{
  "agregar_a_lista_id": "uuid"
}
```

**Response 200:**
```json
{
  "id": "uuid",
  "estado": "RESUELTO",
  "agregado_a_lista": true
}
```

### POST /api/pendientes/{id}/mover-a-lista

Mover pendiente a una lista activa (sin resolverlo).

**Headers:** `Authorization: Bearer <jwt>`

**Request:**
```json
{
  "lista_id": "uuid"
}
```

**Response 201:**
```json
{
  "item_lista_id": "uuid",
  "mensaje": "Item agregado a la lista"
}
```

---

## Historial

### GET /api/familias/{familiaId}/listas/historial

Historial de listas completadas.

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:**
```json
[
  {
    "id": "uuid",
    "nombre": "Compra semanal",
    "supermercado": "Walmart",
    "estado": "COMPLETADA",
    "total_items": 15,
    "comprados": 12,
    "no_hay": 3,
    "completada_por_nombre": "María",
    "fecha_completada": "2025-01-01T00:00:00Z",
    "minutos_transcurridos": 45
  }
]
```

### GET /api/listas/{id}/historial

Detalle de lista completada (read-only).

**Headers:** `Authorization: Bearer <jwt>`

**Response 200:**
```json
{
  "id": "uuid",
  "nombre": "Compra semanal",
  "estado": "COMPLETADA",
  "items": [
    {
      "nombre": "Leche Lala 1L",
      "cantidad": 2.0,
      "cantidad_comprada": 2.0,
      "estado": "COMPRADO",
      "area_super_nombre": "Lácteos",
      "area_casa_nombre": "Cocina",
      "responsable_nombre": "María"
    },
    {
      "nombre": "Pan Bimbo",
      "cantidad": 1.0,
      "cantidad_comprada": null,
      "estado": "NO_HAY",
      "area_super_nombre": "Panadería"
    }
  ],
  "resumen": {
    "comprados": 12,
    "no_hay": 3,
    "total_items": 15,
    "minutos_transcurridos": 45,
    "completada_por": "María",
    "fecha_completada": "2025-01-01T00:00:00Z"
  }
}
```
