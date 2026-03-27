import BackButton from "../components/BackButton";
import { useGame } from "../context/GameContext";

const WS_URL = "ws://localhost:8080/ws/game";

const MODES = [
  { id: "pvp", label: "Player VS Player",   desc: "2 Players compete" },
  { id: "pvb", label: "Player VS Bot",    desc: "1 Player vs Bot" },
  { id: "bvb", label: "Bot VS Bot",     desc: "Watch Bots battle" },
];

export default function SelectModeScreen({ onNext, onBack }) {
  const { setGameState } = useGame();

  const handleSelect = (modeId) => {
    setGameState(prev => ({ ...prev, mode: modeId }));
    if (modeId === "pvp") {
      onNext("room"); // ✅ ไปหน้าสร้าง/เข้าห้องก่อน
    } else {
      onNext("selectMinion"); // PVB/BVB ไป setup เลย
    }
  };

  return (
    <div style={{
      width: "100vw", height: "100vh", display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      backgroundImage: "url('/public/backgroundstart.jpg')",
      backgroundSize: "cover",
      backgroundPosition: "center",
      fontFamily: "'Emilys Candy', serif", position: "relative",
    }}>
      <BackButton onClick={onBack} />

      <h1 style={{
        fontSize: "clamp(28px, 4vw, 52px)", fontFamily: "'Emilys Candy', serif",
        color: "#faedfc", textShadow: "0 0 20px #5a2459",
        marginBottom: 50, letterSpacing: "0.1em", fontWeight: 700,
      }}>SELECT GAME MODE</h1>

      <div style={{ display: "flex", gap: 24, flexWrap: "wrap", justifyContent: "center", padding: "0 20px" }}>
        {MODES.map(mode => (
          <button key={mode.id} onClick={() => handleSelect(mode.id)} style={{
            width: "clamp(180px, 22vw, 300px)", height: "clamp(220px, 28vw, 360px)",
            background: "rgba(255,255,255,0.08)", backdropFilter: "blur(16px)",
            border: "1px solid rgba(255,255,255,0.2)", borderRadius: "20px",
            color: "#fff", cursor: "pointer",
            display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center",
            gap: 16, transition: "all 0.3s", fontFamily: "'Emilys Candy', serif",
          }}
            onMouseEnter={e => {
              e.currentTarget.style.background = "rgba(196,181,253,0.2)";
              e.currentTarget.style.transform = "translateY(-8px) scale(1.02)";
              e.currentTarget.style.borderColor = "rgba(196,181,253,0.6)";
              e.currentTarget.style.boxShadow = "0 20px 40px rgba(124,58,237,0.4)";
            }}
            onMouseLeave={e => {
              e.currentTarget.style.background = "rgba(255,255,255,0.08)";
              e.currentTarget.style.transform = "none";
              e.currentTarget.style.borderColor = "rgba(255,255,255,0.2)";
              e.currentTarget.style.boxShadow = "none";
            }}
          >
            <div style={{ fontSize: 100 }}>{mode.icon}</div>
            <div style={{ fontSize: "clamp(20px, 2vw, 40px)", fontWeight: 700, letterSpacing: "0.05em" }}>{mode.label}</div>
            <div style={{ fontSize: 17, opacity: 0.7, fontFamily: "sans-serif" }}>{mode.desc}</div>
          </button>
        ))}
      </div>
    </div>
  );
}