const express = require('express');
const router = express.Router();
const crypto = require('crypto');
const { getDb } = require('../db');
const { authMiddleware } = require('../middleware/auth');

// Helper: generate 8-char invitation code
function generarCodigo() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  let code = '';
  for (let i = 0; i < 8; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return code;
}

// Helper: seed default areas for a family
function seedDefaultAreas(db, familiaId) {
  const superAreas = [
    { nombre: 'Frutas y Verduras', orden: 1 },
    { nombre: 'Carnes', orden: 2 },
    { nombre: 'Lácteos', orden: 3 },
    { nombre: 'Panadería', orden: 4 },
    { nombre: 'Limpieza', orden: 5 },
    { nombre: 'Bebidas', orden: 6 },
    { nombre: 'Despensa', orden: 7 },
    { nombre: 'Congelados', orden: 8 },
    { nombre: 'Otros', orden: 99 },
  ];
  const casaAreas = [
    { nombre: 'Cocina', orden: 1 },
    { nombre: 'Baño', orden: 2 },
    { nombre: 'Recámara', orden: 3 },
    { nombre: 'Sala', orden: 4 },
    { nombre: 'Despensa', orden: 5 },
    { nombre: 'Otros', orden: 99 },
  ];
  const insertSuper = db.prepare('INSERT INTO areas_super (id, familia_id, nombre, orden) VALUES (?, ?, ?, ?)');
  const insertCasa = db.prepare('INSERT INTO areas_casa (id, familia_id, nombre, orden) VALUES (?, ?, ?, ?)');
  for (const a of superAreas) {
    insertSuper.run(crypto.randomUUID(), familiaId, a.nombre, a.orden);
  }
  for (const a of casaAreas) {
    insertCasa.run(crypto.randomUUID(), familiaId, a.nombre, a.orden);
  }
}

// POST /api/familias
router.post('/familias', authMiddleware, (req, res) => {
  const { nombre } = req.body;
  if (!nombre || !nombre.trim()) {
    return res.status(400).json({ error: 'Nombre requerido' });
  }

  const db = getDb();
  const id = crypto.randomUUID();
  const codigo = generarCodigo();
  const usuarioId = req.usuario.usuario_id;

  const insertFamilia = db.prepare('INSERT INTO familias (id, nombre, codigo_invitacion, creada_por) VALUES (?, ?, ?, ?)');
  const insertMiembro = db.prepare('INSERT INTO miembros_familia (id, familia_id, usuario_id, rol) VALUES (?, ?, ?, ?)');

  const transaction = db.transaction(() => {
    insertFamilia.run(id, nombre.trim(), codigo, usuarioId);
    insertMiembro.run(crypto.randomUUID(), id, usuarioId, 'ADMIN');
    seedDefaultAreas(db, id);
  });
  transaction();

  res.status(201).json({
    id,
    nombre: nombre.trim(),
    codigo_invitacion: codigo,
    miembros: 1,
    fecha_creacion: db.prepare('SELECT fecha_creacion FROM familias WHERE id = ?').get(id).fecha_creacion
  });
});

// GET /api/familias
router.get('/familias', authMiddleware, (req, res) => {
  const db = getDb();
  const rows = db.prepare(`
    SELECT f.id, f.nombre, f.codigo_invitacion, f.fecha_creacion,
           mf.rol,
           (SELECT COUNT(*) FROM miembros_familia WHERE familia_id = f.id) as miembros
    FROM familias f
    JOIN miembros_familia mf ON mf.familia_id = f.id AND mf.usuario_id = ?
    ORDER BY f.fecha_creacion DESC
  `).all(req.usuario.usuario_id);
  res.json(rows);
});

// GET /api/familias/:id
router.get('/familias/:id', authMiddleware, (req, res) => {
  const db = getDb();
  const familia = db.prepare('SELECT * FROM familias WHERE id = ?').get(req.params.id);
  if (!familia) {
    return res.status(404).json({ error: 'Familia no encontrada' });
  }

  // Check membership
  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(req.params.id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const miembros = db.prepare(`
    SELECT mf.usuario_id, u.nombre, u.telefono, mf.rol
    FROM miembros_familia mf
    JOIN usuarios u ON u.id = mf.usuario_id
    WHERE mf.familia_id = ?
  `).all(req.params.id);

  res.json({
    id: familia.id,
    nombre: familia.nombre,
    codigo_invitacion: familia.codigo_invitacion,
    miembros,
    fecha_creacion: familia.fecha_creacion
  });
});

// POST /api/familias/:id/invitar
router.post('/familias/:id/invitar', authMiddleware, (req, res) => {
  const db = getDb();
  const familiaId = req.params.id;

  // Check membership + admin
  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(familiaId, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }
  if (miembro.rol !== 'ADMIN') {
    return res.status(403).json({ error: 'Solo los ADMIN pueden invitar' });
  }

  const { telefono } = req.body;
  if (!telefono) {
    return res.status(400).json({ error: 'Teléfono requerido' });
  }

  // Find user by phone
  const invitado = db.prepare('SELECT id, nombre FROM usuarios WHERE telefono = ? AND verificado = 1').get(telefono);
  if (!invitado) {
    return res.status(400).json({ error: 'El usuario no está registrado' });
  }

  // Check not already a member
  const yaMiembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(familiaId, invitado.id);
  if (yaMiembro) {
    return res.status(400).json({ error: 'El usuario ya es miembro de esta familia' });
  }

  // Check pending invitation
  const pendiente = db.prepare('SELECT * FROM invitaciones WHERE familia_id = ? AND usuario_id = ? AND estado = ?')
    .get(familiaId, invitado.id, 'PENDIENTE');
  if (pendiente) {
    return res.status(400).json({ error: 'Ya existe una invitación pendiente para este usuario' });
  }

  // Create invitation
  const id = crypto.randomUUID();
  db.prepare('INSERT INTO invitaciones (id, familia_id, usuario_id, invitado_por) VALUES (?, ?, ?, ?)')
    .run(id, familiaId, invitado.id, req.usuario.usuario_id);

  res.json({ mensaje: `Invitación enviada a ${telefono}` });
});

// POST /api/familias/unirse
router.post('/familias/unirse', authMiddleware, (req, res) => {
  const { codigo_invitacion } = req.body;
  if (!codigo_invitacion) {
    return res.status(400).json({ error: 'Código de invitación requerido' });
  }

  const db = getDb();
  const familia = db.prepare('SELECT * FROM familias WHERE codigo_invitacion = ?').get(codigo_invitacion);
  if (!familia) {
    return res.status(400).json({ error: 'Código de invitación inválido' });
  }

  // Check not already a member
  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(familia.id, req.usuario.usuario_id);
  if (miembro) {
    return res.status(400).json({ error: 'Ya eres miembro de esta familia' });
  }

  db.prepare('INSERT INTO miembros_familia (id, familia_id, usuario_id, rol) VALUES (?, ?, ?, ?)')
    .run(crypto.randomUUID(), familia.id, req.usuario.usuario_id, 'MIEMBRO');

  res.json({
    mensaje: 'Te has unido a la familia',
    familia_id: familia.id,
    familia_nombre: familia.nombre
  });
});

// GET /api/invitaciones
router.get('/invitaciones', authMiddleware, (req, res) => {
  const db = getDb();
  const rows = db.prepare(`
    SELECT i.id, i.familia_id, f.nombre as familia_nombre,
           u.nombre as invitado_por, i.fecha_creacion
    FROM invitaciones i
    JOIN familias f ON f.id = i.familia_id
    JOIN usuarios u ON u.id = i.invitado_por
    WHERE i.usuario_id = ? AND i.estado = 'PENDIENTE'
    ORDER BY i.fecha_creacion DESC
  `).all(req.usuario.usuario_id);
  res.json(rows);
});

// POST /api/invitaciones/:id/aceptar
router.post('/invitaciones/:id/aceptar', authMiddleware, (req, res) => {
  const db = getDb();
  const invitacion = db.prepare('SELECT * FROM invitaciones WHERE id = ?').get(req.params.id);
  if (!invitacion || invitacion.usuario_id !== req.usuario.usuario_id) {
    return res.status(403).json({ error: 'Esta invitación no es para ti' });
  }
  if (invitacion.estado !== 'PENDIENTE') {
    return res.status(400).json({ error: 'Invitación ya procesada' });
  }

  const transaction = db.transaction(() => {
    db.prepare('UPDATE invitaciones SET estado = ?, fecha_respuesta = datetime(\'now\') WHERE id = ?')
      .run('ACEPTADA', req.params.id);
    db.prepare('INSERT INTO miembros_familia (id, familia_id, usuario_id, rol) VALUES (?, ?, ?, ?)')
      .run(crypto.randomUUID(), invitacion.familia_id, req.usuario.usuario_id, 'MIEMBRO');
  });
  transaction();

  const familia = db.prepare('SELECT id, nombre FROM familias WHERE id = ?').get(invitacion.familia_id);
  res.json({
    mensaje: 'Invitación aceptada',
    familia_id: familia.id,
    familia_nombre: familia.nombre
  });
});

// POST /api/invitaciones/:id/rechazar
router.post('/invitaciones/:id/rechazar', authMiddleware, (req, res) => {
  const db = getDb();
  const invitacion = db.prepare('SELECT * FROM invitaciones WHERE id = ?').get(req.params.id);
  if (!invitacion || invitacion.usuario_id !== req.usuario.usuario_id) {
    return res.status(403).json({ error: 'Esta invitación no es para ti' });
  }

  db.prepare('UPDATE invitaciones SET estado = ?, fecha_respuesta = datetime(\'now\') WHERE id = ?')
    .run('RECHAZADA', req.params.id);

  res.json({ mensaje: 'Invitación rechazada' });
});

module.exports = router;
