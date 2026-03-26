import { useGame } from "../context/GameContext";

const WS_URL = "ws://localhost:8080/ws/game";

export default function GameOverScreen({ winnerData, onPlayAgain }) {
  const { gameState, setGameState } = useGame();
  const mode = gameState.mode || "pvp";

  const getPlayerName = (pid) => {
    if (mode === "bvb") return pid === "p1" ? "Bot 1" : "Bot 2";
    if (mode === "pvb") return pid === "p1" ? "Player" : "Bot";
    return pid === "p1" ? "Player 1" : "Player 2";
  };

  const winnerName = winnerData?.winner === "tie" ? "Draw"
    : winnerData?.winner ? getPlayerName(winnerData.winner)
    : "Draw";

  const handlePlayAgain = async () => {
    try {
      await new Promise((resolve) => {
        const ws = new WebSocket(WS_URL);
        ws.onopen = () => {
          ws.send(JSON.stringify({ action: "reset" }));
        };
        ws.onmessage = (e) => {
          const msg = JSON.parse(e.data);
          if (msg.event === "reset") { ws.close(); resolve(); }
        };
        ws.onerror = () => resolve();
        setTimeout(resolve, 2000);
      });
    } catch (_) {}

    setGameState(prev => ({
      ...prev,
      mode: null,
      minionCount: null,
      myPlayerId: null,
      players: prev.players.map(p => ({
        ...p,
        minionConfigs: [],
        selectedMinions: [],
        strategy: "",
      })),
    }));

    onPlayAgain();
  };

  const handleMainMenu = () => {
    setGameState(prev => ({
      ...prev,
      mode: null,
      minionCount: null,
      myPlayerId: null,
      players: prev.players.map(p => ({
        ...p,
        minionConfigs: [],
        selectedMinions: [],
        strategy: "",
      })),
    }));
    onPlayAgain();
  };

  const p1Data = winnerData?.state?.p1 || winnerData?.p1;
  const p2Data = winnerData?.state?.p2 || winnerData?.p2;

  return (
    <div style={{
      width: "100vw", height: "100vh",
      display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      background: "radial-gradient(ellipse at center, #0f172a 0%, #0a0a1a 100%)",
      fontFamily: "'Cinzel', serif", position: "relative", overflow: "hidden",
    }}>

      {[...Array(20)].map((_, i) => (
        <div key={i} style={{
          position: "absolute", fontSize: 24,
          top: `${Math.random() * 100}%`, left: `${Math.random() * 100}%`,
          animation: `fall ${Math.random() * 4 + 3}s linear ${Math.random() * 2}s infinite`,
          opacity: 0.6,
        }}>
          {["✨","⭐","💫","🌟"][Math.floor(Math.random() * 4)]}
        </div>
      ))}

      <div style={{ fontSize: 80, marginBottom: 24, filter: "drop-shadow(0 0 30px #fbbf24)" }}>🏆</div>

      <div style={{ fontSize: "clamp(14px,2vw,20px)", color: "rgba(255,255,255,0.6)", letterSpacing: "0.3em", marginBottom: 8 }}>
        WINNER
      </div>
      <div style={{ fontSize: "clamp(36px,6vw,80px)", fontWeight: 900, color: "#fbbf24", textShadow: "0 0 40px #f59e0b, 0 0 80px #fbbf24", marginBottom: 16, letterSpacing: "0.05em" }}>
        {winnerName}
      </div>

      <div style={{ display: "flex", gap: 24, marginBottom: 50, flexWrap: "wrap", justifyContent: "center" }}>
        {["p1","p2"].map((pid) => {
          const pData = pid === "p1" ? p1Data : p2Data;
          const isWinner = winnerData?.winner === pid;
          return (
            <div key={pid} style={{
              background: "rgba(255,255,255,0.07)", backdropFilter: "blur(10px)",
              border: `1px solid ${isWinner ? "rgba(251,191,36,0.5)" : "rgba(255,255,255,0.1)"}`,
              borderRadius: 16, padding: "16px 28px", textAlign: "center", minWidth: 140,
            }}>
              <div style={{ color: "rgba(255,255,255,0.5)", fontSize: 12, marginBottom: 6 }}>
                {getPlayerName(pid)}
              </div>
              <div style={{ color: isWinner ? "#fbbf24" : "#fff", fontSize: 18, fontWeight: 700 }}>
                HP: {pData?.hp ?? 0}
              </div>
              <div style={{ color: "#fbbf24", fontSize: 13, marginTop: 4 }}>
                💰 {Math.floor(pData?.budget ?? 0).toLocaleString()}
              </div>
            </div>
          );
        })}
      </div>

      <div style={{ display: "flex", gap: 16 }}>
        <button onClick={handlePlayAgain} style={{
          padding: "14px 48px", fontSize: 18,
          fontFamily: "'Cinzel', serif", fontWeight: 700, letterSpacing: "0.12em",
          background: "linear-gradient(135deg, #7c3aed, #a855f7)",
          border: "none", borderRadius: 40, color: "#fff", cursor: "pointer",
          boxShadow: "0 8px 30px rgba(124,58,237,0.6)", transition: "all 0.25s",
        }}
          onMouseEnter={e => { e.currentTarget.style.transform = "scale(1.06)"; }}
          onMouseLeave={e => { e.currentTarget.style.transform = "none"; }}
        >↺ PLAY AGAIN</button>

        {/* <button onClick={handleMainMenu} style={{
          padding: "14px 36px", fontSize: 16,
          fontFamily: "'Cinzel', serif", fontWeight: 600, letterSpacing: "0.1em",
          background: "rgba(255,255,255,0.08)", backdropFilter: "blur(10px)",
          border: "1px solid rgba(255,255,255,0.25)", borderRadius: 40,
          color: "rgba(255,255,255,0.7)", cursor: "pointer", transition: "all 0.25s",
        }}
          onMouseEnter={e => { e.currentTarget.style.background = "rgba(255,255,255,0.15)"; }}
          onMouseLeave={e => { e.currentTarget.style.background = "rgba(255,255,255,0.08)"; }}
        >Main Menu</button> */}
      </div>

      <style>{`
        @keyframes fall { 0%{transform:translateY(-40px) rotate(0deg);opacity:1} 100%{transform:translateY(110vh) rotate(360deg);opacity:0} }
      `}</style>
    </div>
  );
}