import { MINIONS } from "../context/GameContext";
import MinionCharacter from "./MinionCharacter";

// ============================================================
// 🔧 EDIT HERE: เปลี่ยนสี HP bar / Budget bar ตรงนี้
// ============================================================
export default function PlayerHUD({ player, playerNum, isCurrentTurn, maxHp = 100, maxBudget = 500 }) {
  const hpPct = Math.max(0, (player.hp / maxHp) * 100);
  const budgetPct = Math.max(0, (player.budget / maxBudget) * 100);
  const hpColor = hpPct > 50 ? "#22c55e" : hpPct > 25 ? "#f59e0b" : "#ef4444";

  return (
    <div style={{
      background: "rgba(255,255,255,0.12)",
      backdropFilter: "blur(12px)",
      border: isCurrentTurn
        ? "2px solid rgba(255,215,0,0.8)"
        : "1px solid rgba(255,255,255,0.2)",
      borderRadius: "16px",
      padding: "14px",
      position: "relative",
      boxShadow: isCurrentTurn ? "0 0 20px rgba(255,215,0,0.4)" : "none",
      transition: "all 0.3s",
    }}>

      {/* Turn indicator */}
      {isCurrentTurn && (
        <div style={{
          position: "absolute",
          top: -10,
          right: 12,
          background: "#fbbf24",
          color: "#1e1b4b",
          fontSize: 11,
          fontWeight: 700,
          padding: "2px 10px",
          borderRadius: 20,
          letterSpacing: 1,
        }}>
          ⚡ YOUR TURN
        </div>
      )}

      {/* Player name */}
      <div style={{
        fontFamily: "'Cinzel', serif",
        // ============================================================
        // 🔧 EDIT HERE: เปลี่ยนชื่อ Player ตรงนี้ / เชื่อม backend
        // ============================================================
        fontSize: 18,
        fontWeight: 700,
        color: "#fff",
        marginBottom: 10,
        textAlign: "center",
        letterSpacing: 1,
      }}>
        {player.name}
      </div>

      {/* HP Bar */}
      <div style={{ marginBottom: 6 }}>
        <div style={{ display: "flex", justifyContent: "space-between", fontSize: 11, color: "rgba(255,255,255,0.7)", marginBottom: 3 }}>
          <span>HP</span>
          <span>{player.hp}/{maxHp}</span>
        </div>
        <div style={{ height: 8, background: "rgba(0,0,0,0.3)", borderRadius: 4, overflow: "hidden" }}>
          <div style={{
            width: `${hpPct}%`,
            height: "100%",
            background: hpColor,
            borderRadius: 4,
            transition: "width 0.5s ease",
            boxShadow: `0 0 6px ${hpColor}`,
          }} />
        </div>
      </div>

      {/* Budget Bar */}
      <div style={{ marginBottom: 10 }}>
        <div style={{ display: "flex", justifyContent: "space-between", fontSize: 11, color: "rgba(255,255,255,0.7)", marginBottom: 3 }}>
          <span>Budget</span>
          <span>💰 {player.budget}</span>
        </div>
        <div style={{ height: 8, background: "rgba(0,0,0,0.3)", borderRadius: 4, overflow: "hidden" }}>
          <div style={{
            width: `${budgetPct}%`,
            height: "100%",
            background: "linear-gradient(90deg, #22c55e, #86efac)",
            borderRadius: 4,
            transition: "width 0.5s ease",
          }} />
        </div>
      </div>

      {/* Minion counts */}
      <div style={{
        display: "flex",
        justifyContent: "space-around",
        gap: 4,
      }}>
        {MINIONS.map(m => (
          <div key={m.id} style={{ textAlign: "center" }}>
            <MinionCharacter minionId={m.id} size={40} spin={false} />
            {/* ============================================================
                🔧 EDIT HERE: count มาจาก API → player.minionCounts[m.id]
                ============================================================ */}
            <div style={{ fontSize: 12, color: "#fff", fontWeight: 700, marginTop: 2 }}>
              {player.minionCounts?.[m.id] ?? 0}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
