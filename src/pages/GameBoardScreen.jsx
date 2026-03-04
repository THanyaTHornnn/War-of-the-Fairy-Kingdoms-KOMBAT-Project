import { useState } from "react";
import { useGame, MINIONS } from "../context/GameContext";
import { generateHexGrid, isAdjacentToZone } from "../utils/hexUtils";
import HexGrid from "../components/HexGrid";

const API          = "http://localhost:8080/api/game";
const SPAWN_COST   = 100;
const HEX_COST     = 1000;
const TURN_BUDGET  = 90;
const MAX_BUDGET   = 99999;
const MAX_SPAWNS   = 47;
const INIT_HP      = 100;
const INTEREST_PCT = 5;
const INIT_BUDGET  = 50000;
const PLAYER_COLOR = ['#4ade80', '#f87171'];

// แปลง minions จาก backend → วางบน hex grid
function applyMinions(hexes, minions) {
  const map = {};
  (minions || []).forEach(m => {
    map[`${m.row}-${m.col}`] = { player: m.owner === "p1" ? 1 : 2, type: m.type, hp: m.hp };
  });
  return hexes.map(h => ({ ...h, minion: map[`${h.row}-${h.col}`] || null }));
}

export default function GameBoardScreen({ onGameEnd }) {
  const { gameState, updatePlayer } = useGame();
  const [hexes, setHexes]           = useState(() => generateHexGrid());
  const [selectedHex, setSelectedHex] = useState(null);
  const [mode, setMode]             = useState(null);
  const [selMinion, setSelMinion]   = useState(null);
  const [notif, setNotif]           = useState('');
  const [round, setRound]           = useState(1);
  const [turn, setTurn]             = useState(1);
  const [spawnsLeft, setSpawnsLeft] = useState([MAX_SPAWNS, MAX_SPAWNS]);
  const [budgets, setBudgets]       = useState([INIT_BUDGET, INIT_BUDGET]);
  const [spawnPanel, setSpawnPanel] = useState(null);
  const [loading, setLoading]       = useState(false);

  const curBudget = budgets[turn - 1];
  const notify = (msg) => { setNotif(msg); setTimeout(() => setNotif(''), 2500); };

  // ── Buy Hex ──────────────────────────────────────────────
  const handleHexClick = (hex) => {
    const gridMap = {};
    hexes.forEach(h => { gridMap[h.id] = h; });
    if (mode === 'hex') {
      if (hex.zone !== 0) { notify('❌ hex นี้เป็น zone อยู่แล้ว'); return; }
      if (!isAdjacentToZone(hex.row, hex.col, turn, gridMap)) { notify('❌ ต้อง adjacent กับ zone ของคุณ'); return; }
      if (curBudget < HEX_COST) { notify(`❌ Budget ไม่พอ (ต้องการ ${HEX_COST})`); return; }
      setHexes(prev => prev.map(h => h.id === hex.id ? { ...h, zone: turn } : h));
      setBudgets(prev => prev.map((b, i) => i === turn - 1 ? b - HEX_COST : b));
      notify(`✅ ซื้อ hex (${hex.row},${hex.col})!`);
      setMode(null); setSelectedHex(null);
    } else {
      setSelectedHex(hex.id === selectedHex ? null : hex.id);
    }
  };

  const handleSpawnHex = (hex) => { setSpawnPanel(hex); setSelMinion(null); };

  // ── Spawn Minion → เรียก /spawn แล้วอัปเดต grid ──────────
  const confirmSpawn = async () => {
    if (!spawnPanel || !selMinion) { notify('❌ เลือก minion ก่อน'); return; }
    if (spawnsLeft[turn - 1] <= 0) { notify('❌ หมด max_spawns แล้ว'); return; }
    if (curBudget < SPAWN_COST) { notify(`❌ Budget ไม่พอ (ต้องการ ${SPAWN_COST})`); return; }

    const strategy = gameState.players[turn - 1]?.strategy || "move up";
    const minionDef = MINIONS.find(m => m.id === selMinion);
    const defense = minionDef?.defense ?? 10;

    try {
      const res = await fetch(`${API}/spawn`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          playerId: turn === 1 ? "p1" : "p2",
          kindName: selMinion,
          row: spawnPanel.row,
          col: spawnPanel.col,
          defense,
          strategy,
        }),
      });
      const data = await res.json();
      if (data.ok) {
        // อัปเดต minion positions จาก backend
        setHexes(prev => applyMinions(prev, data.data?.minions));
        const budget = turn === 1 ? data.data?.p1?.budget : data.data?.p2?.budget;
        if (budget !== undefined) setBudgets(prev => prev.map((b, i) => i === turn - 1 ? budget : b));
        else setBudgets(prev => prev.map((b, i) => i === turn - 1 ? b - SPAWN_COST : b));
      } else {
        notify(`❌ ${data.message}`); return;
      }
    } catch {
      // fallback ถ้า backend ไม่ตอบ
      setHexes(prev => prev.map(h =>
        h.id === spawnPanel.id ? { ...h, minion: { player: turn, type: selMinion, hp: INIT_HP } } : h
      ));
      setBudgets(prev => prev.map((b, i) => i === turn - 1 ? b - SPAWN_COST : b));
    }

    setSpawnsLeft(prev => prev.map((v, i) => i === turn - 1 ? v - 1 : v));
    notify(`✅ Spawn ${selMinion}!`);
    setSpawnPanel(null); setSelMinion(null); setMode(null);
  };

  const toggleMode = (m) => {
    setMode(prev => prev === m ? null : m);
    setSelectedHex(null); setSpawnPanel(null);
    if (m !== 'spawn') setSelMinion(null);
  };

  // ── End Turn → backend รัน strategy → อัปเดต minion positions ──
  const endTurn = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API}/execute-turn`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ playerId: turn === 1 ? "p1" : "p2" }),
      });
      const data = await res.json();

      if (data.ok) {
        const state = data.data?.state || data.data;

        // อัปเดต minion positions จาก backend (minion ขยับตาม strategy)
        if (state?.minions) {
          setHexes(prev => applyMinions(prev, state.minions));
        }

        // อัปเดต budget จาก backend
        if (state?.p1?.budget !== undefined) {
          setBudgets([state.p1.budget, state.p2.budget]);
        } else {
          // fallback คำนวณ budget เอง
          let b = budgets[turn - 1] + TURN_BUDGET;
          if (b >= 1 && round > 1) {
            const r = INTEREST_PCT * Math.log10(b) * Math.log(round);
            b = Math.min(Math.floor(b + b * r / 100), MAX_BUDGET);
          }
          b = Math.min(b, MAX_BUDGET);
          setBudgets(prev => prev.map((v, i) => i === turn - 1 ? b : v));
        }

        // เกมจบ
        if (data.data?.isOver) {
          notify(`🏆 จบเกม! ผู้ชนะ: ${data.data.winner}`);
          setTimeout(() => onGameEnd?.(), 2500);
          setLoading(false);
          return;
        }
      } else {
        // backend error → คำนวณ budget เอง
        let b = budgets[turn - 1] + TURN_BUDGET;
        if (b >= 1 && round > 1) {
          const r = INTEREST_PCT * Math.log10(b) * Math.log(round);
          b = Math.min(Math.floor(b + b * r / 100), MAX_BUDGET);
        }
        setBudgets(prev => prev.map((v, i) => i === turn - 1 ? Math.min(b, MAX_BUDGET) : v));
      }
    } catch {
      // backend ไม่ตอบ → คำนวณ frontend อย่างเดียว
      let b = budgets[turn - 1] + TURN_BUDGET;
      if (b >= 1 && round > 1) {
        const r = INTEREST_PCT * Math.log10(b) * Math.log(round);
        b = Math.min(Math.floor(b + b * r / 100), MAX_BUDGET);
      }
      setBudgets(prev => prev.map((v, i) => i === turn - 1 ? Math.min(b, MAX_BUDGET) : v));
    }

    const nextTurn = turn === 1 ? 2 : 1;
    if (nextTurn === 1) setRound(r => r + 1);
    setTurn(nextTurn);
    setMode(null); setSelMinion(null); setSelectedHex(null); setSpawnPanel(null);
    notify(`⚔️ Player ${nextTurn}'s Turn!`);
    setLoading(false);
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

      <PlayerHUD player={gameState.players[0]} playerNum={1}
        isTurn={turn === 1} budget={budgets[0]} spawnsLeft={spawnsLeft[0]}
        initHp={INIT_HP} color={PLAYER_COLOR[0]} side="left" />

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0, minHeight: 0 }}>
        <div style={{ textAlign: 'center', padding: '6px 0 2px', flexShrink: 0 }}>
          <span style={{
            fontSize: 'clamp(13px,1.8vw,22px)', fontWeight: 900,
            color: '#e2d9f3', textShadow: '0 0 18px #c4b5fd', letterSpacing: '0.12em',
          }}>ROUND {round}</span>
          <span style={{ color: '#fbbf24', fontSize: 10, letterSpacing: 2, marginLeft: 10 }}>⚡ P{turn}</span>
        </div>

        <div style={{ flex: 1, minHeight: 0, padding: '2px 8px 0' }}>
          <HexGrid hexes={hexes} onHexClick={handleHexClick} onSpawnHex={handleSpawnHex}
            selectedHex={selectedHex} mode={mode} currentTurn={turn} />
        </div>

        <div style={{ flexShrink: 0, display: 'flex', justifyContent: 'center', gap: 8, padding: '4px 0 6px' }}>
          <Btn label="Buy Hex"  active={mode==='hex'}   color="#a78bfa" onClick={() => toggleMode('hex')} small />
          <Btn label="Spawn"    active={mode==='spawn'} color="#818cf8" onClick={() => toggleMode('spawn')} small />
          <Btn label={loading ? "กำลังประมวล..." : "End Turn ►"}
            color="#f59e0b" bold onClick={endTurn} small disabled={loading} />
        </div>
      </div>

      <PlayerHUD player={gameState.players[1]} playerNum={2}
        isTurn={turn === 2} budget={budgets[1]} spawnsLeft={spawnsLeft[1]}
        initHp={INIT_HP} color={PLAYER_COLOR[1]} side="right" />

      {spawnPanel && (
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
          {MINIONS.map((m) => (
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
                <div style={{ fontSize: 9, color: 'rgba(255,255,255,0.4)' }}>DEF {m.defense}</div>
              </div>
            </button>
          ))}
          <div style={{ flex: 1 }} />
          <button onClick={confirmSpawn} style={{
            padding: '10px', borderRadius: 20, cursor: 'pointer',
            border: '1.5px solid rgba(134,239,172,0.6)',
            background: 'rgba(134,239,172,0.15)', color: '#86efac',
            fontFamily: "'Cinzel', serif", fontSize: 12, fontWeight: 700,
          }}>CONFIRM ✓</button>
          <button onClick={() => { setSpawnPanel(null); setSelMinion(null); }} style={{
            padding: '7px', borderRadius: 20, cursor: 'pointer', border: 'none',
            background: 'transparent', color: 'rgba(255,255,255,0.3)',
            fontFamily: "'Cinzel', serif", fontSize: 11,
          }}>✕ Cancel</button>
        </div>
      )}
    </div>
  );
}

function PlayerHUD({ player, playerNum, isTurn, initHp, budget, spawnsLeft, color, side }) {
  const MINION_IDS = ['verdant','celestia','ivy','nyx','mibi'];
  const EMOJIS     = ['🌸','💜','🍀','🐉','⚔️'];
  return (
    <div style={{
      width: 'clamp(140px,15vw,180px)', flexShrink: 0,
      display: 'flex', flexDirection: 'column', justifyContent: 'center',
      padding: side === 'left' ? '12px 8px 12px 10px' : '12px 10px 12px 8px', gap: 8,
    }}>
      <div style={{
        background: 'rgba(255,255,255,0.07)', backdropFilter: 'blur(14px)',
        border: isTurn ? `2px solid ${color}` : '1px solid rgba(255,255,255,0.12)',
        borderRadius: 16, padding: '12px 12px',
        boxShadow: isTurn ? `0 0 20px ${color}55` : 'none', transition: 'all 0.3s',
      }}>
        {isTurn && (
          <div style={{
            background: color, color: '#0a0a1a', fontSize: 8, fontWeight: 700,
            padding: '2px 8px', borderRadius: 20, letterSpacing: 1, marginBottom: 8, textAlign: 'center',
          }}>⚡ YOUR TURN</div>
        )}
        <div style={{ fontSize: 11, fontWeight: 700, color, letterSpacing: 1, marginBottom: 8, textAlign: 'center' }}>
          PLAYER {playerNum}
        </div>
        <div style={{
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
          background: `${color}18`, border: `1px solid ${color}44`,
          borderRadius: 8, padding: '4px 8px', marginBottom: 6,
        }}>
          <span style={{ color: 'rgba(255,255,255,0.5)', fontSize: 9 }}>❤️ HP</span>
          <span style={{ color, fontSize: 12, fontWeight: 700, fontFamily: 'monospace' }}>
            {player.hp ?? initHp}/{initHp}
          </span>
        </div>
        <div style={{
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
          background: 'rgba(251,191,36,0.1)', border: '1px solid rgba(251,191,36,0.25)',
          borderRadius: 8, padding: '4px 8px', marginBottom: 8,
        }}>
          <span style={{ color: 'rgba(255,255,255,0.5)', fontSize: 9 }}>💰</span>
          <span style={{ color: '#fbbf24', fontSize: 11, fontWeight: 700, fontFamily: 'monospace' }}>
            {(budget ?? 0).toLocaleString()}
          </span>
        </div>
        <div style={{
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
          background: 'rgba(196,181,253,0.08)', border: '1px solid rgba(196,181,253,0.2)',
          borderRadius: 8, padding: '4px 8px', marginBottom: 8,
        }}>
          <span style={{ color: 'rgba(255,255,255,0.4)', fontSize: 9 }}>Spawns</span>
          <span style={{ color: '#c4b5fd', fontSize: 11, fontWeight: 700, fontFamily: 'monospace' }}>{spawnsLeft}</span>
        </div>
        <div style={{ color: 'rgba(255,255,255,0.3)', fontSize: 8, letterSpacing: 1, marginBottom: 4 }}>MINIONS</div>
        <div style={{ display: 'flex', justifyContent: 'space-around' }}>
          {MINION_IDS.map((id, i) => (
            <div key={id} style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 12 }}>{EMOJIS[i]}</div>
              <div style={{ fontSize: 9, color: '#fff', fontWeight: 700 }}>
                {player.minionCounts?.[id] ?? 0}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function Btn({ label, onClick, active=false, color='#fff', bold=false, small=false, disabled=false }) {
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