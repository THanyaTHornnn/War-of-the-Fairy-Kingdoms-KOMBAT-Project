import BackButton from "../components/BackButton";
import { useGame } from "../context/GameContext";

// ============================================================
// 🔧 EDIT HERE: เพิ่ม/ลด game modes ตรงนี้
// ============================================================
const MODES = [
  { id: "pvp", label: "Player VS Player", icon: "", desc: "2 ผู้เล่นแข่งกัน" },
  { id: "pvb", label: "Player VS Bot",    icon: "", desc: "1 ผู้เล่น vs AI" },
  { id: "bvb", label: "Bot VS Bot",       icon: "", desc: "ดู AI แข่งกัน" },
];

export default function SelectModeScreen({ onNext, onBack }) {
  const { setGameState } = useGame();

  const handleSelect = (modeId) => {
    // ============================================================
    // 🔧 EDIT HERE: ถ้าต้องส่ง mode ไป backend → เรียก API ตรงนี้
    // await api.post('/game/setMode', { mode: modeId })
    // ============================================================
    setGameState(prev => ({ ...prev, mode: modeId }));
    onNext("selectMinion");
  };

  return (
    <div style={{
      width: "100vw",
      height: "100vh",
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      // 🖼️ EDIT HERE: backgroundImage: "url('/bg/main.jpg')"
      // background: "radial-gradient(ellipse at center, #1e1b4b 0%, #0f0c29 40%, #0a0a1a 100%)",
      backgroundImage: "url('/public/me.jpg')",
      backgroundSize: "cover",
      backgroundPosition: "center",
      fontFamily: "'Emilys Candy', serif",
      position: "relative",
    }}>
      <BackButton onClick={onBack} />

      {/* Title */}
      <h1 style={{
        fontSize: "clamp(28px, 4vw, 52px)",
        fontFamily: "'Emilys Candy', serif",
        color: "#e2d9f3",
        textShadow: "0 0 20px #c4b5fd",
        marginBottom: 50,
        letterSpacing: "0.1em",
        fontWeight: 700,
      }}>
        SELECT GAME MODE
      </h1>

      {/* Mode cards */}
      <div style={{
        display: "flex",
        gap: 24,
        flexWrap: "wrap",
        justifyContent: "center",
        padding: "0 20px",
      }}>
        {MODES.map(mode => (
          <button
            key={mode.id}
            onClick={() => handleSelect(mode.id)}
            style={{
              // ============================================================
              // 🎨 EDIT HERE: ขนาด/สไตล์ card ของแต่ละ mode
              // ============================================================
              width: "clamp(180px, 22vw, 300px)",
              height: "clamp(220px, 28vw, 360px)",
              background: "rgba(255,255,255,0.08)",
              backdropFilter: "blur(16px)",
              border: "1px solid rgba(255,255,255,0.2)",
              borderRadius: "20px",
              color: "#fff",
              cursor: "pointer",
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              justifyContent: "center",
              gap: 16,
              transition: "all 0.3s",
              fontFamily: "'Emilys Candy', serif",
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
            <div style={{ fontSize: 48 }}>{mode.icon}</div>
            <div style={{ fontSize: "clamp(16px, 2vw, 24px)", fontWeight: 700, letterSpacing: "0.05em" }}>
              {mode.label}
            </div>
            <div style={{ fontSize: 13, opacity: 0.7, fontFamily: "sans-serif" }}>
              {mode.desc}
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}
