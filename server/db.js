const Database = require('better-sqlite3');
const path = require('path');
const crypto = require('crypto');

const DB_PATH = path.join(__dirname, '..', 'superlist.db');

let db;

function getDb() {
  if (!db) {
    db = new Database(DB_PATH);
    db.pragma('journal_mode = WAL');
    db.pragma('foreign_keys = ON');
    runMigrations();
  }
  return db;
}

function runMigrations() {
  db.exec(`
    -- 001-auth: usuarios
    CREATE TABLE IF NOT EXISTS usuarios (
      id TEXT PRIMARY KEY,
      nombre TEXT,
      telefono TEXT NOT NULL UNIQUE,
      codigo_verificacion TEXT,
      codigo_expiracion TEXT,
      codigo_intentos INTEGER DEFAULT 0,
      verificado INTEGER DEFAULT 0,
      fecha_creacion TEXT DEFAULT (datetime('now')),
      fecha_actualizacion TEXT DEFAULT (datetime('now'))
    );

    -- 002-familias
    CREATE TABLE IF NOT EXISTS familias (
      id TEXT PRIMARY KEY,
      nombre TEXT NOT NULL,
      codigo_invitacion TEXT NOT NULL UNIQUE,
      creada_por TEXT NOT NULL REFERENCES usuarios(id),
      fecha_creacion TEXT DEFAULT (datetime('now')),
      fecha_actualizacion TEXT DEFAULT (datetime('now'))
    );

    CREATE TABLE IF NOT EXISTS miembros_familia (
      id TEXT PRIMARY KEY,
      familia_id TEXT NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
      usuario_id TEXT NOT NULL REFERENCES usuarios(id),
      rol TEXT NOT NULL DEFAULT 'MIEMBRO' CHECK(rol IN ('ADMIN','MIEMBRO')),
      fecha_creacion TEXT DEFAULT (datetime('now')),
      UNIQUE(familia_id, usuario_id)
    );

    CREATE TABLE IF NOT EXISTS invitaciones (
      id TEXT PRIMARY KEY,
      familia_id TEXT NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
      usuario_id TEXT NOT NULL REFERENCES usuarios(id),
      estado TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK(estado IN ('PENDIENTE','ACEPTADA','RECHAZADA')),
      invitado_por TEXT NOT NULL REFERENCES usuarios(id),
      fecha_creacion TEXT DEFAULT (datetime('now')),
      fecha_respuesta TEXT
    );

    -- 003-articulos-catalogo
    CREATE TABLE IF NOT EXISTS articulos (
      id TEXT PRIMARY KEY,
      familia_id TEXT NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
      nombre TEXT NOT NULL,
      sku TEXT NOT NULL,
      codigo_barras TEXT,
      cantidad_defecto REAL NOT NULL DEFAULT 1.00,
      creado_por TEXT NOT NULL REFERENCES usuarios(id),
      activo INTEGER NOT NULL DEFAULT 1,
      fecha_creacion TEXT DEFAULT (datetime('now')),
      fecha_actualizacion TEXT DEFAULT (datetime('now')),
      UNIQUE(familia_id, sku)
    );

    CREATE TABLE IF NOT EXISTS areas_super (
      id TEXT PRIMARY KEY,
      familia_id TEXT NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
      nombre TEXT NOT NULL,
      orden INTEGER DEFAULT 0
    );

    CREATE TABLE IF NOT EXISTS areas_casa (
      id TEXT PRIMARY KEY,
      familia_id TEXT NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
      nombre TEXT NOT NULL,
      orden INTEGER DEFAULT 0
    );

    -- 004-listas
    CREATE TABLE IF NOT EXISTS listas (
      id TEXT PRIMARY KEY,
      familia_id TEXT NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
      nombre TEXT NOT NULL,
      supermercado TEXT,
      estado TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK(estado IN ('PENDIENTE','EN_CURSO','COMPLETADA','CANCELADA')),
      creado_por TEXT NOT NULL REFERENCES usuarios(id),
      completada_por TEXT REFERENCES usuarios(id),
      total_estimado REAL NOT NULL DEFAULT 0.00,
      fecha_creacion TEXT DEFAULT (datetime('now')),
      fecha_actualizacion TEXT DEFAULT (datetime('now')),
      fecha_completada TEXT
    );

    CREATE TABLE IF NOT EXISTS items_lista (
      id TEXT PRIMARY KEY,
      lista_id TEXT NOT NULL REFERENCES listas(id) ON DELETE CASCADE,
      articulo_id TEXT REFERENCES articulos(id),
      nombre_manual TEXT,
      cantidad REAL NOT NULL DEFAULT 1.00,
      cantidad_comprada REAL,
      area_super_id TEXT REFERENCES areas_super(id),
      area_casa_id TEXT REFERENCES areas_casa(id),
      responsable_id TEXT REFERENCES usuarios(id),
      estado TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK(estado IN ('PENDIENTE','COMPRADO','NO_HAY')),
      notas TEXT,
      orden INTEGER NOT NULL DEFAULT 0,
      fecha_creacion TEXT DEFAULT (datetime('now')),
      fecha_actualizacion TEXT DEFAULT (datetime('now'))
    );

    -- 005-flujo-compras: lista_pendientes + items_pendientes
    CREATE TABLE IF NOT EXISTS lista_pendientes (
      id TEXT PRIMARY KEY,
      familia_id TEXT NOT NULL REFERENCES familias(id) ON DELETE CASCADE,
      nombre TEXT NOT NULL DEFAULT 'Pendientes',
      creado_por TEXT REFERENCES usuarios(id),
      fecha_creacion TEXT DEFAULT (datetime('now')),
      UNIQUE(familia_id)
    );

    CREATE TABLE IF NOT EXISTS items_pendientes (
      id TEXT PRIMARY KEY,
      lista_pendiente_id TEXT NOT NULL REFERENCES lista_pendientes(id) ON DELETE CASCADE,
      articulo_id TEXT REFERENCES articulos(id),
      nombre_manual TEXT,
      cantidad REAL NOT NULL DEFAULT 1.00,
      area_super_id TEXT REFERENCES areas_super(id),
      area_casa_id TEXT REFERENCES areas_casa(id),
      lista_origen_id TEXT REFERENCES listas(id),
      agregado_por TEXT NOT NULL REFERENCES usuarios(id),
      resuelto_por TEXT REFERENCES usuarios(id),
      estado TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK(estado IN ('PENDIENTE','RESUELTO')),
      fecha_creacion TEXT DEFAULT (datetime('now')),
      fecha_resolucion TEXT
    );
  `);

  // Create indexes
  const indexes = [
    'CREATE INDEX IF NOT EXISTS idx_usuarios_telefono ON usuarios(telefono)',
    'CREATE INDEX IF NOT EXISTS idx_miembros_usuario ON miembros_familia(usuario_id)',
    'CREATE INDEX IF NOT EXISTS idx_miembros_familia ON miembros_familia(familia_id)',
    'CREATE INDEX IF NOT EXISTS idx_invitaciones_usuario ON invitaciones(usuario_id)',
    'CREATE INDEX IF NOT EXISTS idx_invitaciones_estado ON invitaciones(estado)',
    'CREATE INDEX IF NOT EXISTS idx_familias_codigo ON familias(codigo_invitacion)',
    'CREATE INDEX IF NOT EXISTS idx_articulos_familia ON articulos(familia_id)',
    'CREATE INDEX IF NOT EXISTS idx_articulos_sku ON articulos(familia_id, sku)',
    'CREATE INDEX IF NOT EXISTS idx_articulos_barras ON articulos(codigo_barras)',
    'CREATE INDEX IF NOT EXISTS idx_articulos_activo ON articulos(familia_id, activo)',
    'CREATE INDEX IF NOT EXISTS idx_areas_super_familia ON areas_super(familia_id)',
    'CREATE INDEX IF NOT EXISTS idx_areas_casa_familia ON areas_casa(familia_id)',
    'CREATE INDEX IF NOT EXISTS idx_listas_familia ON listas(familia_id)',
    'CREATE INDEX IF NOT EXISTS idx_listas_estado ON listas(familia_id, estado)',
    'CREATE INDEX IF NOT EXISTS idx_items_lista ON items_lista(lista_id)',
    'CREATE INDEX IF NOT EXISTS idx_items_estado ON items_lista(lista_id, estado)',
    'CREATE INDEX IF NOT EXISTS idx_items_pendientes_lista ON items_pendientes(lista_pendiente_id)',
    'CREATE INDEX IF NOT EXISTS idx_items_pendientes_estado ON items_pendientes(lista_pendiente_id, estado)',
  ];
  for (const sql of indexes) {
    try { db.exec(sql); } catch(e) { /* ignore duplicate */ }
  }

  // Seed default areas for familias that don't have them yet (run in migration)
  // We handle default areas in the familias route on create.
}

module.exports = { getDb };
