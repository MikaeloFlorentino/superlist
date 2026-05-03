# Quickstart: Familias

## Prerequisites

- Backend running on `http://localhost:8080`
- JWT token disponible (obtenido de verificación)

## Scenario 1: Crear familia

```bash
TOKEN="eyJ..."

curl -X POST http://localhost:8080/api/familias \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Casa de Juan"}'
# → 201 {"id":"...","nombre":"Casa de Juan","codigo_invitacion":"ABC123XY","miembros":1}
```

## Scenario 2: Listar familias

```bash
curl http://localhost:8080/api/familias \
  -H "Authorization: Bearer $TOKEN"
```

## Scenario 3: Invitar miembro

```bash
curl -X POST http://localhost:8080/api/familias/{id}/invitar \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"telefono": "+521234567891"}'
```

## Scenario 4: Aceptar invitación (desde otra cuenta)

```bash
TOKEN2="eyJ..." # token del usuario invitado

# Ver invitaciones
curl http://localhost:8080/api/invitaciones \
  -H "Authorization: Bearer $TOKEN2"

# Aceptar
curl -X POST http://localhost:8080/api/invitaciones/{invitacionId}/aceptar \
  -H "Authorization: Bearer $TOKEN2"
```

## Scenario 5: Unirse por código

```bash
curl -X POST http://localhost:8080/api/familias/unirse \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"codigo_invitacion": "ABC123XY"}'
```
