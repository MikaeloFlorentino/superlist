# Data Model: Artículos y Catálogo

## Entities

### Articulo

| Campo | Tipo | Restricciones |
|-------|------|--------------|
| id | UUID | PK, autogenerado |
| familia_id | UUID | FK → familias.id, NOT NULL |
| nombre | String | NOT NULL, max 150 |
| sku | String | NOT NULL, max 50 |
| codigo_barras | String | nullable, max 50 |
| cantidad_defecto | Decimal | NOT NULL, default 1.0 |
| creado_por | UUID | FK → usuarios.id, NOT NULL |
| activo | Boolean | NOT NULL, default true |
| fecha_creacion | Timestamp | autogenerado |
| fecha_actualizacion | Timestamp | autogenerado |

*Unique constraint:* (familia_id, sku)

### AreaSuper

| Campo | Tipo | Restricciones |
|-------|------|--------------|
| id | UUID | PK, autogenerado |
| familia_id | UUID | FK → familias.id, NOT NULL |
| nombre | String | NOT NULL, max 80 |
| orden | Integer | default 0 |

### AreaCasa

| Campo | Tipo | Restricciones |
|-------|------|--------------|
| id | UUID | PK, autogenerado |
| familia_id | UUID | FK → familias.id, NOT NULL |
| nombre | String | NOT NULL, max 80 |
| orden | Integer | default 0 |

### Relationships

```text
Familia 1──* Articulo
Familia 1──* AreaSuper
Familia 1──* AreaCasa
```

### Default Areas (seeded on family creation)

**Areas del Super:** Frutas y Verduras (1), Carnes (2), Lácteos (3), Panadería (4), Limpieza (5), Bebidas (6), Despensa (7), Congelados (8), Otros (99)

**Areas de la Casa:** Cocina (1), Baño (2), Recámara (3), Sala (4), Despensa (5), Otros (99)

## Database Migration

### V3__create_articulos.sql

```sql
CREATE TABLE articulos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    familia_id UUID NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
    nombre VARCHAR(150) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    codigo_barras VARCHAR(50),
    cantidad_defecto DECIMAL(10,2) NOT NULL DEFAULT 1.00,
    creado_por UUID NOT NULL REFERENCES usuarios(id),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(familia_id, sku)
);

CREATE TABLE areas_super (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    familia_id UUID NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
    nombre VARCHAR(80) NOT NULL,
    orden INTEGER DEFAULT 0
);

CREATE TABLE areas_casa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    familia_id UUID NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
    nombre VARCHAR(80) NOT NULL,
    orden INTEGER DEFAULT 0
);

CREATE INDEX idx_articulos_familia ON articulos(familia_id);
CREATE INDEX idx_articulos_sku ON articulos(familia_id, sku);
CREATE INDEX idx_articulos_barras ON articulos(codigo_barras);
CREATE INDEX idx_articulos_activo ON articulos(familia_id, activo);
CREATE INDEX idx_areas_super_familia ON areas_super(familia_id);
CREATE INDEX idx_areas_casa_familia ON areas_casa(familia_id);
```
