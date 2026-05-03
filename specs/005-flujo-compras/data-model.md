# Data Model: Flujo de Compras y Pendientes

## Entities

### ListaPendiente

| Campo | Tipo | Restricciones |
|-------|------|--------------|
| id | UUID | PK, autogenerado |
| familia_id | UUID | FK → familias.id, NOT NULL |
| nombre | String | NOT NULL, max 150, default 'Pendientes' |
| creado_por | UUID | FK → usuarios.id, nullable |
| fecha_creacion | Timestamp | autogenerado |

*Unique constraint:* (familia_id) — una por familia

### ItemPendiente

| Campo | Tipo | Restricciones |
|-------|------|--------------|
| id | UUID | PK, autogenerado |
| lista_pendiente_id | UUID | FK → lista_pendientes.id, NOT NULL |
| articulo_id | UUID | FK → articulos.id, nullable |
| nombre_manual | String | nullable, max 150 |
| cantidad | Decimal | NOT NULL, default 1.0 |
| area_super_id | UUID | FK → areas_super.id, nullable |
| area_casa_id | UUID | FK → areas_casa.id, nullable |
| lista_origen_id | UUID | FK → listas.id (de dónde vino el no hay), nullable |
| agregado_por | UUID | FK → usuarios.id |
| resuelto_por | UUID | FK → usuarios.id, nullable |
| estado | Enum | PENDIENTE, RESUELTO |
| fecha_creacion | Timestamp | autogenerado |
| fecha_resolucion | Timestamp | nullable |

### Relationships

```text
Familia 1──1 ListaPendiente
ListaPendiente 1──* ItemPendiente
ItemPendiente *──1 Articulo (nullable)
ItemPendiente *──1 Lista (origen, nullable)
```

## Database Migration

### V5__create_pendientes.sql

```sql
CREATE TYPE estado_pendiente AS ENUM ('PENDIENTE', 'RESUELTO');

CREATE TABLE lista_pendientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    familia_id UUID NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
    nombre VARCHAR(150) NOT NULL DEFAULT 'Pendientes',
    creado_por UUID REFERENCES usuarios(id),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(familia_id)
);

CREATE TABLE items_pendientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lista_pendiente_id UUID NOT NULL REFERENCES lista_pendientes(id) ON DELETE CASCADE,
    articulo_id UUID REFERENCES articulos(id),
    nombre_manual VARCHAR(150),
    cantidad DECIMAL(10,2) NOT NULL DEFAULT 1.00,
    area_super_id UUID REFERENCES areas_super(id),
    area_casa_id UUID REFERENCES areas_casa(id),
    lista_origen_id UUID REFERENCES listas(id),
    agregado_por UUID NOT NULL REFERENCES usuarios(id),
    resuelto_por UUID REFERENCES usuarios(id),
    estado estado_pendiente NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion TIMESTAMP
);

CREATE INDEX idx_items_pendientes_lista ON items_pendientes(lista_pendiente_id);
CREATE INDEX idx_items_pendientes_estado ON items_pendientes(lista_pendiente_id, estado);
```
