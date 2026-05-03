# Quickstart: Flujo de Compras y Pendientes

## Prerequisites

- Backend running, JWT, familia con artículos y una lista con items

## Scenario 1: Modo compra

```bash
TOKEN="eyJ..."
LISTA_ID="uuid"

curl "http://localhost:8080/api/listas/$LISTA_ID/modo-compra" \
  -H "Authorization: Bearer $TOKEN"
# → Items agrupados por área del super
```

## Scenario 2: Marcar NO_HAY → pendiente automático

```bash
ITEM_ID="uuid-del-item-en-lista"

curl -X PATCH "http://localhost:8080/api/items/$ITEM_ID/estado" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"estado":"NO_HAY"}'
# → {"mensaje":"Item marcado como no disponible","agregado_a_pendientes":true,"item_pendiente_id":"uuid"}
```

## Scenario 3: Ver pendientes

```bash
FAMILIA_ID="uuid"
curl "http://localhost:8080/api/familias/$FAMILIA_ID/pendientes" \
  -H "Authorization: Bearer $TOKEN"
```

## Scenario 4: Resolver pendiente

```bash
PENDIENTE_ID="uuid"
curl -X PATCH "http://localhost:8080/api/pendientes/$PENDIENTE_ID/resolver" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
# → {"id":"uuid","estado":"RESUELTO","agregado_a_lista":false}
```

## Scenario 5: Historial de compras

```bash
curl "http://localhost:8080/api/familias/$FAMILIA_ID/listas/historial" \
  -H "Authorization: Bearer $TOKEN"
```
