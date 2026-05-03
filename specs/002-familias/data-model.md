# Data Model: Familias

## Entities

### Familia

| Campo | Tipo | Restricciones |
|-------|------|--------------|
| id | UUID | PK, autogenerado |
| nombre | String | NOT NULL, max 100 |
| codigo_invitacion | String | UNIQUE, NOT NULL, 8 caracteres |
| creada_por | UUID | FK → usuarios.id |
| fecha_creacion | Timestamp | autogenerado |
| fecha_actualizacion | Timestamp | autogenerado |

### MiembroFamilia

| Campo | Tipo | Restricciones |
|-------|------|--------------|
| id | UUID | PK, autogenerado |
| familia_id | UUID | FK → familias.id, NOT NULL |
| usuario_id | UUID | FK → usuarios.id, NOT NULL |
| rol | Enum | ADMIN, MIEMBRO |
| fecha_creacion | Timestamp | autogenerado |

*Unique constraint:* (familia_id, usuario_id)

### Invitacion

| Campo | Tipo | Restricciones |
|-------|------|--------------|
| id | UUID | PK, autogenerado |
| familia_id | UUID | FK → familias.id, NOT NULL |
| usuario_id | UUID | FK → usuarios.id, NOT NULL |
| estado | Enum | PENDIENTE, ACEPTADA, RECHAZADA |
| invitado_por | UUID | FK → usuarios.id, NOT NULL |
| fecha_creacion | Timestamp | autogenerado |
| fecha_respuesta | Timestamp | nullable |

### Relationships

```text
Familia 1──* MiembroFamilia *──1 Usuario
Familia 1──* Invitacion     *──1 Usuario
```

## Database Migration

### V2__create_familias.sql

```sql
CREATE TYPE rol_miembro AS ENUM ('ADMIN', 'MIEMBRO');
CREATE TYPE estado_invitacion AS ENUM ('PENDIENTE', 'ACEPTADA', 'RECHAZADA');

CREATE TABLE familias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    codigo_invitacion VARCHAR(8) NOT NULL UNIQUE,
    creada_por UUID NOT NULL REFERENCES usuarios(id),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE miembros_familia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    familia_id UUID NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    rol rol_miembro NOT NULL DEFAULT 'MIEMBRO',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(familia_id, usuario_id)
);

CREATE TABLE invitaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    familia_id UUID NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    estado estado_invitacion NOT NULL DEFAULT 'PENDIENTE',
    invitado_por UUID NOT NULL REFERENCES usuarios(id),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_respuesta TIMESTAMP
);

CREATE INDEX idx_miembros_usuario ON miembros_familia(usuario_id);
CREATE INDEX idx_miembros_familia ON miembros_familia(familia_id);
CREATE INDEX idx_invitaciones_usuario ON invitaciones(usuario_id);
CREATE INDEX idx_invitaciones_estado ON invitaciones(estado);
CREATE INDEX idx_familias_codigo ON familias(codigo_invitacion);
```
