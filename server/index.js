const express = require('express');
const cors = require('cors');
const path = require('path');
const { getDb } = require('./db');

const authRoutes = require('./routes/auth');
const familiasRoutes = require('./routes/familias');
const catalogRoutes = require('./routes/catalog');
const listsRoutes = require('./routes/lists');
const shoppingRoutes = require('./routes/shopping');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Static files (SPA)
app.use(express.static(path.join(__dirname, '..', 'public')));

// API routes
app.use('/api/auth', authRoutes);
app.use('/api', familiasRoutes);
app.use('/api', catalogRoutes);
app.use('/api', listsRoutes);
app.use('/api', shoppingRoutes);

// Fallback: serve SPA for non-API routes
app.get('*', (req, res) => {
  if (!req.path.startsWith('/api')) {
    res.sendFile(path.join(__dirname, '..', 'public', 'index.html'));
  }
});

// Initialize DB on startup
getDb();
console.log('✅ Base de datos inicializada');

app.listen(PORT, () => {
  console.log(`🚀 Superlist API corriendo en http://localhost:${PORT}`);
});
