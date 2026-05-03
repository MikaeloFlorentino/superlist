const express = require('express');
const router = express.Router();
const crypto = require('crypto');
const { getDb } = require('../db');
const { authMiddleware } = require('../middleware/auth');

// ────────────────────────────────────────────
// Listas
// ────────────────────────────────────────────

// POST /api/familias/:familiaId/listas
router.post('/familias/:familiaId/listas', authMiddleware, (req, res) => {
  const db = getDb();
  const familiaId = req.params.familiaId;

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(familiaId, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const { nombre, supermercado } = req.body;
  if (!nombre || !nombre.trim()) {
    return res.status(400).json({ error: 'Nombre requerido' });
  }

  const id = crypto.randomUUID();
  db.prepare('INSERT INTO listas (id, familia_id, nombre, supermercado, creado_por) VALUES (?, ?, ?, ?, ?)')
    .run(id, familiaId, nombre.trim(), supermercado || null, req.usuario.usuario_id);

  res.status(201).json({
    id,
    familia_id: familiaId,
    nombre: nombre.trim(),
    supermercado: supermercado || null,
    estado: 'PENDIENTE',
    total_estimado: 0.00,
    items_count: 0,
    creado_por: req.usuario.usuario_id,
    fecha_creacion: db.prepare('SELECT fecha_creacion FROM listas WHERE id = ?').get(id).fecha_creacion
  });
});

// GET /api/familias/:familiaId/listas
router.get('/familias/:familiaId/listas', authMiddleware, (req, res) => {
  const db = getDb();
  const familiaId = req.params.familiaId;

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(familiaId, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  let sql = `
    SELECT l.*,
      (SELECT COUNT(*) FROM items_lista WHERE lista_id = l.id) as items_count,
      (SELECT COUNT(*) FROM items_lista WHERE lista_id = l.id AND estado = 'COMPRADO') as items_completados
    FROM listas l
    WHERE l.familia_id = ?
  `;
  const params = [familiaId];

  if (req.query.estado) {
    sql += ' AND l.estado = ?';
    params.push(req.query.estado);
  }

  sql += ' ORDER BY l.fecha_creacion DESC';

  res.json(db.prepare(sql).all(...params));
});

// GET /api/listas/:id
router.get('/listas/:id', authMiddleware, (req, res) => {
  const db = getDb();
  const lista = db.prepare(`
    SELECT l.*, u.nombre as creado_por_nombre
    FROM listas l
    JOIN usuarios u ON u.id = l.creado_por
    WHERE l.id = ?
  `).get(req.params.id);
  if (!lista) {
    return res.status(404).json({ error: 'Lista no encontrada' });
  }

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(lista.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const items = db.prepare(`
    SELECT it.*,
      a.nombre as articulo_nombre,
      sp.nombre as area_super_nombre,
      ca.nombre as area_casa_nombre,
      u.nombre as responsable_nombre
    FROM items_lista it
    LEFT JOIN articulos a ON a.id = it.articulo_id
    LEFT JOIN areas_super sp ON sp.id = it.area_super_id
    LEFT JOIN areas_casa ca ON ca.id = it.area_casa_id
    LEFT JOIN usuarios u ON u.id = it.responsable_id
    WHERE it.lista_id = ?
    ORDER BY it.orden ASC
  `).all(req.params.id);

  res.json({ ...lista, items });
});

// PATCH /api/listas/:id/estado
router.patch('/listas/:id/estado', authMiddleware, (req, res) => {
  const db = getDb();
  const { estado } = req.body;
  const estadosValidos = ['PENDIENTE', 'EN_CURSO', 'COMPLETADA', 'CANCELADA'];
  if (!estadosValidos.includes(estado)) {
    return res.status(400).json({ error: 'Estado inválido' });
  }

  const lista = db.prepare('SELECT * FROM listas WHERE id = ?').get(req.params.id);
  if (!lista) {
    return res.status(404).json({ error: 'Lista no encontrada' });
  }

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(lista.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  let extra = '';
  if (estado === 'COMPLETADA') {
    extra = ", completada_por = ?, fecha_completada = datetime('now')";
  }
  if (estado === 'PENDIENTE') {
    extra = ", completada_por = NULL, fecha_completada = NULL";
  }

  const params = [estado];
  if (estado === 'COMPLETADA') params.push(req.usuario.usuario_id);
  if (estado === 'PENDIENTE') params.push(null);
  params.push(req.params.id);
  db.prepare(`UPDATE listas SET estado = ?, fecha_actualizacion = datetime('now')${extra} WHERE id = ?`)
    .run(...params);

  res.json({ id: req.params.id, estado });
});

// ────────────────────────────────────────────
// Items de Lista
// ────────────────────────────────────────────

// POST /api/listas/:listaId/items
router.post('/listas/:listaId/items', authMiddleware, (req, res) => {
  const db = getDb();
  const lista = db.prepare('SELECT * FROM listas WHERE id = ?').get(req.params.listaId);
  if (!lista) {
    return res.status(404).json({ error: 'Lista no encontrada' });
  }
  if (['COMPLETADA', 'CANCELADA'].includes(lista.estado)) {
    return res.status(400).json({ error: 'No se pueden agregar items a una lista completada o cancelada' });
  }

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(lista.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const { articulo_id, nombre_manual, cantidad, area_super_id, area_casa_id, responsable_id, notas } = req.body;
  if (!articulo_id && !nombre_manual) {
    return res.status(400).json({ error: 'Debes proporcionar articulo_id o nombre_manual' });
  }

  // Get next order
  const maxOrden = db.prepare('SELECT MAX(orden) as max_ord FROM items_lista WHERE lista_id = ?').get(req.params.listaId);
  const orden = (maxOrden.max_ord || 0) + 1;

  const id = crypto.randomUUID();
  db.prepare(`
    INSERT INTO items_lista (id, lista_id, articulo_id, nombre_manual, cantidad, area_super_id, area_casa_id, responsable_id, notas, orden)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `).run(id, req.params.listaId, articulo_id || null, nombre_manual || null, cantidad || 1.0, area_super_id || null, area_casa_id || null, responsable_id || null, notas || null, orden);

  res.status(201).json(db.prepare('SELECT * FROM items_lista WHERE id = ?').get(id));
});

// PUT /api/items/:id
router.put('/items/:id', authMiddleware, (req, res) => {
  const db = getDb();
  const item = db.prepare('SELECT * FROM items_lista WHERE id = ?').get(req.params.id);
  if (!item) {
    return res.status(404).json({ error: 'Item no encontrado' });
  }

  const lista = db.prepare('SELECT * FROM listas WHERE id = ?').get(item.lista_id);
  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(lista.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const { cantidad, area_super_id, area_casa_id, responsable_id, notas } = req.body;
  const updates = [];
  const params = [];

  if (cantidad !== undefined) { updates.push('cantidad = ?'); params.push(cantidad); }
  if (area_super_id !== undefined) { updates.push('area_super_id = ?'); params.push(area_super_id || null); }
  if (area_casa_id !== undefined) { updates.push('area_casa_id = ?'); params.push(area_casa_id || null); }
  if (responsable_id !== undefined) { updates.push('responsable_id = ?'); params.push(responsable_id || null); }
  if (notas !== undefined) { updates.push('notas = ?'); params.push(notas || null); }

  if (updates.length > 0) {
    updates.push("fecha_actualizacion = datetime('now')");
    params.push(req.params.id);
    db.prepare(`UPDATE items_lista SET ${updates.join(', ')} WHERE id = ?`).run(...params);
  }

  res.json(db.prepare('SELECT * FROM items_lista WHERE id = ?').get(req.params.id));
});

// PATCH /api/items/:id/estado
router.patch('/items/:id/estado', authMiddleware, (req, res) => {
  const db = getDb();
  const { estado, cantidad_comprada } = req.body;
  const estadosValidos = ['PENDIENTE', 'COMPRADO', 'NO_HAY'];
  if (!estadosValidos.includes(estado)) {
    return res.status(400).json({ error: 'Estado inválido' });
  }

  const item = db.prepare('SELECT * FROM items_lista WHERE id = ?').get(req.params.id);
  if (!item) {
    return res.status(404).json({ error: 'Item no encontrado' });
  }

  const lista = db.prepare('SELECT * FROM listas WHERE id = ?').get(item.lista_id);
  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(lista.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  // Use transaction: update item + auto-add to pendientes if NO_HAY
  const transaction = db.transaction(() => {
    const updates = ["estado = ?", "fecha_actualizacion = datetime('now')"];
    const params = [estado];
    if (cantidad_comprada !== undefined) {
      updates.push('cantidad_comprada = ?');
      params.push(cantidad_comprada);
    }
    params.push(req.params.id);
    db.prepare(`UPDATE items_lista SET ${updates.join(', ')} WHERE id = ?`).run(...params);

    if (estado === 'NO_HAY') {
      // Import addItemToPendientes from shopping routes
      const { addItemToPendientes } = require('./shopping');
      addItemToPendientes(db, item, item.lista_id, lista.familia_id, req.usuario.usuario_id);
    }
  });
  transaction();

  res.json({ id: req.params.id, estado });
});

// DELETE /api/items/:id
router.delete('/items/:id', authMiddleware, (req, res) => {
  const db = getDb();
  const item = db.prepare('SELECT * FROM items_lista WHERE id = ?').get(req.params.id);
  if (!item) {
    return res.status(404).json({ error: 'Item no encontrado' });
  }

  const lista = db.prepare('SELECT * FROM listas WHERE id = ?').get(item.lista_id);
  if (['COMPLETADA', 'CANCELADA'].includes(lista.estado)) {
    return res.status(400).json({ error: 'No se pueden eliminar items de una lista completada o cancelada' });
  }

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(lista.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  db.prepare('DELETE FROM items_lista WHERE id = ?').run(req.params.id);
  res.json({ mensaje: 'Item eliminado' });
});

// PUT /api/listas/:id/items/reordenar
router.put('/listas/:id/items/reordenar', authMiddleware, (req, res) => {
  const db = getDb();
  const lista = db.prepare('SELECT * FROM listas WHERE id = ?').get(req.params.id);
  if (!lista) {
    return res.status(404).json({ error: 'Lista no encontrada' });
  }

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(lista.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const { orden_items } = req.body;
  if (!Array.isArray(orden_items)) {
    return res.status(400).json({ error: 'orden_items debe ser un array de IDs' });
  }

  const update = db.prepare('UPDATE items_lista SET orden = ? WHERE id = ? AND lista_id = ?');
  const transaction = db.transaction(() => {
    orden_items.forEach((itemId, idx) => {
      update.run(idx + 1, itemId, req.params.id);
    });
  });
  transaction();

  res.json({ mensaje: 'Items reordenados' });
});

// GET /api/listas/:id/total
router.get('/listas/:id/total', authMiddleware, (req, res) => {
  const db = getDb();
  const lista = db.prepare('SELECT * FROM listas WHERE id = ?').get(req.params.id);
  if (!lista) {
    return res.status(404).json({ error: 'Lista no encontrada' });
  }

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(lista.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const total = db.prepare('SELECT COUNT(*) as c FROM items_lista WHERE lista_id = ?').get(req.params.id).c;
  const pendientes = db.prepare("SELECT COUNT(*) as c FROM items_lista WHERE lista_id = ? AND estado = 'PENDIENTE'").get(req.params.id).c;
  const comprados = db.prepare("SELECT COUNT(*) as c FROM items_lista WHERE lista_id = ? AND estado = 'COMPRADO'").get(req.params.id).c;
  const noHay = db.prepare("SELECT COUNT(*) as c FROM items_lista WHERE lista_id = ? AND estado = 'NO_HAY'").get(req.params.id).c;
  const progreso = total > 0 ? Math.round((comprados / total) * 10000) / 100 : 0;

  res.json({ total_items: total, pendientes, comprados, no_hay: noHay, progreso });
});

module.exports = router;
