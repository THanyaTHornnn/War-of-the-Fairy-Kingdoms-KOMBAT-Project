import { useGame } from "../context/GameContext";

// ============================================================
// 🖼️ EDIT HERE: เปลี่ยน background image → ใส่ path รูปจริงแทน gradient
// style={{ backgroundImage: "url('/assets/bg-main.jpg')" }}
// ============================================================

export default function StartScreen({ onNext }) {
  const { setGameState } = useGame();

  const handleStart = () => {
    // Reset game state เมื่อเริ่มใหม่
    setGameState(prev => ({ ...prev, phase: "setup", winner: null, round: 1 }));
    onNext("selectMode");
  };

  return (
    <div style={{
      width: "100vw",
      height: "100vh",
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      // ============================================================
      // 🖼️ EDIT HERE: แทน backgroundImage ด้วยรูปจาก Figma
      // backgroundImage: "url('/bg/main.jpg')"
      // ============================================================
     // background: "radial-gradient(ellipse at center, #1e1b4b 0%, #0f0c29 40%, #0a0a1a 100%)",
     backgroundImage: "url('/public/me.jpg')",
     backgroundSize: "cover",
     backgroundPosition: "center",
     // position: "relative",
      //overflow: "hidden",
    //  fontFamily: "'Cinzel', serif",
    }}>

      {/* Decorative stars */}
      {[...Array(40)].map((_, i) => (
        <div key={i} style={{
          position: "absolute",
          width: Math.random() * 3 + 1,
          height: Math.random() * 3 + 1,
          background: "#fff",
          borderRadius: "50%",
          top: `${Math.random() * 100}%`,
          left: `${Math.random() * 100}%`,
          opacity: Math.random() * 0.8 + 0.2,
          animation: `twinkle ${Math.random() * 3 + 2}s ease-in-out infinite`,
        }} />
      ))}

     
      {/* Title */}
      <div style={{ textAlign: "center", zIndex: 10, marginTop: -100
       }}>
        {/* ============================================================
            🔧 EDIT HERE: ฟอนต์ Title — ต้องโหลด Cinzel จาก Google Fonts
            เพิ่มใน index.html: <link href="https://fonts.googleapis.com/css2?family=Cinzel:wght@700;900&display=swap" rel="stylesheet">
            ============================================================ */}
        <h1 style={{
          fontSize: "clamp(50px, 10vw, 120px)",
          fontFamily: "Mountains of Christmas, serif",
          color: "#E8EAC6",
          textShadow: "0 0 30px #CAAA7F, 0 0 60px #EEEDA4",
          margin: 0,
          lineHeight: 1.1,
          letterSpacing: "0.08em",
          fontWeight: 900,
        }}>
          KOMBAT
        </h1>
        <h2 style={{
          fontSize: "clamp(20px, 3vw, 80px)",
          fontFamily: "Mountains of Christmas, serif",
          color: "#fbe0fb",
          textShadow: "0 0 20px #ffd3fe",
          margin: "10px 0",
          fontWeight: 700,
          letterSpacing: "0.12em",
        }}>
          WAR OF THE FAIRY KINGDOMS
        </h2>
       
      </div>

      {/* START Button */}
      <button
        onClick={handleStart}
        style={{
          marginTop: 60,
          padding: "16px 80px",
          fontSize: 22,
          fontFamily: "'Emilys Candy', serif",
          fontWeight: 700,
          letterSpacing: "0.2em",
          // ============================================================
          // 🎨 EDIT HERE: สี/สไตล์ปุ่ม START
          // ============================================================
          background: "rgba(255,255,255,0.12)",
          backdropFilter: "blur(10px)",
          border: "1px solid rgba(255,255,255,0.35)",
          borderRadius: "50px",
          color: "#fff",
          cursor: "pointer",
          boxShadow: "0 8px 32px rgba(124,58,237,0.4)",
          transition: "all 0.25s",
          zIndex: 10,
        }}
        onMouseEnter={e => {
          e.currentTarget.style.background = "rgba(255,255,255,0.22)";
          e.currentTarget.style.transform = "scale(1.05)";
          e.currentTarget.style.boxShadow = "0 8px 40px rgba(196,181,253,0.6)";
        }}
        onMouseLeave={e => {
          e.currentTarget.style.background = "rgba(255,255,255,0.12)";
          e.currentTarget.style.transform = "scale(1)";
          e.currentTarget.style.boxShadow = "0 8px 32px rgba(124,58,237,0.4)";
        }}
      >
        START
      </button>

      <style>{`
        @keyframes twinkle { 0%,100%{opacity:0.3} 50%{opacity:1} }
        @keyframes float { 0%,100%{transform:translateX(-50%) translateY(0)} 50%{transform:translateX(-50%) translateY(-12px)} }
        @keyframes pulse { 0%,100%{opacity:0.5;transform:scale(1)} 50%{opacity:1;transform:scale(1.05)} }
      `}</style>
    </div>
  );
}
