/**
 * ============================================================
 *  KOMBAT Game - App.jsx
 *  Main React Application Component
 * ============================================================
 *  จัดการ UI ทั้งหมด:
 *  - หน้า Config (ตั้งค่าเกม)
 *  - หน้า Kind Setup (กำหนด minion types + strategy scripts)
 *  - หน้า Initial Spawn (วาง minion เริ่มต้น)
 *  - หน้า Play (เล่นเกมจริง)
 *  - หน้า Game Over (แสดงผลลัพธ์)
 * ============================================================
 */
import React, { useState, useEffect, useCallback, useRef } from 'react';
import socket from './socket';
import HexBoard from './components/HexBoard';

// ---- Default Config Text ----
const DEFAULT_CONFIG = `spawn_cost=20
hex_purchase_cost=30
init_budget=25
init_hp=100
turn_budget=15
max_budget=200
interest_pct=5
max_turns=100
max_spawns=10`;

// ---- Default Strategy ----
const DEFAULT_SCRIPT = `# Simple Strategy
t = 0
while (t - t) move up
t = t + 1
if (opponent) then {
  x = opponent
  d = x % 10
  if (d - 1) then {
    if (d - 2) then {
      if (d - 3) then {
        if (d - 4) then {
          if (d - 5) then shoot upleft Budget % 8 + 2
          else shoot downleft Budget % 8 + 2
        } else shoot down Budget % 8 + 2
      } else shoot downright Budget % 8 + 2
    } else shoot upright Budget % 8 + 2
  } else shoot up Budget % 8 + 2
} else {
  r = random % 6
  if (r % 6) then {
    if (r - 1) then {
      if (r - 2) then {
        if (r - 3) then {
          if (r - 4) then move upleft
          else move downleft
        } else move down
      } else move downright
    } else move upright
  } else move up
}
done`;

export default function App() {
  // ---- Connection State ----
  const [connected, setConnected] = useState(false);
  const [myRole, setMyRole] = useState(null); // { player: 1|2|-1, role: 'player'|'spectator' }
  const [gameState, setGameState] = useState(null);
  const [errorMsg, setErrorMsg] = useState(null);
  const errorTimer = useRef(null);

  // ---- Setup State ----
  const [configText, setConfigText] = useState(DEFAULT_CONFIG);
  const [gameMode, setGameMode] = useState('duel');
  const [kinds, setKinds] = useState([
    { name: 'ATK', defense: 1, script: DEFAULT_SCRIPT },
  ]);
  const [selectedKindIdx, setSelectedKindIdx] = useState(0);
  const [kindsSubmitted, setKindsSubmitted] = useState(false);

  // ---- Play State ----
  const [logOpen, setLogOpen] = useState(true);

  // ---- Socket.IO Event Handlers ----
  useEffect(() => {
    socket.on('connect', () => setConnected(true));
    socket.on('disconnect', () => setConnected(false));

    socket.on('assigned', (data) => {
      setMyRole(data);
    });

    socket.on('gameState', (state) => {
      setGameState(state);
    });

    socket.on('playersReady', () => {
      // ผู้เล่นครบแล้ว
    });

    socket.on('playerDisconnected', (data) => {
      showError(`Player ${data.player} disconnected!`);
    });

    socket.on('error', (data) => {
      showError(data.message);
    });

    return () => {
      socket.off('connect');
      socket.off('disconnect');
      socket.off('assigned');
      socket.off('gameState');
      socket.off('playersReady');
      socket.off('playerDisconnected');
      socket.off('error');
    };
  }, []);

  // ---- Error Display ----
  const showError = useCallback((msg) => {
    setErrorMsg(msg);
    if (errorTimer.current) clearTimeout(errorTimer.current);
    errorTimer.current = setTimeout(() => setErrorMsg(null), 4000);
  }, []);

  // ---- Helpers ----
  const myPlayerIdx = myRole ? myRole.player - 1 : -1; // 0 or 1
  const isMyTurn = gameState && myPlayerIdx === gameState.currentPlayer;
  const isSpectator = myRole?.role === 'spectator';
  const phase = gameState?.phase || 'config';
  const turnPhase = gameState?.turnPhase;

  // ---- Event Emitters ----
  const initGame = () => {
    socket.emit('initGame', { config: configText, mode: gameMode });
  };

  const submitKinds = () => {
    if (kinds.length === 0) return showError('Add at least 1 minion kind');
    for (const k of kinds) {
      if (!k.name.trim()) return showError('Kind name required');
      if (!k.script.trim()) return showError('Strategy script required');
    }
    socket.emit('setKinds', { kinds });
    setKindsSubmitted(true);
  };

  const handleHexClick = (r, c) => {
    if (isSpectator) return;

    // Initial Spawn
    if (phase === 'spawn') {
      socket.emit('initialSpawn', { r, c, kindIdx: selectedKindIdx });
      return;
    }

    // Play phase
    if (phase === 'play' && isMyTurn) {
      if (turnPhase === 'buy') {
        socket.emit('buyHex', { r, c });
      } else if (turnPhase === 'spawn') {
        socket.emit('spawnMinion', { r, c, kindIdx: selectedKindIdx });
      }
    }
  };

  const startTurn = () => socket.emit('startTurn');
  const skipBuy = () => socket.emit('skipBuy');
  const skipSpawn = () => socket.emit('skipSpawn');
  const executeStrats = () => socket.emit('executeStrategies');
  const resetGame = () => {
    socket.emit('resetGame');
    setKindsSubmitted(false);
  };

  // ---- Kind Editor Helpers ----
  const addKind = () => {
    if (kinds.length >= 5) return showError('Max 5 kinds');
    setKinds([...kinds, { name: 'NEW', defense: 0, script: DEFAULT_SCRIPT }]);
  };

  const removeKind = (idx) => {
    if (kinds.length <= 1) return;
    const next = kinds.filter((_, i) => i !== idx);
    setKinds(next);
    if (selectedKindIdx >= next.length) setSelectedKindIdx(next.length - 1);
  };

  const updateKind = (idx, field, value) => {
    const next = [...kinds];
    next[idx] = { ...next[idx], [field]: value };
    setKinds(next);
  };

  // ---- Highlight Hexes for current action ----
  const getHighlightHexes = () => {
    if (!gameState || !gameState.players) return [];
    if (phase === 'spawn' || (phase === 'play' && turnPhase === 'spawn')) {
      const pi = gameState.currentPlayer;
      if (pi === myPlayerIdx || isSpectator) {
        return gameState.players[pi]?.spawnZone || [];
      }
    }
    if (phase === 'play' && turnPhase === 'buy' && isMyTurn) {
      // highlight hexes ที่สามารถซื้อได้ (adjacent to spawn zone)
      // ส่งทั้ง spawn zone เพื่อ reference
      return gameState.players[myPlayerIdx]?.spawnZone || [];
    }
    return [];
  };

  // ============================================================
  //  RENDER
  // ============================================================

  // ---- Not Connected ----
  if (!connected) {
    return (
      <div className="app-container">
        <div className="header">
          <h1>KOMBAT</h1>
        </div>
        <div className="waiting-screen">
          <div className="pulse-ring" />
          <p style={{ color: 'var(--text-secondary)', fontFamily: 'var(--font-display)', letterSpacing: 2 }}>
            CONNECTING TO SERVER...
          </p>
        </div>
      </div>
    );
  }

  // ---- Config Phase ----
  if (phase === 'config') {
    return (
      <div className="app-container">
        <Header myRole={myRole} connected={connected} />
        {isSpectator ? (
          <div className="waiting-screen">
            <p style={{ color: 'var(--text-secondary)' }}>Waiting for players to configure the game...</p>
          </div>
        ) : (
          <div className="setup-screen">
            <div className="setup-card">
              <h2>⚙ Game Configuration</h2>
              <div className="form-group">
                <label>Game Mode</label>
                <select value={gameMode} onChange={(e) => setGameMode(e.target.value)}>
                  <option value="duel">Duel (2 Players)</option>
                  <option value="solitaire">Solitaire (vs Bot)</option>
                  <option value="auto">Auto (Bot vs Bot)</option>
                </select>
              </div>
              <div className="form-group">
                <label>Configuration (key=value)</label>
                <textarea
                  rows={10}
                  value={configText}
                  onChange={(e) => setConfigText(e.target.value)}
                />
              </div>
              <button className="btn btn-primary" onClick={initGame}>
                Initialize Game
              </button>
            </div>
          </div>
        )}
        {errorMsg && <div className="error-toast">{errorMsg}</div>}
      </div>
    );
  }

  // ---- Setup Kinds Phase ----
  if (phase === 'setupKinds') {
    return (
      <div className="app-container">
        <Header myRole={myRole} connected={connected} />
        {isSpectator ? (
          <div className="waiting-screen">
            <p style={{ color: 'var(--text-secondary)' }}>Players are setting up their minion strategies...</p>
          </div>
        ) : kindsSubmitted ? (
          <div className="waiting-screen">
            <div className="pulse-ring" />
            <p style={{ color: 'var(--text-secondary)', fontFamily: 'var(--font-display)', letterSpacing: 2 }}>
              WAITING FOR OTHER PLAYER...
            </p>
          </div>
        ) : (
          <div className="setup-screen">
            <div className="setup-card" style={{ maxWidth: 800 }}>
              <h2>🗡 Define Minion Kinds (P{myPlayerIdx + 1})</h2>
              <p style={{ color: 'var(--text-secondary)', marginBottom: 16, fontSize: '0.85rem' }}>
                กำหนด Minion Type 1-5 ชนิด พร้อม Strategy Script
              </p>

              {kinds.map((k, i) => (
                <div className="kind-editor" key={i}>
                  <div className="kind-header">
                    <div className="form-group">
                      <label>Name (3 chars)</label>
                      <input
                        type="text"
                        maxLength={3}
                        value={k.name}
                        onChange={(e) => updateKind(i, 'name', e.target.value.toUpperCase())}
                        style={{ textTransform: 'uppercase', fontFamily: 'var(--font-display)' }}
                      />
                    </div>
                    <div className="form-group">
                      <label>Defense</label>
                      <input
                        type="number"
                        min="0"
                        max="9"
                        value={k.defense}
                        onChange={(e) => updateKind(i, 'defense', e.target.value)}
                      />
                    </div>
                    {kinds.length > 1 && (
                      <button
                        className="btn btn-danger"
                        onClick={() => removeKind(i)}
                        style={{ marginBottom: 0, padding: '8px 12px' }}
                      >
                        ✕
                      </button>
                    )}
                  </div>
                  <div className="form-group">
                    <label>Strategy Script</label>
                    <textarea
                      rows={8}
                      value={k.script}
                      onChange={(e) => updateKind(i, 'script', e.target.value)}
                      spellCheck={false}
                    />
                  </div>
                </div>
              ))}

              <div className="action-buttons">
                {kinds.length < 5 && (
                  <button className="btn" onClick={addKind}>+ Add Kind</button>
                )}
                <button className="btn btn-primary" onClick={submitKinds}>
                  Submit Kinds
                </button>
              </div>
            </div>
          </div>
        )}
        {errorMsg && <div className="error-toast">{errorMsg}</div>}
      </div>
    );
  }

  // ---- Spawn / Play / Over Phases ----
  const gs = gameState;
  const players = gs?.players || [{}, {}];

  return (
    <div className="app-container">
      <Header myRole={myRole} connected={connected} />

      <div className="main-layout">
        {/* ---- Left Panel: Player 1 Info ---- */}
        <div className="side-panel">
          <PlayerPanel
            player={players[0]}
            playerIdx={0}
            isActive={gs?.currentPlayer === 0}
            cfg={gs?.cfg}
          />
        </div>

        {/* ---- Center: Board + Controls ---- */}
        <div className="center-area">
          {/* Turn Banner */}
          {phase === 'play' && (
            <div className="turn-banner">
              <span>
                P{(gs?.currentPlayer || 0) + 1}'s Turn
              </span>
              <span style={{ color: 'var(--text-dim)' }}>|</span>
              <span>Turn #{gs?.turn || 0}</span>
              {turnPhase && (
                <span className={`phase-tag ${turnPhase}`}>{turnPhase}</span>
              )}
              {isMyTurn && <span style={{ color: 'var(--accent-green)' }}>◀ YOUR TURN</span>}
            </div>
          )}

          {phase === 'spawn' && (
            <div className="turn-banner">
              <span>INITIAL SPAWN</span>
              <span style={{ color: 'var(--text-dim)' }}>|</span>
              <span>P{(gs?.currentPlayer ?? 0) + 1} place your minion</span>
            </div>
          )}

          {/* Kind Selector (for spawn) */}
          {((phase === 'spawn') || (phase === 'play' && turnPhase === 'spawn')) && isMyTurn && !isSpectator && (
            <div style={{ marginBottom: 8, display: 'flex', gap: 8, alignItems: 'center' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontFamily: 'var(--font-display)', letterSpacing: 1 }}>
                SELECT KIND:
              </span>
              {kinds.map((k, i) => (
                <button
                  key={i}
                  className={`btn ${selectedKindIdx === i ? 'btn-primary' : ''}`}
                  onClick={() => setSelectedKindIdx(i)}
                  style={{ padding: '4px 12px', fontSize: '0.7rem' }}
                >
                  {k.name} (def:{k.defense})
                </button>
              ))}
            </div>
          )}

          {/* Hex Board */}
          <div className="hex-board-container">
            <HexBoard
              grid={gs?.grid || {}}
              players={players}
              currentPlayer={gs?.currentPlayer}
              turnPhase={turnPhase}
              phase={phase}
              highlightHexes={getHighlightHexes()}
              onHexClick={handleHexClick}
              myPlayer={myPlayerIdx}
            />
          </div>

          {/* Action Buttons */}
          {phase === 'play' && isMyTurn && !isSpectator && (
            <div className="action-buttons">
              {turnPhase === 'start' && (
                <button className="btn btn-primary" onClick={startTurn}>
                  ▶ Start Turn
                </button>
              )}
              {turnPhase === 'buy' && (
                <>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                    Click hex adjacent to spawn zone to buy (cost: {gs?.cfg?.hex_purchase_cost})
                  </span>
                  <button className="btn btn-skip" onClick={skipBuy}>
                    Skip Buy →
                  </button>
                </>
              )}
              {turnPhase === 'spawn' && (
                <>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                    Click spawn zone hex to spawn (cost: {gs?.cfg?.spawn_cost})
                  </span>
                  <button className="btn btn-skip" onClick={skipSpawn}>
                    Skip Spawn →
                  </button>
                </>
              )}
              {turnPhase === 'execute' && (
                <button className="btn btn-primary" onClick={executeStrats}>
                  ⚔ Execute Strategies
                </button>
              )}
            </div>
          )}

          {/* Spectator notice */}
          {isSpectator && phase === 'play' && (
            <div style={{ fontSize: '0.8rem', color: 'var(--accent-gold)', marginTop: 8, fontFamily: 'var(--font-display)', letterSpacing: 2 }}>
              👁 SPECTATING
            </div>
          )}

          {/* Game Log */}
          {gs?.log && gs.log.length > 0 && (
            <div className="game-log" style={{ width: '100%', maxWidth: 600 }}>
              <div className="log-toggle" onClick={() => setLogOpen(!logOpen)}>
                {logOpen ? '▼' : '▶'} Game Log ({gs.log.length})
              </div>
              {logOpen && (
                <div className="log-entries">
                  {gs.log.slice().reverse().map((entry, i) => (
                    <div
                      key={i}
                      className={`log-entry${
                        entry.includes('killed') ? ' kill' :
                        entry.includes('---') ? ' turn' :
                        entry.includes('spawned') ? ' spawn' : ''
                      }`}
                    >
                      {entry}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* ---- Right Panel: Player 2 Info ---- */}
        <div className="side-panel right">
          <PlayerPanel
            player={players[1]}
            playerIdx={1}
            isActive={gs?.currentPlayer === 1}
            cfg={gs?.cfg}
          />
        </div>
      </div>

      {/* ---- Game Over Overlay ---- */}
      {phase === 'over' && gs && (
        <div className="game-over-overlay">
          <div className="game-over-card">
            <h2>GAME OVER</h2>
            <div className={`winner-text ${
              gs.winner === 'P1' ? 'p1' : gs.winner === 'P2' ? 'p2' : 'draw'
            }`}>
              {gs.winner === 'P1' && '🏆 PLAYER 1 WINS'}
              {gs.winner === 'P2' && '🏆 PLAYER 2 WINS'}
              {gs.winner === 'draw' && '🤝 DRAW'}
            </div>
            <div style={{ marginBottom: 16, color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
              <div>P1: {players[0]?.minionCount || 0} minions, {players[0]?.totalHp || 0} HP, {players[0]?.budget || 0} budget</div>
              <div>P2: {players[1]?.minionCount || 0} minions, {players[1]?.totalHp || 0} HP, {players[1]?.budget || 0} budget</div>
              <div style={{ marginTop: 8 }}>Total turns: {gs.turn}</div>
            </div>
            <button className="btn btn-primary" onClick={resetGame}>
              Play Again
            </button>
          </div>
        </div>
      )}

      {/* Error Toast */}
      {errorMsg && <div className="error-toast">{errorMsg}</div>}
    </div>
  );
}

// ============================================================
//  Sub-Components
// ============================================================

function Header({ myRole, connected }) {
  return (
    <div className="header">
      <h1>KOMBAT</h1>
      <div className="header-info">
        <span style={{ color: connected ? 'var(--accent-green)' : 'var(--accent-red)', fontSize: '0.75rem' }}>
          {connected ? '● Connected' : '● Disconnected'}
        </span>
        {myRole && (
          <span className={`role-badge ${
            myRole.role === 'spectator' ? 'spectator' :
            myRole.player === 2 ? 'p2' : 'player'
          }`}>
            {myRole.role === 'spectator'
              ? 'Spectator'
              : `Player ${myRole.player}`
            }
          </span>
        )}
      </div>
    </div>
  );
}

function PlayerPanel({ player, playerIdx, isActive, cfg }) {
  if (!player) return null;

  const p = player;
  const label = `Player ${playerIdx + 1}`;
  const colorClass = playerIdx === 0 ? 'p1' : 'p2';

  return (
    <div>
      <div className={`panel-title ${colorClass}`}>
        {isActive && '▶ '}{label}
      </div>

      <div className="stat-row">
        <span className="stat-label">Budget</span>
        <span className="stat-value budget">{p.budget ?? 0}</span>
      </div>
      <div className="stat-row">
        <span className="stat-label">Minions</span>
        <span className="stat-value">{p.minionCount ?? 0}</span>
      </div>
      <div className="stat-row">
        <span className="stat-label">Total HP</span>
        <span className="stat-value hp">{p.totalHp ?? 0}</span>
      </div>
      <div className="stat-row">
        <span className="stat-label">Spawns Left</span>
        <span className="stat-value">{p.spawnsLeft ?? 0}</span>
      </div>
      <div className="stat-row">
        <span className="stat-label">Turn</span>
        <span className="stat-value">{p.turnCount ?? 0}</span>
      </div>

      {/* Minion List */}
      {p.minions && p.minions.length > 0 && (
        <div className="minion-list">
          <div className={`panel-title ${colorClass}`} style={{ fontSize: '0.6rem', marginTop: 12 }}>
            Minions
          </div>
          {p.minions.map((m) => (
            <div className="minion-card" key={m.id}>
              <div>
                <div className="name" style={{ color: playerIdx === 0 ? 'var(--p1-color)' : 'var(--p2-color)' }}>
                  {m.name}#{m.id}
                </div>
                <div style={{ fontSize: '0.7rem', color: 'var(--text-dim)' }}>
                  ({m.r},{m.c}) def:{m.def}
                </div>
                <div className="hp-bar">
                  <div
                    className="hp-fill"
                    style={{
                      width: `${Math.max(0, (m.hp / (cfg?.init_hp || 100)) * 100)}%`,
                      background: m.hp < 30 ? 'var(--accent-red)' : m.hp < 60 ? 'var(--accent-orange)' : 'var(--accent-green)',
                    }}
                  />
                </div>
              </div>
              <div style={{ fontFamily: 'var(--font-display)', fontSize: '0.8rem', color: 'var(--accent-green)' }}>
                {m.hp}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Kinds */}
      {p.kinds && p.kinds.length > 0 && (
        <div style={{ marginTop: 12 }}>
          <div className={`panel-title ${colorClass}`} style={{ fontSize: '0.6rem' }}>
            Kinds
          </div>
          {p.kinds.map((k, i) => (
            <div key={i} style={{ fontSize: '0.8rem', padding: '3px 0', color: 'var(--text-secondary)' }}>
              <span style={{ fontFamily: 'var(--font-display)', color: 'var(--text-primary)' }}>{k.name}</span>
              {' '}(def: {k.defense})
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
