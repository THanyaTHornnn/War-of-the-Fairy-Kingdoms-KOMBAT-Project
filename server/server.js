/**
 * ============================================================
 *  KOMBAT Game - server.js
 *  Node.js Backend Server (Express + Socket.IO)
 * ============================================================
 *  จัดการการเชื่อมต่อผู้เล่นและส่งต่อ event ไปยัง GameController
 *  - รองรับ 2 ผู้เล่น + ผู้ชม (spectators)
 *  - Real-time communication ผ่าน Socket.IO
 *  - Serve React frontend จาก client/build
 * ============================================================
 */

const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const path = require('path');
const { GameController } = require('./controller');

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: { origin: '*', methods: ['GET', 'POST'] }
});

// ---- Serve React Build ----
app.use(express.static(path.join(__dirname, '..', 'client', 'build')));
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, '..', 'client', 'build', 'index.html'));
});

// ---- Game State ----
const game = new GameController();
const players = [null, null]; // socket IDs ของผู้เล่น 1 และ 2
const spectators = [];        // socket IDs ของผู้ชม

/**
 * Broadcast state ให้ทุกคน
 */
function broadcastState() {
  const state = game.getState();
  io.emit('gameState', state);
}

/**
 * ส่ง error กลับให้ผู้เล่นที่ร้องขอ
 */
function sendError(socket, msg) {
  socket.emit('error', { message: msg });
}

/**
 * หา player index จาก socket id
 * คืน 0 หรือ 1, หรือ -1 ถ้าเป็น spectator
 */
function getPlayerIdx(socketId) {
  if (players[0] === socketId) return 0;
  if (players[1] === socketId) return 1;
  return -1;
}

// ============================================================
//  SOCKET.IO Events
// ============================================================

io.on('connection', (socket) => {
  console.log(`[CONNECT] ${socket.id}`);

  // ---- ลงทะเบียนผู้เล่น ----
  if (players[0] === null) {
    players[0] = socket.id;
    socket.emit('assigned', { player: 1, role: 'player' });
    console.log(`[PLAYER 1] ${socket.id}`);
  } else if (players[1] === null) {
    players[1] = socket.id;
    socket.emit('assigned', { player: 2, role: 'player' });
    console.log(`[PLAYER 2] ${socket.id}`);

    // ผู้เล่นครบ 2 คน → แจ้งทุกคน
    io.emit('playersReady', { message: 'Both players connected!' });
  } else {
    // คนที่ 3+ เป็นผู้ชม
    spectators.push(socket.id);
    socket.emit('assigned', { player: -1, role: 'spectator' });
    console.log(`[SPECTATOR] ${socket.id}`);
  }

  // ส่ง state ปัจจุบัน
  socket.emit('gameState', game.getState());

  // ---- Event Handlers ----

  /**
   * เริ่มเกมด้วย config
   * data: { config: string, mode: 'duel'|'solitaire'|'auto' }
   */
  socket.on('initGame', (data) => {
    try {
      const pi = getPlayerIdx(socket.id);
      if (pi < 0) return sendError(socket, 'Spectators cannot start game');
      if (game.phase !== 'config') return sendError(socket, 'Game already initialized');

      const result = game.initGame(data.config || '', data.mode || 'duel');
      broadcastState();
    } catch (e) {
      sendError(socket, e.message);
    }
  });

  /**
   * ตั้งค่า minion kinds
   * data: { kinds: [{ name, defense, script }] }
   */
  socket.on('setKinds', (data) => {
    try {
      const pi = getPlayerIdx(socket.id);
      if (pi < 0) return sendError(socket, 'Spectators cannot set kinds');
      if (game.phase !== 'setupKinds') return sendError(socket, 'Not in setup phase');

      const result = game.setKinds(pi, data.kinds);
      broadcastState();
    } catch (e) {
      sendError(socket, e.message);
    }
  });

  /**
   * Initial spawn
   * data: { r, c, kindIdx }
   */
  socket.on('initialSpawn', (data) => {
    try {
      const pi = getPlayerIdx(socket.id);
      if (pi < 0) return sendError(socket, 'Spectators cannot spawn');

      const result = game.initialSpawn(pi, data.r, data.c, data.kindIdx);
      broadcastState();
    } catch (e) {
      sendError(socket, e.message);
    }
  });

  /**
   * เริ่มเทิร์น
   */
  socket.on('startTurn', () => {
    try {
      const pi = getPlayerIdx(socket.id);
      if (pi < 0) return sendError(socket, 'Spectators cannot play');
      if (pi !== game.gs?.cp) return sendError(socket, 'Not your turn');

      game.startTurn();
      broadcastState();
    } catch (e) {
      sendError(socket, e.message);
    }
  });

  /**
   * ซื้อ hex
   * data: { r, c }
   */
  socket.on('buyHex', (data) => {
    try {
      const pi = getPlayerIdx(socket.id);
      if (pi < 0) return sendError(socket, 'Spectators cannot play');
      if (pi !== game.gs?.cp) return sendError(socket, 'Not your turn');

      game.buyHex(data.r, data.c);
      broadcastState();
    } catch (e) {
      sendError(socket, e.message);
    }
  });

  /**
   * ข้ามการซื้อ hex
   */
  socket.on('skipBuy', () => {
    try {
      const pi = getPlayerIdx(socket.id);
      if (pi < 0) return sendError(socket, 'Spectators cannot play');
      if (pi !== game.gs?.cp) return sendError(socket, 'Not your turn');

      game.skipBuy();
      broadcastState();
    } catch (e) {
      sendError(socket, e.message);
    }
  });

  /**
   * Spawn minion
   * data: { r, c, kindIdx }
   */
  socket.on('spawnMinion', (data) => {
    try {
      const pi = getPlayerIdx(socket.id);
      if (pi < 0) return sendError(socket, 'Spectators cannot play');
      if (pi !== game.gs?.cp) return sendError(socket, 'Not your turn');

      game.spawnMinion(data.r, data.c, data.kindIdx);
      broadcastState();
    } catch (e) {
      sendError(socket, e.message);
    }
  });

  /**
   * ข้ามการ spawn
   */
  socket.on('skipSpawn', () => {
    try {
      const pi = getPlayerIdx(socket.id);
      if (pi < 0) return sendError(socket, 'Spectators cannot play');
      if (pi !== game.gs?.cp) return sendError(socket, 'Not your turn');

      game.skipSpawn();
      broadcastState();
    } catch (e) {
      sendError(socket, e.message);
    }
  });

  /**
   * Execute strategies
   */
  socket.on('executeStrategies', () => {
    try {
      const pi = getPlayerIdx(socket.id);
      if (pi < 0) return sendError(socket, 'Spectators cannot play');
      if (pi !== game.gs?.cp) return sendError(socket, 'Not your turn');

      game.executeStrategies();
      broadcastState();
    } catch (e) {
      sendError(socket, e.message);
    }
  });

  /**
   * รีเซ็ตเกม
   */
  socket.on('resetGame', () => {
    const pi = getPlayerIdx(socket.id);
    if (pi < 0) return;

    // สร้าง controller ใหม่
    Object.assign(game, new GameController());
    broadcastState();
    console.log('[RESET] Game reset by P' + (pi + 1));
  });

  // ---- Disconnect ----
  socket.on('disconnect', () => {
    console.log(`[DISCONNECT] ${socket.id}`);

    if (players[0] === socket.id) {
      players[0] = null;
      io.emit('playerDisconnected', { player: 1 });
    } else if (players[1] === socket.id) {
      players[1] = null;
      io.emit('playerDisconnected', { player: 2 });
    } else {
      const idx = spectators.indexOf(socket.id);
      if (idx >= 0) spectators.splice(idx, 1);
    }
  });
});

// ---- Start Server ----
const PORT = process.env.PORT || 3001;
server.listen(PORT, () => {
  console.log(`
╔════════════════════════════════════════════╗
║         KOMBAT Game Server v1.0            ║
║────────────────────────────────────────────║
║  Running on: http://localhost:${PORT}         ║
║  Mode: Waiting for players...              ║
║  Player 1: Connect first                   ║
║  Player 2: Connect second                  ║
║  Others:   Become spectators               ║
╚════════════════════════════════════════════╝
  `);
});
