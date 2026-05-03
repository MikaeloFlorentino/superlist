# Quickstart: Artículos y Catálogo

## Prerequisites

- Backend running, JWT token disponible

## Scenario 1: Crear artículo

```bash
TOKEN="eyJ..."
FAMILIA_ID="uuid-de-la-familia"

curl -X POST "http://localhost:8080/api/familias/$FAMILIA_ID/articulos" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Leche Lala 1L","sku":"LEC-001","codigo_barras":"7501234567890","cantidad_defecto":2.0}'
```

## Scenario 2: Buscar artículos

```bash
curl "http://localhost:8080/api/familias/$FAMILIA_ID/articulos?q=leche" \
  -H "Authorization: Bearer $TOKEN"

# Por código de barras
curl "http://localhost:8080/api/articulos/buscar/codigo?codigo=7501234567890" \
  -H "Authorization: Bearer $TOKEN"
```

## Scenario 3: Ver áreas por defecto

```bash
curl "http://localhost:8080/api/familias/$FAMILIA_ID/areas-super" \
  -H "Authorization: Bearer $TOKEN"
# → 9 áreas (Frutas y Verduras, Carnes, Lácteos...)
```

## Scenario 4: Importar artículo

```bash
curl -X POST "http://localhost:8080/api/familias/$FAMILIA_DESTINO/articulos/importar" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"articulo_origen_id":"uuid-origen","familia_origen_id":"uuid-familia-origen"}'
```
