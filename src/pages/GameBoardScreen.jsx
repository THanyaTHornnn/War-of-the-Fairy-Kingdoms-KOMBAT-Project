import { useState, useCallback, useEffect, useRef } from "react";
import { useGame, MINIONS } from "../context/GameContext";
import { generateHexGrid } from "../utils/hexUtils";
import HexGrid from "../components/HexGrid";


const PLAYER_COLOR = ['#4ade80', '#f87171'];

const P2_SPAWN_POSITIONS = [
  { row: 8, col: 8 }, { row: 8, col: 7 }, { row: 7, col: 8 },
  { row: 7, col: 7 }, { row: 8, col: 6 }
];

function applyMinions(hexes, minions) {
  const map = {};
  (minions || []).forEach(m => {
    const ownerId = typeof m.owner === "object" ? m.owner?.id : m.owner;
    const key = m.row + "-" + m.col;
    map[key] = { player: ownerId === "p1" ? 1 : 2, type: m.type, hp: m.hp };
  });
  return hexes.map(h => ({ ...h, minion: map[h.row + "-" + h.col] || null }));
}

export default function GameBoardScreen({ onGameEnd }) {
  const { gameState, updatePlayer, send, getPlayerId, setMessageHandler } = useGame();
  const [hexes, setHexes]              = useState(() => generateHexGrid());
  const [backendState, setBackendState] = useState(null);
  const [mode, setMode]                = useState(null);
  const [spawnPanel, setSpawnPanel]    = useState(null);
  const [selMinion, setSelMinion]      = useState(null);
  const [notif, setNotif]              = useState('');
  const [loading, setLoading]          = useState(false);
  const [gameOver, setGameOver]        = useState(false);
  const [p2Configs, setP2Configs]      = useState(null);
  const [myPlayerId, setMyPlayerId]    = useState(null);

  const turnRef = useRef(1);
  const sendRef = useRef(null);

  const phase   = backendState?.phase   || "SETUP";
  const current = backendState?.current || "p1";
  const turn    = current === "p1" ? 1 : 2;
  const round   = backendState?.turn    || 1;

  useEffect(() => { turnRef.current = turn; }, [turn]);

  const minionConfigs    = gameState.players[0]?.minionConfigs || [];
  const isSpectator = myPlayerId === "spectator" || 
                    (myPlayerId && myPlayerId !== "p1" && myPlayerId !== "p2");
  const isMyTurn = !isSpectator && (phase === "SETUP" ? true : current === myPlayerId);
  const activeConfigs    = myPlayerId === "p1" ? minionConfigs : (p2Configs || minionConfigs);
  const activeMinionIds  = activeConfigs.map(c => c.minionId).filter(Boolean);
  const availableMinions = activeMinionIds.length > 0
    ? MINIONS.filter(m => activeMinionIds.includes(m.id))
    : MINIONS;

  const notify = useCallback((msg) => {
    setNotif(msg);
    setTimeout(() => setNotif(''), 3000);
  }, []);

  const handleMessage = useCallback((msg) => {
    console.log("📨 WS msg:", msg);
    const { event, data } = msg;

    // เมื่อได้รับสิทธิ์หรือโดนเลื่อนขั้น
  // 1. ทันทีที่ต่อติด หรือ โดนเลื่อนขั้น
  if (event === "joined" && data?.playerId) {
    const assignedId = data.playerId;
    console.log("🎯 Server assigned ID:", assignedId);
    
    setMyPlayerId(assignedId); // อัปเดต State หน้าจอทันที
    sessionStorage.setItem("playerId", assignedId); // เซฟลงเครื่องกันเหนียว
    
    if (assignedId === "p2") {
      sendRef.current?.("get-configs", {});
    }
    return;
  }

    // กรณีคนอื่นออก (ที่เราคุยกันเรื่อง alert)
  if (event === "player_left") {
    const leftId = data.playerId;
    if (leftId !== myPlayerId && (leftId === "p1" || leftId === "p2")) {
      alert(`ผู้เล่น ${leftId.toUpperCase()} ออกจากเกมแล้ว!`);
    }
    return;
  }

    if (!msg.ok) {
      notify(msg.message || "Error");
      setLoading(false);
      return;
    }

    const state = (data && data.phase) ? data : (data?.state || null);
    if (state?.phase) setBackendState(state);
    if (state?.minions && Array.isArray(state.minions)) {
      setHexes(prev => applyMinions(prev, state.minions));
    }

    switch (event) {
      case "spawned":
        notify(state?.phase === "PLAYING" ? "✅ เกมเริ่มแล้ว!" : "✅ Spawn สำเร็จ!");
        setSpawnPanel(null); setSelMinion(null); setMode(null);
        break;
      case "waiting_for_p2": notify("⏳ รอ P2 spawn..."); break;
      case "spawn_failed":   notify("❌ Spawn ไม่ได้"); break;
      case "hex_purchased":  notify("✅ ซื้อ hex สำเร็จ!"); setMode(null); break;
      case "hex_failed":     notify("❌ ซื้อ hex ไม่ได้"); break;
      case "turn_executed":  notify("⚔️ Player " + (turnRef.current === 1 ? 2 : 1) + "'s Turn!"); break;
      case "game_over":
        setGameOver(true);
        notify("🏆 จบเกม! ผู้ชนะ: " + data?.winner);
        setTimeout(() => onGameEnd?.(data), 3000);
        break;
      default: break;
    }
    setLoading(false);
  }, [onGameEnd, notify, updatePlayer]);

  useEffect(() => {
  setMessageHandler(handleMessage);
  return () => setMessageHandler(null);
}, [handleMessage, setMessageHandler]);

useEffect(() => {
  sendRef.current = send;
}, [send]);

useEffect(() => {
  const currentId = getPlayerId();
  if (currentId) setMyPlayerId(currentId);
}, [getPlayerId]);

  const getStrategyFor = (minionId) => {
    const cfg = activeConfigs.find(c => c.minionId === minionId);
    return cfg?.strategy || "done";
  };

  const getDefenseFor = (minionId) => {
    const cfg = activeConfigs.find(c => c.minionId === minionId);
    if (cfg) return cfg.defense ?? 10;
    return MINIONS.find(m => m.id === minionId)?.defense ?? 10;
  };

  const handleHexClick = (hex) => {
    if (mode !== "hex" || gameOver || loading || !isMyTurn) return;
    setLoading(true);
    send("purchase-hex", { playerId: myPlayerId, row: hex.row, col: hex.col });
  };

  const handleSpawnHex = (hex) => {
    if (gameOver || !isMyTurn) return;
    if (myPlayerId === "p2") {
      const inZone = P2_SPAWN_POSITIONS.some(p => p.row === hex.row && p.col === hex.col);
      if (!inZone && phase === "SETUP") { notify("❌ ต้อง spawn ใน zone ของ P2"); return; }
    }
    setSpawnPanel(hex); setSelMinion(null);
  };

  const confirmSpawn = () => {
    const currentId = getPlayerId(); // ดึงค่าล่าสุดจาก Ref ใน Hook
  if (!currentId || currentId === "spectator") {
    notify("❌ คุณไม่มีสิทธิ์วางยูนิต (Status: " + currentId + ")");
    return;
  }
    send("spawn", {
      playerId: currentId, // ส่ง ID ที่แน่นอนที่สุดไป
      kindName: selMinion,
      row:      Math.floor(spawnPanel.row),
      col:      Math.floor(spawnPanel.col),
      defense:  Math.floor(getDefenseFor(selMinion)),
      strategy: getStrategyFor(selMinion),
    });
  };

  const endTurn = () => {
    if (gameOver || loading || !isMyTurn) return;
    setLoading(true);
    send("execute-turn", { playerId: myPlayerId });
  };

  const toggleMode = (m) => {
    if (gameOver || loading || !isMyTurn) return;
    setMode(prev => prev === m ? null : m);
    setSpawnPanel(null); setSelMinion(null);
  };

  return (
    <div style={{
      width: '100vw', height: '100vh', display: 'flex',
      background: 'radial-gradient(ellipse at 30% 50%, #1e1b4b 0%, #0f0c29 50%, #0a0a1a 100%)',
      fontFamily: "'Cinzel', serif", overflow: 'hidden', position: 'relative',
    }}>
      {notif && (
        <div style={{
          position: 'absolute', top: 12, left: '50%', transform: 'translateX(-50%)',
          background: 'rgba(0,0,0,0.88)', backdropFilter: 'blur(10px)',
          border: '1px solid rgba(255,255,255,0.2)', borderRadius: 30,
          padding: '8px 24px', color: '#fff', fontSize: 13, zIndex: 300, whiteSpace: 'nowrap',
        }}>{notif}</div>
      )}

      {!isMyTurn && phase === "PLAYING" && (
        <div style={{
          position: 'absolute', top: 44, left: '50%', transform: 'translateX(-50%)',
          background: 'rgba(239,68,68,0.15)', border: '1px solid rgba(239,68,68,0.5)',
          borderRadius: 20, padding: '5px 18px', color: '#fca5a5', fontSize: 11, zIndex: 200,
        }}>⏳ รอ Player {myPlayerId === "p1" ? 2 : 1}...</div>
      )}

      <PlayerHUD
        playerNum={1} isTurn={turn === 1 && phase === "PLAYING"} isMe={myPlayerId === "p1"}
        budget={backendState?.p1?.budget ?? 0}
        spawnsLeft={backendState?.p1?.spawns ?? 0}
        hp={backendState?.p1?.hp ?? 0}
        color={PLAYER_COLOR[0]} side="left"
      />

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0, minHeight: 0 }}>
        <div style={{ textAlign: 'center', padding: '6px 0 2px', flexShrink: 0 }}>
          <span style={{
            fontSize: 'clamp(13px,1.8vw,22px)', fontWeight: 900,
            color: '#e2d9f3', textShadow: '0 0 18px #c4b5fd', letterSpacing: '0.12em',
          }}>{"ROUND " + round}</span>
          <span style={{ color: '#fbbf24', fontSize: 10, letterSpacing: 2, marginLeft: 10 }}>⚡ P{turn}</span>
          <span style={{ color: '#c4b5fd', fontSize: 10, marginLeft: 8 }}>
  {myPlayerId === "p1" ? "(คุณคือ 🟢 P1)" : 
   myPlayerId === "p2" ? "(คุณคือ 🔴 P2)" : 
   "(คุณคือ 👁 SPECTATOR)"}
</span>
        </div>

        <div style={{ flex: 1, minHeight: 0, padding: '2px 8px 0' }}>
          <HexGrid
            hexes={hexes}
            onHexClick={handleHexClick}
            onSpawnHex={handleSpawnHex}
            selectedHex={null}
            mode={mode}
            currentTurn={myPlayerId === "p1" ? 1 : 2}
          />
        </div>
        {!isSpectator && (
        <div style={{ flexShrink: 0, display: 'flex', justifyContent: 'center', gap: 8, padding: '4px 0 6px' }}>
          <Btn label="Buy Hex" active={mode === 'hex'} color="#a78bfa"
            onClick={() => toggleMode('hex')} small disabled={loading || gameOver || !isMyTurn} />
          <Btn label="Spawn" active={mode === 'spawn'} color="#818cf8"
            onClick={() => toggleMode('spawn')} small disabled={loading || gameOver || !isMyTurn} />
          <Btn label={loading ? "กำลังประมวล..." : "End Turn ►"}
            color="#f59e0b" bold onClick={endTurn} small disabled={loading || gameOver || !isMyTurn} />
        </div> )}
        {isSpectator && (
          <div style={{ 
            textAlign: 'center', padding: '8px', 
            color: 'rgba(255,255,255,0.3)', fontSize: 11, letterSpacing: 2 
          }}>
            👁 SPECTATOR MODE — ดูเกมอย่างเดียว
          </div>
        )}
      </div>
      
      <PlayerHUD
        playerNum={2} isTurn={turn === 2 && phase === "PLAYING"} isMe={myPlayerId === "p2"}
        budget={backendState?.p2?.budget ?? 0}
        spawnsLeft={backendState?.p2?.spawns ?? 0}
        hp={backendState?.p2?.hp ?? 0}
        color={PLAYER_COLOR[1]} side="right"
      />

      {spawnPanel && mode === 'spawn' && isMyTurn && (
        <div style={{
          position: 'absolute', left: 0, top: 0, bottom: 0,
          width: 'clamp(160px,18vw,200px)',
          background: 'rgba(10,10,26,0.96)', backdropFilter: 'blur(16px)',
          borderRight: '1px solid rgba(196,181,253,0.25)',
          display: 'flex', flexDirection: 'column', padding: '20px 14px', gap: 10, zIndex: 200,
        }}>
          <div style={{ color: '#c4b5fd', fontSize: 11, letterSpacing: 2, marginBottom: 4 }}>SPAWN MINION</div>
          <div style={{ color: 'rgba(255,255,255,0.4)', fontSize: 10, marginBottom: 8 }}>
            hex ({spawnPanel.row},{spawnPanel.col})
          </div>
          {availableMinions.map((m) => (
            <button key={m.id} onClick={() => setSelMinion(m.id)} style={{
              display: 'flex', alignItems: 'center', gap: 10,
              padding: '10px 12px', borderRadius: 12, cursor: 'pointer',
              border: selMinion === m.id ? `1.5px solid ${m.color}` : '1px solid rgba(255,255,255,0.12)',
              background: selMinion === m.id ? `${m.color}22` : 'rgba(255,255,255,0.04)',
              color: '#fff', fontFamily: "'Cinzel', serif", fontSize: 11,
            }}>
              <span style={{ fontSize: 20 }}>{m.emoji}</span>
              <div style={{ textAlign: 'left' }}>
                <div style={{ fontWeight: 700, color: m.color }}>{m.name}</div>
                <div style={{ fontSize: 9, color: 'rgba(255,255,255,0.4)' }}>DEF {getDefenseFor(m.id)}</div>
              </div>
            </button>
          ))}
          <div style={{ flex: 1 }} />
          <button onClick={confirmSpawn} disabled={loading || !selMinion} style={{
            padding: '10px', borderRadius: 20,
            cursor: selMinion && !loading ? 'pointer' : 'not-allowed',
            border: '1.5px solid rgba(134,239,172,0.6)',
            background: 'rgba(134,239,172,0.15)', color: '#86efac',
            fontFamily: "'Cinzel', serif", fontSize: 12, fontWeight: 700,
            opacity: selMinion && !loading ? 1 : 0.4,
          }}>{loading ? '...' : 'CONFIRM ✓'}</button>
          <button onClick={() => { setSpawnPanel(null); setSelMinion(null); setMode(null); }} style={{
            padding: '7px', borderRadius: 20, cursor: 'pointer', border: 'none',
            background: 'transparent', color: 'rgba(255,255,255,0.3)',
            fontFamily: "'Cinzel', serif", fontSize: 11,
          }}>✕ Cancel</button>
        </div>
      )}
    </div>
  );
}

function PlayerHUD({ playerNum, isTurn, isMe, budget, spawnsLeft, hp, color, side }) {
  return (
    <div style={{
      width: 'clamp(140px,15vw,180px)', flexShrink: 0,
      display: 'flex', flexDirection: 'column', justifyContent: 'center',
      padding: side === 'left' ? '12px 8px 12px 10px' : '12px 10px 12px 8px', gap: 8,
    }}>
      <div style={{
        background: 'rgba(255,255,255,0.07)', backdropFilter: 'blur(14px)',
        border: isTurn ? `2px solid ${color}` : '1px solid rgba(255,255,255,0.12)',
        borderRadius: 16, padding: '12px',
        boxShadow: isTurn ? `0 0 20px ${color}55` : 'none', transition: 'all 0.3s',
      }}>
        {isTurn && (
          <div style={{
            background: color, color: '#0a0a1a', fontSize: 8, fontWeight: 700,
            padding: '2px 8px', borderRadius: 20, letterSpacing: 1, marginBottom: 8, textAlign: 'center',
          }}>⚡ YOUR TURN</div>
        )}
        <div style={{ fontSize: 11, fontWeight: 700, color, letterSpacing: 1, marginBottom: 4, textAlign: 'center' }}>
          PLAYER {playerNum} {isMe ? "👤" : ""}
        </div>
        {[['❤️ HP', hp, color], ['💰', Math.floor(budget).toLocaleString(), '#fbbf24'], ['Spawns', spawnsLeft, '#c4b5fd']].map(([l, v, c]) => (
          <div key={l} style={{
            display: 'flex', justifyContent: 'space-between', alignItems: 'center',
            background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)',
            borderRadius: 8, padding: '4px 8px', marginBottom: 6,
          }}>
            <span style={{ color: 'rgba(255,255,255,0.5)', fontSize: 9 }}>{l}</span>
            <span style={{ color: c, fontSize: 11, fontWeight: 700, fontFamily: 'monospace' }}>{v}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function Btn({ label, onClick, active = false, color = '#fff', bold = false, small = false, disabled = false }) {
  return (
    <button onClick={onClick} disabled={disabled} style={{
      padding: small ? '6px 14px' : '9px 20px',
      borderRadius: 26, fontSize: small ? 10 : 12,
      fontWeight: bold ? 700 : 600, letterSpacing: '0.06em',
      border: `1.5px solid ${active ? color : 'rgba(255,255,255,0.22)'}`,
      background: active ? `${color}33` : 'rgba(255,255,255,0.08)',
      backdropFilter: 'blur(10px)', color: active ? color : '#fff',
      cursor: disabled ? 'not-allowed' : 'pointer',
      fontFamily: "'Cinzel', serif",
      boxShadow: active ? `0 0 12px ${color}55` : 'none',
      transition: 'all 0.2s', opacity: disabled ? 0.6 : 1,
    }}>{label}</button>
  );
}