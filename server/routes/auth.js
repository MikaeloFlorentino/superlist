const express = require('express');
const router = express.Router();
const crypto = require('crypto');
const { getDb } = require('../db');
const { generateToken, authMiddleware } = require('../middleware/auth');

// Rate limiter: map telefono -> timestamps[]
const rateLimitMap = new Map();
const MAX_SOLICITUDES = 3;
const VENTANA_MINUTOS = 5;

function checkRateLimit(telefono) {
  const now = Date.now();
  const timestamps = rateLimitMap.get(telefono) || [];
  const recentes = timestamps.filter(t => (now - t) < VENTANA_MINUTOS * 60 * 1000);
  if (recentes.length >= MAX_SOLICITUDES) {
    return false;
  }
  recentes.push(now);
  rateLimitMap.set(telefono, recentes);
  return true;
}

// POST /api/auth/solicitar-codigo
router.post('/solicitar-codigo', (req, res) => {
  const { telefono } = req.body;
  if (!telefono) {
    return res.status(400).json({ error: 'Teléfono requerido' });
  }

  if (!checkRateLimit(telefono)) {
    return res.status(429).json({ error: 'Demasiadas solicitudes. Intenta en 5 minutos.' });
  }

  const db = getDb();

  // Find or create user
  let usuario = db.prepare('SELECT * FROM usuarios WHERE telefono = ?').get(telefono);
  if (!usuario) {
    const id = crypto.randomUUID();
    db.prepare('INSERT INTO usuarios (id, telefono) VALUES (?, ?)').run(id, telefono);
    usuario = db.prepare('SELECT * FROM usuarios WHERE telefono = ?').get(telefono);
  }

  // Generate 6-digit code
  const codigo = String(Math.floor(100000 + Math.random() * 900000));
  const expiracion = new Date(Date.now() + 10 * 60 * 1000).toISOString();

  db.prepare('UPDATE usuarios SET codigo_verificacion = ?, codigo_expiracion = ?, codigo_intentos = 0 WHERE id = ?')
    .run(codigo, expiracion, usuario.id);

  console.log(`[SMS-MVP] Código para ${telefono}: ${codigo}`);

  res.json({ mensaje: 'Código enviado' });
});

// POST /api/auth/verificar-codigo
router.post('/verificar-codigo', (req, res) => {
  const { telefono, codigo } = req.body;
  if (!telefono || !codigo) {
    return res.status(400).json({ error: 'Teléfono y código requeridos' });
  }

  const db = getDb();
  const usuario = db.prepare('SELECT * FROM usuarios WHERE telefono = ?').get(telefono);

  if (!usuario) {
    return res.status(401).json({ error: 'Código inválido' });
  }

  // Check expiration
  const ahora = new Date();
  const expiracion = new Date(usuario.codigo_expiracion + 'Z');
  if (ahora > expiracion) {
    return res.status(401).json({ error: 'Código inválido' });
  }

  // Check attempts
  if (usuario.codigo_intentos >= 5) {
    return res.status(401).json({ error: 'Código inválido' });
  }

  if (usuario.codigo_verificacion !== codigo) {
    db.prepare('UPDATE usuarios SET codigo_intentos = codigo_intentos + 1 WHERE id = ?').run(usuario.id);
    return res.status(401).json({ error: 'Código inválido' });
  }

  // Success
  db.prepare('UPDATE usuarios SET verificado = 1, codigo_verificacion = NULL, codigo_expiracion = NULL, codigo_intentos = 0 WHERE id = ?')
    .run(usuario.id);

  const token = generateToken({
    usuario_id: usuario.id,
    telefono: usuario.telefono,
    fecha_emision: new Date().toISOString()
  });

  res.json({
    token,
    usuario: {
      id: usuario.id,
      nombre: usuario.nombre,
      telefono: usuario.telefono
    }
  });
});

// GET /api/auth/perfil
router.get('/perfil', authMiddleware, (req, res) => {
  const db = getDb();
  const usuario = db.prepare('SELECT id, nombre, telefono, fecha_creacion FROM usuarios WHERE id = ?').get(req.usuario.usuario_id);
  if (!usuario) {
    return res.status(401).json({ error: 'No autorizado' });
  }
  res.json(usuario);
});

// PUT /api/auth/perfil
router.put('/perfil', authMiddleware, (req, res) => {
  const { nombre } = req.body;
  if (!nombre || !nombre.trim()) {
    return res.status(400).json({ error: 'El nombre no puede estar vacío' });
  }
  const db = getDb();
  db.prepare('UPDATE usuarios SET nombre = ?, fecha_actualizacion = datetime(\'now\') WHERE id = ?')
    .run(nombre.trim(), req.usuario.usuario_id);
  const usuario = db.prepare('SELECT id, nombre, telefono FROM usuarios WHERE id = ?').get(req.usuario.usuario_id);
  res.json(usuario);
});

// POST /api/auth/buscar-telefono (utility for invitations — find user by phone)
router.post('/buscar-telefono', authMiddleware, (req, res) => {
  const { telefono } = req.body;
  if (!telefono) {
    return res.status(400).json({ error: 'Teléfono requerido' });
  }
  const db = getDb();
  const usuario = db.prepare('SELECT id, nombre, telefono FROM usuarios WHERE telefono = ? AND verificado = 1').get(telefono);
  if (!usuario) {
    return res.json({ encontrado: false });
  }
  res.json({ encontrado: true, usuario });
});

module.exports = router;
