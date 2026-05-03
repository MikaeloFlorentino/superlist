# Data Model: Usuarios y Autenticación

## Entities

### Usuario

| Campo | Tipo | Restricciones |
|-------|------|--------------|
| id | UUID | PK, autogenerado |
| nombre | String | max 100 |
| telefono | String | UNIQUE, NOT NULL, formato E.164 |
| codigo_verificacion | String | nullable, 6 dígitos |
| codigo_expiracion | Timestamp | nullable |
| codigo_intentos | Integer | default 0 |
| verificado | Boolean | default false |
| fecha_creacion | Timestamp | autogenerado |
| fecha_actualizacion | Timestamp | autogenerado |

### Relationships

```text
Usuario ── (no foreign keys to other entities in this spec)
         ── tiene 0..* solicitudes de código (rate limiting via timestamps)
```

### Rate Limiting Data

Rate limiting se implementa en memoria o en la entidad misma (`codigo_intentos`). Para el rate limit de 3 solicitudes/5min se puede usar:
- Un mapa en memoria: `Map<String, List<Instant>>` con teléfono → timestamps de solicitudes
- O cache con expiración (Caffeine/Guava)

Para MVP se usa un bean `CodeRateLimiter` con mapa en memoria.

## Database Migration

### V1__create_usuarios.sql

```sql
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100),
    telefono VARCHAR(20) NOT NULL UNIQUE,
    codigo_verificacion VARCHAR(6),
    codigo_expiracion TIMESTAMP,
    codigo_intentos INTEGER DEFAULT 0,
    verificado BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuarios_telefono ON usuarios(telefono);
```
