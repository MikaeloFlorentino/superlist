const express = require('express');
const router = express.Router();
const crypto = require('crypto');
const { getDb } = require('../db');
const { authMiddleware } = require('../middleware/auth');

// ────────────────────────────────────────────
// Modo Compra
// ────────────────────────────────────────────

// GET /api/listas/:id/modo-compra
router.get('/listas/:id/modo-compra', authMiddleware, (req, res) => {
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
      COALESCE(a.nombre, it.nombre_manual) as nombre,
      sp.id as area_id,
      COALESCE(sp.nombre, 'Sin área') as area_nombre,
      sp.orden as area_orden
    FROM items_lista it
    LEFT JOIN articulos a ON a.id = it.articulo_id
    LEFT JOIN areas_super sp ON sp.id = it.area_super_id
    WHERE it.lista_id = ?
    ORDER BY sp.orden ASC, it.orden ASC
  `).all(req.params.id);

  // Group by area
  const areaMap = {};
  for (const item of items) {
    const key = item.area_id || '__none__';
    if (!areaMap[key]) {
      areaMap[key] = {
        area_id: item.area_id,
        area_nombre: item.area_nombre,
        orden: item.area_orden !== null ? item.area_orden : 999,
        items: []
      };
    }
    areaMap[key].items.push({
      id: item.id,
      nombre: item.nombre,
      cantidad: item.cantidad,
      estado: item.estado,
      notas: item.notas
    });
  }

  const areas = Object.values(areaMap).sort((a, b) => a.orden - b.orden);

  const total = items.length;
  const comprados = items.filter(i => i.estado === 'COMPRADO').length;
  const pendientes = items.filter(i => i.estado === 'PENDIENTE').length;
  const noHay = items.filter(i => i.estado === 'NO_HAY').length;
  const progreso = total > 0 ? Math.round((comprados / total) * 10000) / 100 : 0;

  res.json({
    lista_id: lista.id,
    lista_nombre: lista.nombre,
    supermercado: lista.supermercado,
    estado: lista.estado,
    areas,
    resumen: { total, comprados, pendientes, no_hay: noHay, progreso }
  });
});

// ────────────────────────────────────────────
// Pendientes (items NO_HAY persistidos)
// ────────────────────────────────────────────

// Helper: ensure lista_pendientes exists for a family
function ensureListaPendientes(db, familiaId, usuarioId) {
  let lp = db.prepare('SELECT * FROM lista_pendientes WHERE familia_id = ?').get(familiaId);
  if (!lp) {
    const id = crypto.randomUUID();
    db.prepare('INSERT INTO lista_pendientes (id, familia_id, creado_por) VALUES (?, ?, ?)')
      .run(id, familiaId, usuarioId);
    lp = db.prepare('SELECT * FROM lista_pendientes WHERE familia_id = ?').get(familiaId);
  }
  return lp;
}

// POST /api/listas/:id/marcar-no-hay (utility: mark item as NO_HAY AND add to pendientes)
router.post('/listas/:id/marcar-no-hay', authMiddleware, (req, res) => {
  const db = getDb();
  const { item_id } = req.body;
  if (!item_id) {
    return res.status(400).json({ error: 'item_id requerido' });
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

  const item = db.prepare('SELECT * FROM items_lista WHERE id = ? AND lista_id = ?').get(item_id, req.params.id);
  if (!item) {
    return res.status(404).json({ error: 'Item no encontrado en esta lista' });
  }

  // Mark item as NO_HAY
  db.prepare("UPDATE items_lista SET estado = 'NO_HAY', fecha_actualizacion = datetime('now') WHERE id = ?").run(item_id);

  // Add to pendientes
  const lp = ensureListaPendientes(db, lista.familia_id, req.usuario.usuario_id);
  const pendId = crypto.randomUUID();
  db.prepare(`
    INSERT INTO items_pendientes (id, lista_pendiente_id, articulo_id, nombre_manual, cantidad, area_super_id, area_casa_id, lista_origen_id, agregado_por)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
  `).run(pendId, lp.id, item.articulo_id, item.nombre_manual, item.cantidad, item.area_super_id, item.area_casa_id, req.params.id, req.usuario.usuario_id);

  res.json({ mensaje: 'Item marcado como no hay y agregado a pendientes' });
});

// GET /api/familias/:familiaId/pendientes
router.get('/familias/:familiaId/pendientes', authMiddleware, (req, res) => {
  const db = getDb();

  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(req.params.familiaId, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const lp = db.prepare('SELECT * FROM lista_pendientes WHERE familia_id = ?').get(req.params.familiaId);
  if (!lp) {
    return res.json({ id: null, nombre: 'Pendientes', items: [], total_pendientes: 0, total_resueltos: 0 });
  }

  const estado = req.query.estado || 'PENDIENTE';
  const items = db.prepare(`
    SELECT ip.*,
      a.nombre as articulo_nombre,
      sp.nombre as area_super_nombre,
      ca.nombre as area_casa_nombre,
      l.nombre as lista_origen_nombre,
      u.nombre as agregado_por_nombre,
      u2.nombre as resuelto_por_nombre
    FROM items_pendientes ip
    LEFT JOIN articulos a ON a.id = ip.articulo_id
    LEFT JOIN areas_super sp ON sp.id = ip.area_super_id
    LEFT JOIN areas_casa ca ON ca.id = ip.area_casa_id
    LEFT JOIN listas l ON l.id = ip.lista_origen_id
    LEFT JOIN usuarios u ON u.id = ip.agregado_por
    LEFT JOIN usuarios u2 ON u2.id = ip.resuelto_por
    WHERE ip.lista_pendiente_id = ? AND ip.estado = ?
    ORDER BY ip.fecha_creacion DESC
  `).all(lp.id, estado);

  const totalPendientes = db.prepare("SELECT COUNT(*) as c FROM items_pendientes WHERE lista_pendiente_id = ? AND estado = 'PENDIENTE'").get(lp.id).c;
  const totalResueltos = db.prepare("SELECT COUNT(*) as c FROM items_pendientes WHERE lista_pendiente_id = ? AND estado = 'RESUELTO'").get(lp.id).c;

  res.json({
    id: lp.id,
    nombre: lp.nombre,
    items,
    total_pendientes: totalPendientes,
    total_resueltos: totalResueltos
  });
});

// GET /api/familias/:familiaId/pendientes/total
router.get('/familias/:familiaId/pendientes/total', authMiddleware, (req, res) => {
  const db = getDb();
  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(req.params.familiaId, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const lp = db.prepare('SELECT * FROM lista_pendientes WHERE familia_id = ?').get(req.params.familiaId);
  if (!lp) {
    return res.json({ total_pendientes: 0, articulos_pendientes: [] });
  }

  const pendientes = db.prepare(`
    SELECT ip.*, COALESCE(a.nombre, ip.nombre_manual) as nombre
    FROM items_pendientes ip
    LEFT JOIN articulos a ON a.id = ip.articulo_id
    WHERE ip.lista_pendiente_id = ? AND ip.estado = 'PENDIENTE'
  `).all(lp.id);

  res.json({
    total_pendientes: pendientes.length,
    articulos_pendientes: pendientes.map(p => p.nombre)
  });
});

// PATCH /api/pendientes/:id/resolver
router.patch('/pendientes/:id/resolver', authMiddleware, (req, res) => {
  const db = getDb();
  const pendiente = db.prepare('SELECT * FROM items_pendientes WHERE id = ?').get(req.params.id);
  if (!pendiente) {
    return res.status(404).json({ error: 'Pendiente no encontrado' });
  }

  const lp = db.prepare('SELECT * FROM lista_pendientes WHERE id = ?').get(pendiente.lista_pendiente_id);
  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(lp.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const { agregar_a_lista_id } = req.body;
  let agregadoALista = false;

  const transaction = db.transaction(() => {
    db.prepare("UPDATE items_pendientes SET estado = 'RESUELTO', resuelto_por = ?, fecha_resolucion = datetime('now') WHERE id = ?")
      .run(req.usuario.usuario_id, req.params.id);

    if (agregar_a_lista_id) {
      // Add item to the specified list
      const maxOrden = db.prepare('SELECT MAX(orden) as max_ord FROM items_lista WHERE lista_id = ?').get(agregar_a_lista_id);
      const orden = (maxOrden.max_ord || 0) + 1;
      const itemId = crypto.randomUUID();
      db.prepare(`
        INSERT INTO items_lista (id, lista_id, articulo_id, nombre_manual, cantidad, area_super_id, area_casa_id, notas, orden)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      `).run(itemId, agregar_a_lista_id, pendiente.articulo_id, pendiente.nombre_manual, pendiente.cantidad, pendiente.area_super_id, pendiente.area_casa_id, 'Agregado desde pendientes', orden);
      agregadoALista = true;
    }
  });
  transaction();

  res.json({ id: req.params.id, estado: 'RESUELTO', agregado_a_lista: agregadoALista });
});

// POST /api/pendientes/:id/mover-a-lista
router.post('/pendientes/:id/mover-a-lista', authMiddleware, (req, res) => {
  const db = getDb();
  const pendiente = db.prepare('SELECT * FROM items_pendientes WHERE id = ?').get(req.params.id);
  if (!pendiente) {
    return res.status(404).json({ error: 'Pendiente no encontrado' });
  }

  const lp = db.prepare('SELECT * FROM lista_pendientes WHERE id = ?').get(pendiente.lista_pendiente_id);
  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(lp.familia_id, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const { lista_id } = req.body;
  if (!lista_id) {
    return res.status(400).json({ error: 'lista_id requerido' });
  }

  const lista = db.prepare('SELECT * FROM listas WHERE id = ? AND familia_id = ?').get(lista_id, lp.familia_id);
  if (!lista) {
    return res.status(404).json({ error: 'Lista no encontrada' });
  }

  const maxOrden = db.prepare('SELECT MAX(orden) as max_ord FROM items_lista WHERE lista_id = ?').get(lista_id);
  const orden = (maxOrden.max_ord || 0) + 1;
  const itemId = crypto.randomUUID();
  db.prepare(`
    INSERT INTO items_lista (id, lista_id, articulo_id, nombre_manual, cantidad, area_super_id, area_casa_id, notas, orden)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
  `).run(itemId, lista_id, pendiente.articulo_id, pendiente.nombre_manual, pendiente.cantidad, pendiente.area_super_id, pendiente.area_casa_id, 'Desde pendientes', orden);

  res.status(201).json({ item_lista_id: itemId, mensaje: 'Item agregado a la lista' });
});

// ────────────────────────────────────────────
// Historial
// ────────────────────────────────────────────

// GET /api/familias/:familiaId/listas/historial
router.get('/familias/:familiaId/listas/historial', authMiddleware, (req, res) => {
  const db = getDb();
  const miembro = db.prepare('SELECT * FROM miembros_familia WHERE familia_id = ? AND usuario_id = ?')
    .get(req.params.familiaId, req.usuario.usuario_id);
  if (!miembro) {
    return res.status(403).json({ error: 'No eres miembro de esta familia' });
  }

  const historial = db.prepare(`
    SELECT l.*,
      (SELECT COUNT(*) FROM items_lista WHERE lista_id = l.id) as total_items,
      (SELECT COUNT(*) FROM items_lista WHERE lista_id = l.id AND estado = 'COMPRADO') as comprados,
      (SELECT COUNT(*) FROM items_lista WHERE lista_id = l.id AND estado = 'NO_HAY') as no_hay,
      u.nombre as completada_por_nombre
    FROM listas l
    LEFT JOIN usuarios u ON u.id = l.completada_por
    WHERE l.familia_id = ? AND l.estado = 'COMPLETADA'
    ORDER BY l.fecha_completada DESC
  `).all(req.params.familiaId);

  // Calculate minutos_transcurridos for each
  for (const h of historial) {
    if (h.fecha_creacion && h.fecha_completada) {
      const creado = new Date(h.fecha_creacion + 'Z');
      const completado = new Date(h.fecha_completada + 'Z');
      h.minutos_transcurridos = Math.round((completado - creado) / 60000);
    } else {
      h.minutos_transcurridos = 0;
    }
  }

  res.json(historial);
});

// GET /api/listas/:id/historial
router.get('/listas/:id/historial', authMiddleware, (req, res) => {
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

  const items = db.prepare(`
    SELECT it.*,
      COALESCE(a.nombre, it.nombre_manual) as nombre,
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

  const comprados = items.filter(i => i.estado === 'COMPRADO').length;
  const noHay = items.filter(i => i.estado === 'NO_HAY').length;

  let minutos = 0;
  if (lista.fecha_creacion && lista.fecha_completada) {
    minutos = Math.round((new Date(lista.fecha_completada + 'Z') - new Date(lista.fecha_creacion + 'Z')) / 60000);
  }

  res.json({
    id: lista.id,
    nombre: lista.nombre,
    estado: lista.estado,
    items,
    resumen: {
      comprados,
      no_hay: noHay,
      total_items: items.length,
      minutos_transcurridos: minutos,
      completada_por: db.prepare('SELECT nombre FROM usuarios WHERE id = ?').get(lista.completada_por)?.nombre || null,
      fecha_completada: lista.fecha_completada
    }
  });
});

module.exports = router;
