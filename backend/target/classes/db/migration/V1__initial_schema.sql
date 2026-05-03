-- ============================================
-- Migration V1: Initial schema - All features
-- ============================================

-- 001-auth: Usuarios
CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    telefono VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100),
    codigo_verificacion VARCHAR(6),
    codigo_expiracion TIMESTAMP WITH TIME ZONE,
    codigo_intentos INT DEFAULT 0,
    verificado BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_usuarios_telefono ON usuarios(telefono);

-- 002-familias: Familias, miembros, invitaciones
CREATE TABLE familias (
    id UUID PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo_invitacion VARCHAR(20) UNIQUE,
    creado_por UUID NOT NULL REFERENCES usuarios(id),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE miembros_familia (
    id UUID PRIMARY KEY,
    familia_id UUID NOT NULL REFERENCES familias(id),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    rol VARCHAR(20) DEFAULT 'MIEMBRO' CHECK (rol IN ('ADMIN', 'MIEMBRO')),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(familia_id, usuario_id)
);

CREATE INDEX idx_miembros_familia_usuario ON miembros_familia(usuario_id);
CREATE INDEX idx_miembros_familia_familia ON miembros_familia(familia_id);

CREATE TABLE invitaciones (
    id UUID PRIMARY KEY,
    familia_id UUID NOT NULL REFERENCES familias(id),
    usuario_id UUID REFERENCES usuarios(id),
    telefono VARCHAR(20),
    codigo_invitacion VARCHAR(20) NOT NULL,
    estado VARCHAR(20) DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'EXPIRADA')),
    creado_por UUID NOT NULL REFERENCES usuarios(id),
    fecha_expiracion TIMESTAMP WITH TIME ZONE,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_respuesta TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_invitaciones_usuario ON invitaciones(usuario_id);
CREATE INDEX idx_invitaciones_familia ON invitaciones(familia_id);
CREATE INDEX idx_invitaciones_codigo ON invitaciones(codigo_invitacion);

-- 003-articulos-catalogo: Artículos, áreas
CREATE TABLE articulos (
    id UUID PRIMARY KEY,
    familia_id UUID NOT NULL REFERENCES familias(id),
    nombre VARCHAR(200) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    codigo_barras VARCHAR(50),
    cantidad_defecto DECIMAL(10,2) DEFAULT 1.00,
    activo BOOLEAN DEFAULT TRUE,
    creado_por UUID REFERENCES usuarios(id),
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(familia_id, sku)
);

CREATE INDEX idx_articulos_familia ON articulos(familia_id);
CREATE INDEX idx_articulos_codigo_barras ON articulos(codigo_barras);

CREATE TABLE areas_super (
    id UUID PRIMARY KEY,
    familia_id UUID NOT NULL REFERENCES familias(id),
    nombre VARCHAR(100) NOT NULL,
    orden INT DEFAULT 0,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_areas_super_familia ON areas_super(familia_id);

CREATE TABLE areas_casa (
    id UUID PRIMARY KEY,
    familia_id UUID NOT NULL REFERENCES familias(id),
    nombre VARCHAR(100) NOT NULL,
    orden INT DEFAULT 0,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_areas_casa_familia ON areas_casa(familia_id);

-- 004-listas: Listas e items
CREATE TABLE listas (
    id UUID PRIMARY KEY,
    familia_id UUID NOT NULL REFERENCES familias(id),
    nombre VARCHAR(200) NOT NULL,
    supermercado VARCHAR(100),
    estado VARCHAR(20) DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'EN_CURSO', 'COMPLETADA', 'CANCELADA')),
    total_estimado DECIMAL(10,2) DEFAULT 0.00,
    creado_por UUID NOT NULL REFERENCES usuarios(id),
    completada_por UUID REFERENCES usuarios(id),
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_completada TIMESTAMP WITH TIME ZONE,
    fecha_actualizacion TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_listas_familia ON listas(familia_id);
CREATE INDEX idx_listas_estado ON listas(estado);

CREATE TABLE items_lista (
    id UUID PRIMARY KEY,
    lista_id UUID NOT NULL REFERENCES listas(id),
    articulo_id UUID REFERENCES articulos(id),
    nombre_manual VARCHAR(200),
    cantidad DECIMAL(10,2) DEFAULT 1.00,
    cantidad_comprada DECIMAL(10,2),
    area_super_id UUID REFERENCES areas_super(id),
    area_casa_id UUID REFERENCES areas_casa(id),
    responsable_id UUID REFERENCES usuarios(id),
    notas TEXT,
    estado VARCHAR(20) DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'COMPRADO', 'NO_HUBO')),
    orden INT DEFAULT 0,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_items_lista_lista ON items_lista(lista_id);
CREATE INDEX idx_items_lista_articulo ON items_lista(articulo_id);

-- 005-flujo-compras: Pendientes e historial
CREATE TABLE lista_pendientes (
    id UUID PRIMARY KEY,
    familia_id UUID NOT NULL REFERENCES familias(id),
    nombre VARCHAR(200) DEFAULT 'Pendientes',
    creado_por UUID NOT NULL REFERENCES usuarios(id),
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_lista_pendientes_familia ON lista_pendientes(familia_id);

CREATE TABLE items_pendientes (
    id UUID PRIMARY KEY,
    lista_pendiente_id UUID NOT NULL REFERENCES lista_pendientes(id),
    articulo_id UUID REFERENCES articulos(id),
    nombre_manual VARCHAR(200),
    cantidad DECIMAL(10,2) DEFAULT 1.00,
    area_super_id UUID REFERENCES areas_super(id),
    area_casa_id UUID REFERENCES areas_casa(id),
    lista_origen_id UUID REFERENCES listas(id),
    estado VARCHAR(20) DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'RESUELTO')),
    agregado_por UUID REFERENCES usuarios(id),
    resuelto_por UUID REFERENCES usuarios(id),
    fecha_resolucion TIMESTAMP WITH TIME ZONE,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_items_pendientes_lista ON items_pendientes(lista_pendiente_id);
CREATE INDEX idx_items_pendientes_estado ON items_pendientes(estado);

-- ============================================
-- Seed data: Default areas for new families
-- ============================================

-- Areas del super (default)
CREATE TABLE areas_super_default (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    orden INT DEFAULT 0
);

INSERT INTO areas_super_default (nombre, orden) VALUES
    ('Frutas y Verduras', 1),
    ('Carnes', 2),
    ('Lácteos', 3),
    ('Panadería', 4),
    ('Limpieza', 5),
    ('Bebidas', 6),
    ('Despensa', 7),
    ('Congelados', 8),
    ('Otros', 9);

-- Areas de la casa (default)
CREATE TABLE areas_casa_default (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    orden INT DEFAULT 0
);

INSERT INTO areas_casa_default (nombre, orden) VALUES
    ('Cocina', 1),
    ('Despensa', 2),
    ('Baño', 3),
    ('Lavandería', 4),
    ('Habitaciones', 5),
    ('Jardín', 6);
