const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'superlist-secret-key-mvp-2025';
const JWT_EXPIRES = '7d';

function generateToken(payload) {
  return jwt.sign(payload, JWT_SECRET, { expiresIn: JWT_EXPIRES });
}

function authMiddleware(req, res, next) {
  const header = req.headers.authorization;
  if (!header || !header.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'No autorizado' });
  }
  const token = header.slice(7);
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.usuario = decoded;
    next();
  } catch (err) {
    return res.status(401).json({ error: 'No autorizado' });
  }
}

module.exports = { generateToken, authMiddleware, JWT_SECRET };
