# Data Model: Listas del Super

## Entities

### Lista

| Campo | Tipo | Restricciones |
|-------|------|--------------|
| id | UUID | PK, autogenerado |
| familia_id | UUID | FK → familias.id, NOT NULL |
| nombre | String | NOT NULL, max 150 |
| supermercado | String | nullable, max 100 |
| estado | Enum | PENDIENTE, EN_CURSO, COMPLETADA, CANCELADA |
| creado_por | UUID | FK → usuarios.id, NOT NULL |
| completada_por | UUID | FK → usuarios.id, nullable |
| total_estimado | Decimal | NOT NULL, default 0 |
| fecha_creacion | Timestamp | autogenerado |
| fecha_actualizacion | Timestamp | autogenerado |
| fecha_completada | Timestamp | nullable |

### ItemLista

| Campo | Tipo | Restricciones |
|-------|------|--------------|
| id | UUID | PK, autogenerado |
| lista_id | UUID | FK → listas.id, NOT NULL |
| articulo_id | UUID | FK → articulos.id, nullable |
| nombre_manual | String | nullable, max 150 |
| cantidad | Decimal | NOT NULL, default 1.0 |
| cantidad_comprada | Decimal | nullable |
| area_super_id | UUID | FK → areas_super.id, nullable |
| area_casa_id | UUID | FK → areas_casa.id, nullable |
| responsable_id | UUID | FK → usuarios.id, nullable |
| estado | Enum | PENDIENTE, COMPRADO, NO_HAY |
| notas | String | nullable, max 500 |
| orden | Integer | NOT NULL, default 0 |
| fecha_creacion | Timestamp | autogenerado |
| fecha_actualizacion | Timestamp | autogenerado |

*Validation:* Al menos `articulo_id` o `nombre_manual` debe estar presente.

### Relationships

```text
Familia 1──* Lista
Lista   1──* ItemLista
ItemLista *──1 Articulo (nullable)
ItemLista *──1 AreaSuper (nullable)
ItemLista *──1 AreaCasa (nullable)
```

## Database Migration

### V4__create_listas.sql

```sql
CREATE TYPE estado_lista AS ENUM ('PENDIENTE', 'EN_CURSO', 'COMPLETADA', 'CANCELADA');
CREATE TYPE estado_item AS ENUM ('PENDIENTE', 'COMPRADO', 'NO_HAY');

CREATE TABLE listas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    familia_id UUID NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
    nombre VARCHAR(150) NOT NULL,
    supermercado VARCHAR(100),
    estado estado_lista NOT NULL DEFAULT 'PENDIENTE',
    creado_por UUID NOT NULL REFERENCES usuarios(id),
    completada_por UUID REFERENCES usuarios(id),
    total_estimado DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_completada TIMESTAMP
);

CREATE TABLE items_lista (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lista_id UUID NOT NULL REFERENCES listas(id) ON DELETE CASCADE,
    articulo_id UUID REFERENCES articulos(id),
    nombre_manual VARCHAR(150),
    cantidad DECIMAL(10,2) NOT NULL DEFAULT 1.00,
    cantidad_comprada DECIMAL(10,2),
    area_super_id UUID REFERENCES areas_super(id),
    area_casa_id UUID REFERENCES areas_casa(id),
    responsable_id UUID REFERENCES usuarios(id),
    estado estado_item NOT NULL DEFAULT 'PENDIENTE',
    notas VARCHAR(500),
    orden INTEGER NOT NULL DEFAULT 0,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_listas_familia ON listas(familia_id);
CREATE INDEX idx_listas_estado ON listas(familia_id, estado);
CREATE INDEX idx_items_lista ON items_lista(lista_id);
CREATE INDEX idx_items_estado ON items_lista(lista_id, estado);
```
