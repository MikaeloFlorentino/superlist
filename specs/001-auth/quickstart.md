# Quickstart: Usuarios y Autenticación

## Prerequisites

- PostgreSQL running
- Backend started: `cd backend && mvn spring-boot:run`
- Codes aparecen en consola del backend

## Scenario 1: Registro feliz

```bash
# 1. Solicitar código
curl -X POST http://localhost:8080/api/auth/solicitar-codigo \
  -H "Content-Type: application/json" \
  -d '{"telefono": "+521234567890"}'
# → {"mensaje": "Código enviado"}
# (revisar consola para el código: "Código de verificación para +521234567890: 123456")

# 2. Verificar código
curl -X POST http://localhost:8080/api/auth/verificar-codigo \
  -H "Content-Type: application/json" \
  -d '{"telefono": "+521234567890", "codigo": "123456"}'
# → {"token": "eyJ...", "usuario": {"id": "...", "nombre": null, "telefono": "+521234567890"}}

# 3. Obtener perfil
curl -X GET http://localhost:8080/api/auth/perfil \
  -H "Authorization: Bearer eyJ..."

# 4. Actualizar nombre
curl -X PUT http://localhost:8080/api/auth/perfil \
  -H "Authorization: Bearer eyJ..." \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Juan Pérez"}'
```

## Scenario 2: Código inválido

```bash
curl -X POST http://localhost:8080/api/auth/verificar-codigo \
  -H "Content-Type: application/json" \
  -d '{"telefono": "+521234567890", "codigo": "000000"}'
# → 401 {"error": "Código inválido"}
```

## Scenario 3: Rate limiting

```bash
# 4 solicitudes rápidas
for i in 1 2 3 4; do
  curl -s -X POST http://localhost:8080/api/auth/solicitar-codigo \
    -H "Content-Type: application/json" \
    -d '{"telefono": "+521234567890"}'
  echo ""
done
# La 4ta → 429 {"error": "Demasiadas solicitudes..."}
```
