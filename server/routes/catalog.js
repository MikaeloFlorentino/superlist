const express = require('express');
const router = express.Router();
const crypto = require('crypto');
const { getDb } = require('../db');
const { authMiddleware } = require('../middleware/auth');

// ────────────────────────────────────────────
// Artículos
// ────────────────────────────────────────────

// POST /api/familias/:familiaId/articulos
router.post('/familias/:familiaId/articulos', authMiddleware, (req, res) => {
  const db = getDb();
  const familiaId = req.params.familiaId;

  // Check membership
  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(familiaId, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const { nombre, sku, codigo_barras, cantidad_defecto } = req.body;
  if (!nombre || !nombre.trim() || !sku || !sku.trim()) {
    return res.status(400).json({ error: 'Nombre y SKU requeridos' });
  }

  // Check unique SKU
  const existente = db.prepare('SELECT id FROM articulos WHERE familia_id = ? AND sku = ?').get(familiaId, sku.trim());
  if (existente) {
    return res.status(400).json({ error: 'Ya existe un artículo con ese SKU' });
  }

  const id = crypto.randomUUID();
  db.prepare(`
    INSERT INTO articulos (id, familia_id, nombre, sku, codigo_barras, cantidad_defecto, creado_por)
    VALUES (?, ?, ?, ?, ?, ?, ?)
  `).run(id, familiaId, nombre.trim(), sku.trim(), codigo_barras || null, cantidad_defecto || 1.0, req.usuario.usuario_id);

  const articulo = db.prepare('SELECT * FROM articulos WHERE id = ?').get(id);
  res.status(201).json(articulo);
});

// GET /api/familias/:familiaId/articulos
router.get('/familias/:familiaId/articulos', authMiddleware, (req, res) => {
  const db = getDb();
  const familiaId = req.params.familiaId;

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(familiaId, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  let sql = 'SELECT * FROM articulos WHERE familia_id = ?';
  const params = [familiaId];

  // Filter by activo
  const activos = req.query.activos !== 'false';
  if (activos) {
    sql += ' AND activo = 1';
  }

  // Search
  if (req.query.q && req.query.q.trim()) {
    sql += ' AND nombre LIKE ?';
    params.push(`%${req.query.q.trim()}%`);
  }

  sql += ' ORDER BY nombre ASC';

  const articulos = db.prepare(sql).all(...params);
  res.json(articulos);
});

// GET /api/articulos/buscar/codigo
router.get('/articulos/buscar/codigo', authMiddleware, (req, res) => {
  const db = getDb();
  const codigo = req.query.codigo;
  if (!codigo) {
    return res.status(400).json({ error: 'Código de barras requerido' });
  }

  // Buscar en familias del usuario
  const articulo = db.prepare(`
    SELECT a.*, f.nombre as familia_nombre
    FROM articulos a
    JOIN familias f ON f.id = a.familia_id
    JOIN miembros_familia mf ON mf.familia_id = a.familia_id AND mf.usuario_id = ?
    WHERE a.codigo_barras = ? AND a.activo = 1
    LIMIT 1
  `).get(req.usuario.usuario_id, codigo);

  if (!articulo) {
    return res.status(404).json({ error: 'Artículo no encontrado' });
  }
  res.json(articulo);
});

// GET /api/articulos/mis-familias
router.get('/articulos/mis-familias', authMiddleware, (req, res) => {
  const db = getDb();
  const q = req.query.q ? req.query.q.trim() : '';
  const excluir = req.query.excluir_familia_id;

  let sql = `
    SELECT a.*, f.nombre as familia_nombre
    FROM articulos a
    JOIN familias f ON f.id = a.familia_id
    JOIN miembros_familia mf ON mf.familia_id = a.familia_id AND mf.usuario_id = ?
    WHERE a.activo = 1
  `;
  const params = [req.usuario.usuario_id];

  if (q) {
    sql += ' AND a.nombre LIKE ?';
    params.push(`%${q}%`);
  }
  if (excluir) {
    sql += ' AND a.familia_id != ?';
    params.push(excluir);
  }

  sql += ' ORDER BY a.nombre ASC LIMIT 50';
  res.json(db.prepare(sql).all(...params));
});

// GET /api/articulos/:id
router.get('/articulos/:id', authMiddleware, (req, res) => {
  const db = getDb();
  const articulo = db.prepare('SELECT * FROM articulos WHERE id = ?').get(req.params.id);
  if (!articulo) {
    return res.status(404).json({ error: 'Artículo no encontrado' });
  }
  // Check membership
  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(articulo.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No tienes acceso a este artículo' });
  }
  res.json(articulo);
});

// PUT /api/articulos/:id
router.put('/articulos/:id', authMiddleware, (req, res) => {
  const db = getDb();
  const articulo = db.prepare('SELECT * FROM articulos WHERE id = ?').get(req.params.id);
  if (!articulo) {
    return res.status(404).json({ error: 'Artículo no encontrado' });
  }

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(articulo.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No tienes acceso a este artículo' });
  }

  const { nombre, sku, codigo_barras, cantidad_defecto } = req.body;
  const updates = [];
  const params = [];

  if (nombre !== undefined) { updates.push('nombre = ?'); params.push(nombre.trim()); }
  if (sku !== undefined) { updates.push('sku = ?'); params.push(sku.trim()); }
  if (codigo_barras !== undefined) { updates.push('codigo_barras = ?'); params.push(codigo_barras || null); }
  if (cantidad_defecto !== undefined) { updates.push('cantidad_defecto = ?'); params.push(cantidad_defecto); }

  if (updates.length > 0) {
    updates.push("fecha_actualizacion = datetime('now')");
    params.push(req.params.id);
    db.prepare(`UPDATE articulos SET ${updates.join(', ')} WHERE id = ?`).run(...params);
  }

  res.json(db.prepare('SELECT * FROM articulos WHERE id = ?').get(req.params.id));
});

// DELETE /api/articulos/:id (soft delete)
router.delete('/articulos/:id', authMiddleware, (req, res) => {
  const db = getDb();
  const articulo = db.prepare('SELECT * FROM articulos WHERE id = ?').get(req.params.id);
  if (!articulo) {
    return res.status(404).json({ error: 'Artículo no encontrado' });
  }

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(articulo.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No tienes acceso a este artículo' });
  }

  db.prepare("UPDATE articulos SET activo = 0, fecha_actualizacion = datetime('now') WHERE id = ?").run(req.params.id);
  res.json({ mensaje: 'Artículo desactivado' });
});

// POST /api/familias/:familiaId/articulos/importar
router.post('/familias/:familiaId/articulos/importar', authMiddleware, (req, res) => {
  const db = getDb();
  const familiaDestId = req.params.familiaId;

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(familiaDestId, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const { articulo_origen_id, familia_origen_id } = req.body;
  if (!articulo_origen_id || !familia_origen_id) {
    return res.status(400).json({ error: 'articulo_origen_id y familia_origen_id requeridos' });
  }

  const origen = db.prepare('SELECT * FROM articulos WHERE id = ? AND familia_id = ?').get(articulo_origen_id, familia_origen_id);
  if (!origen) {
    return res.status(404).json({ error: 'Artículo de origen no encontrado' });
  }

  let sku = origen.sku;
  const existente = db.prepare('SELECT id FROM articulos WHERE familia_id = ? AND sku = ?').get(familiaDestId, sku);
  if (existente) {
    sku = sku + '-copia';
  }

  const id = crypto.randomUUID();
  db.prepare(`
    INSERT INTO articulos (id, familia_id, nombre, sku, codigo_barras, cantidad_defecto, creado_por)
    VALUES (?, ?, ?, ?, ?, ?, ?)
  `).run(id, familiaDestId, origen.nombre, sku, origen.codigo_barras, origen.cantidad_defecto, req.usuario.usuario_id);

  res.status(201).json(db.prepare('SELECT * FROM articulos WHERE id = ?').get(id));
});

// ────────────────────────────────────────────
// Áreas del Super
// ────────────────────────────────────────────

// GET /api/familias/:familiaId/areas-super
router.get('/familias/:familiaId/areas-super', authMiddleware, (req, res) => {
  const db = getDb();
  const rows = db.prepare('SELECT * FROM areas_super WHERE familia_id = ? ORDER BY orden ASC')
    .all(req.params.familiaId);
  res.json(rows);
});

// POST /api/familias/:familiaId/areas-super
router.post('/familias/:familiaId/areas-super', authMiddleware, (req, res) => {
  const db = getDb();
  const { nombre, orden } = req.body;
  if (!nombre || !nombre.trim()) {
    return res.status(400).json({ error: 'Nombre requerido' });
  }
  const id = crypto.randomUUID();
  db.prepare('INSERT INTO areas_super (id, familia_id, nombre, orden) VALUES (?, ?, ?, ?)')
    .run(id, req.params.familiaId, nombre.trim(), orden || 0);
  res.status(201).json(db.prepare('SELECT * FROM areas_super WHERE id = ?').get(id));
});

// DELETE /api/familias/:familiaId/areas-super/:id
router.delete('/familias/:familiaId/areas-super/:areaId', authMiddleware, (req, res) => {
  const db = getDb();
  db.prepare('DELETE FROM areas_super WHERE id = ? AND familia_id = ?').run(req.params.areaId, req.params.familiaId);
  res.json({ mensaje: 'Área eliminada' });
});

// ────────────────────────────────────────────
// Áreas de la Casa
// ────────────────────────────────────────────

// GET /api/familias/:familiaId/areas-casa
router.get('/familias/:familiaId/areas-casa', authMiddleware, (req, res) => {
  const db = getDb();
  const rows = db.prepare('SELECT * FROM areas_casa WHERE familia_id = ? ORDER BY orden ASC')
    .all(req.params.familiaId);
  res.json(rows);
});

// POST /api/familias/:familiaId/areas-casa
router.post('/familias/:familiaId/areas-casa', authMiddleware, (req, res) => {
  const db = getDb();
  const { nombre, orden } = req.body;
  if (!nombre || !nombre.trim()) {
    return res.status(400).json({ error: 'Nombre requerido' });
  }
  const id = crypto.randomUUID();
  db.prepare('INSERT INTO areas_casa (id, familia_id, nombre, orden) VALUES (?, ?, ?, ?)')
    .run(id, req.params.familiaId, nombre.trim(), orden || 0);
  res.status(201).json(db.prepare('SELECT * FROM areas_casa WHERE id = ?').get(id));
});

// DELETE /api/familias/:familiaId/areas-casa/:id
router.delete('/familias/:familiaId/areas-casa/:areaId', authMiddleware, (req, res) => {
  const db = getDb();
  db.prepare('DELETE FROM areas_casa WHERE id = ? AND familia_id = ?').run(req.params.areaId, req.params.familiaId);
  res.json({ mensaje: 'Área eliminada' });
});

module.exports = router;
