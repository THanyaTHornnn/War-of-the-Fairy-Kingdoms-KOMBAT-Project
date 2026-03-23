
import { useState } from "react";
import BackButton from "../components/BackButton";
import { useGame, MINIONS } from "../context/GameContext";
import MinionCharacter from "../components/MinionCharacter";

const MAX_COUNT = 5;

function getMinionsForCard(count) {
  return MINIONS.slice(0, count);
}

export default function SelectMinionCountScreen({ onNext, onBack }) {
  const [selected, setSelected] = useState(null);
  const { setGameState } = useGame();
const handleConfirm = () => {
  if (!selected) return;
  setGameState(prev => ({ ...prev, minionCount: selected }));
  onNext("collection"); // ← ไปหน้า JOIN ก่อน
};

  return (
    <div style={{
      width: "100vw", height: "100vh",
      display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "flex-start",
      paddingBottom: "20vh",
      paddingTop: "6vh",
      background: "radial-gradient(ellipse at center, #1e1b4b 0%, #0f0c29 40%, #0a0a1a 100%)",
      // 🖼️ ใส่รูปพื้นหลัง: backgroundImage: "url('/bg/main.jpg')", backgroundSize: "cover",
      fontFamily: "'Cinzel', serif",  // ← 🔤 เปลี่ยน font ทั้งหน้าตรงนี้
      position: "relative", overflow: "hidden",
    }}>
      <BackButton onClick={onBack} />

      <h1 style={{
        fontSize: "clamp(22px, 3.5vw, 48px)",
        fontFamily: "'Emilys Candy', serif",
        color: "#e2d9f3",
        textShadow: "0 0 20px #c4b5fd",
        marginBottom: "5vh",
        letterSpacing: "0.08em",
        fontWeight: 700,
        textAlign: "center",
        // 🔤 เปลี่ยน font เฉพาะ title: fontFamily: "'MyFont', serif"
      }}>
        SELECT NUMBER OF MINION
      </h1>

      {/* Cards */}
      <div style={{
        display: "flex", gap: "clamp(8px, 1.5vw, 20px)",
        alignItems: "flex-end", justifyContent: "center",
        padding: "0 20px", flex: 1, paddingBottom: "10vh", paddingTop: "6vh",
      }}>
        {Array.from({ length: MAX_COUNT }, (_, i) => i + 1).map(count => (
          <div key={count} onClick={() => setSelected(count)} style={{
            width: "clamp(110px, 15vw, 190px)",
            height: "clamp(260px, 35vw, 400px)",
            background: selected === count ? "rgba(196,181,253,0.25)" : "rgba(255,255,255,0.07)",
            backdropFilter: "blur(14px)",
            border: selected === count ? "2px solid rgba(196,181,253,0.8)" : "1px solid rgba(255,255,255,0.15)",
            borderRadius: 18, cursor: "pointer",
            display: "flex", flexDirection: "column",
            alignItems: "center", justifyContent: "center",
            padding: "16px 8px", gap: 4, transition: "all 0.3s",
            transform: selected === count ? "translateY(-12px)" : "none",
            boxShadow: selected === count ? "0 20px 40px rgba(196,181,253,0.3)" : "none",
          }}
            onMouseEnter={e => { if (selected!==count) { e.currentTarget.style.background="rgba(255,255,255,0.12)"; e.currentTarget.style.transform="translateY(-6px)"; }}}
            onMouseLeave={e => { if (selected!==count) { e.currentTarget.style.background="rgba(255,255,255,0.07)"; e.currentTarget.style.transform="none"; }}}
          >
            <div style={{ display:"flex", flexWrap:"wrap", justifyContent:"center", gap:4, flex:1, alignItems:"center" }}>
              {getMinionsForCard(count).map(m => (
                <MinionCharacter key={m.id} minionId={m.id} size={count===1?70:count<=3?55:45} spin={false} />
              ))}
            </div>
            <div style={{
              fontSize: "clamp(28px, 4vw, 48px)",
              color: selected === count ? "#c4b5fd" : "rgba(255,255,255,0.6)",
              fontWeight: 900, marginTop: 8,
            }}>{count}</div>
          </div>
        ))}
      </div>

      {/* CONFIRM button — แสดงเมื่อเลือกแล้ว */}
      <div style={{ height: "2vh", display:"flex", alignItems:"center", justifyContent:"center" }}>
        {selected && (
          <button onClick={handleConfirm} style={{
            padding: "12px 48px", fontSize: 15,
            fontFamily: "'Emilys Candy', serif",  // 🔤 เปลี่ยน font ปุ่มตรงนี้
            fontWeight: 700, letterSpacing: "0.15em",
            background: "rgba(196,181,253,0.2)", backdropFilter: "blur(10px)",
            border: "1.5px solid rgba(196,181,253,0.6)",
            borderRadius: 40, color: "#c4b5fd", cursor: "pointer",
            transition: "all 0.25s",
            animation: "fadeIn 0.3s ease",
          }}
            onMouseEnter={e => { e.currentTarget.style.background="rgba(196,181,253,0.35)"; e.currentTarget.style.transform="scale(1.05)"; }}
            onMouseLeave={e => { e.currentTarget.style.background="rgba(196,181,253,0.2)"; e.currentTarget.style.transform="none"; }}
          >
            CONFIRM {selected} MINION →
          </button>
        )}
      </div>
      <div style={{ height: "3vh" }} />
    </div>
  );
}
