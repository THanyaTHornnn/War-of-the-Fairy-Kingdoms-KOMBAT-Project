import { useGame } from "../context/GameContext";
import { generateHexGrid } from "../utils/hexUtils";
import { BASE_HP, BASE_BUDGET } from "../context/GameContext";

// ============================================================
// 🔧 EDIT HERE: แก้ animation / สี winner screen ตรงนี้
// ============================================================
export default function GameOverScreen({ winner, onPlayAgain }) {
  const { gameState, setGameState } = useGame();
  const winnerName = winner !== null ? gameState.players[winner]?.name : "Draw";

  const handlePlayAgain = () => {
    // ============================================================
    // 🔧 EDIT HERE: Reset state ทั้งหมด + เรียก API reset
    // await api.post('/game/reset')
    // ============================================================
    setGameState(prev => ({
      ...prev,
      round: 1,
      currentTurn: 1,
      phase: "setup",
      winner: null,
      players: prev.players.map(p => ({
        ...p,
        hp: BASE_HP,
        budget: BASE_BUDGET,
        minionCounts: Object.fromEntries(Object.keys(p.minionCounts).map(k => [k, 0])),
        strategy: "",
      })),
      hexGrid: generateHexGrid(),
    }));
    onPlayAgain();
  };

  return (
    <div style={{
      width: "100vw",
      height: "100vh",
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      // 🖼️ EDIT HERE: backgroundImage: "url('/bg/end.jpg')"
      background: "radial-gradient(ellipse at center, #0f172a 0%, #0a0a1a 100%)",
      fontFamily: "'Cinzel', serif",
      position: "relative",
      overflow: "hidden",
    }}>

      {/* Particle burst */}
      {[...Array(20)].map((_, i) => (
        <div key={i} style={{
          position: "absolute",
          fontSize: 24,
          top: `${Math.random() * 100}%`,
          left: `${Math.random() * 100}%`,
          animation: `fall ${Math.random() * 4 + 3}s linear ${Math.random() * 2}s infinite`,
          opacity: 0.6,
        }}>
          {["✨", "⭐", "💫", "🌟"][Math.floor(Math.random() * 4)]}
        </div>
      ))}

      {/* Trophy */}
      <div style={{
        fontSize: 80,
        marginBottom: 24,
        animation: "bounceIn 0.8s cubic-bezier(0.34,1.56,0.64,1)",
        filter: "drop-shadow(0 0 30px #fbbf24)",
      }}>
        🏆
      </div>

      {/* Winner announcement */}
      <div style={{
        fontSize: "clamp(14px, 2vw, 20px)",
        color: "rgba(255,255,255,0.6)",
        letterSpacing: "0.3em",
        marginBottom: 8,
        animation: "fadeIn 1s ease 0.3s both",
      }}>
        WINNER
      </div>
      <div style={{
        fontSize: "clamp(36px, 6vw, 80px)",
        fontWeight: 900,
        color: "#fbbf24",
        textShadow: "0 0 40px #f59e0b, 0 0 80px #fbbf24",
        marginBottom: 16,
        letterSpacing: "0.05em",
        animation: "fadeIn 1s ease 0.5s both",
      }}>
        {winnerName}
      </div>

      {/* Stats summary */}
      <div style={{
        display: "flex",
        gap: 24,
        marginBottom: 50,
        animation: "fadeIn 1s ease 0.8s both",
        flexWrap: "wrap",
        justifyContent: "center",
      }}>
        {gameState.players.map((p, i) => (
          <div key={i} style={{
            background: "rgba(255,255,255,0.07)",
            backdropFilter: "blur(10px)",
            border: `1px solid ${winner === i ? "rgba(251,191,36,0.5)" : "rgba(255,255,255,0.1)"}`,
            borderRadius: 16,
            padding: "16px 28px",
            textAlign: "center",
            minWidth: 140,
          }}>
            <div style={{ color: "rgba(255,255,255,0.5)", fontSize: 12, marginBottom: 6 }}>{p.name}</div>
            <div style={{ color: winner === i ? "#fbbf24" : "#fff", fontSize: 22, fontWeight: 700 }}>
              {p.hp > 0 ? `HP: ${p.hp}` : "💀 Defeated"}
            </div>
          </div>
        ))}
      </div>

      {/* Buttons */}
      <div style={{ display: "flex", gap: 16, animation: "fadeIn 1s ease 1.1s both" }}>
        {/* PLAY AGAIN */}
        <button
          onClick={handlePlayAgain}
          style={{
            padding: "14px 48px",
            fontSize: 18,
            fontFamily: "'Cinzel', serif",
            fontWeight: 700,
            letterSpacing: "0.12em",
            // ============================================================
            // 🎨 EDIT HERE: สี Play Again button
            // ============================================================
            background: "linear-gradient(135deg, #7c3aed, #a855f7)",
            border: "none",
            borderRadius: "40px",
            color: "#fff",
            cursor: "pointer",
            boxShadow: "0 8px 30px rgba(124,58,237,0.6)",
            transition: "all 0.25s",
          }}
          onMouseEnter={e => { e.currentTarget.style.transform = "scale(1.06)"; e.currentTarget.style.boxShadow = "0 12px 40px rgba(168,85,247,0.7)"; }}
          onMouseLeave={e => { e.currentTarget.style.transform = "none"; e.currentTarget.style.boxShadow = "0 8px 30px rgba(124,58,237,0.6)"; }}
        >
          ↺ PLAY AGAIN
        </button>

        {/* MAIN MENU */}
        <button
          onClick={onPlayAgain}
          style={{
            padding: "14px 36px",
            fontSize: 16,
            fontFamily: "'Cinzel', serif",
            fontWeight: 600,
            letterSpacing: "0.1em",
            background: "rgba(255,255,255,0.08)",
            backdropFilter: "blur(10px)",
            border: "1px solid rgba(255,255,255,0.25)",
            borderRadius: "40px",
            color: "rgba(255,255,255,0.7)",
            cursor: "pointer",
            transition: "all 0.25s",
          }}
          onMouseEnter={e => { e.currentTarget.style.background = "rgba(255,255,255,0.15)"; }}
          onMouseLeave={e => { e.currentTarget.style.background = "rgba(255,255,255,0.08)"; }}
        >
          Main Menu
        </button>
      </div>

      <style>{`
        @keyframes bounceIn { 0%{transform:scale(0);opacity:0} 100%{transform:scale(1);opacity:1} }
        @keyframes fadeIn { 0%{opacity:0;transform:translateY(20px)} 100%{opacity:1;transform:translateY(0)} }
        @keyframes fall { 0%{transform:translateY(-40px) rotate(0deg);opacity:1} 100%{transform:translateY(110vh) rotate(360deg);opacity:0} }
      `}</style>
    </div>
  );
}
