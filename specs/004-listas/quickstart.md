# Quickstart: Listas del Super

## Prerequisites

- Backend running, JWT token disponible, familia creada

## Scenario 1: Ciclo completo de lista

```bash
TOKEN="eyJ..."
FAMILIA_ID="uuid"
ARTICULO_ID="uuid-de-leche"
AREA_SUPER_ID="uuid-de-lacteos"

# 1. Crear lista
curl -X POST "http://localhost:8080/api/familias/$FAMILIA_ID/listas" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Compra semanal","supermercado":"Walmart"}'
# → 201 {"id":"lista-uuid", "estado":"PENDIENTE"}

# 2. Agregar items
LISTA_ID="lista-uuid"
curl -X POST "http://localhost:8080/api/listas/$LISTA_ID/items" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"articulo_id\":\"$ARTICULO_ID\",\"cantidad\":2.0,\"area_super_id\":\"$AREA_SUPER_ID\"}"

# 3. Iniciar lista
curl -X PATCH "http://localhost:8080/api/listas/$LISTA_ID/estado" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"estado":"EN_CURSO"}'

# 4. Marcar item como COMPRADO
ITEM_ID="item-uuid"
curl -X PATCH "http://localhost:8080/api/items/$ITEM_ID/estado" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"estado":"COMPRADO"}'

# 5. Ver progreso
curl "http://localhost:8080/api/listas/$LISTA_ID/total" \
  -H "Authorization: Bearer $TOKEN"

# 6. Completar lista
curl -X PATCH "http://localhost:8080/api/listas/$LISTA_ID/estado" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"estado":"COMPLETADA"}'
```
