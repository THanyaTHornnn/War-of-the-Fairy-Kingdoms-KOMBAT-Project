import { useEffect, useState } from "react";
import { useGame, MINIONS } from "../context/GameContext";

export default function WaitingRoomScreen({ onNext, onBack }) {
  const { gameState, send, setMessageHandler, getPlayerId } = useGame();
  const [status, setStatus] = useState("waiting"); // "waiting" | "confirm" | "rejected"
  const myId = getPlayerId() || gameState.myPlayerId;
  const minionConfigs = gameState.players[0]?.minionConfigs || [];

  useEffect(() => {
    setMessageHandler((msg) => {
      // P2 ได้รับ event "created" → แสดง popup ยืนยัน
      if (msg.event === "created" && myId === "p2") {
        setStatus("confirm");
      }
      // ทุกคนได้รับ "p2_confirmed" → เข้าเกม
      if (msg.event === "p2_confirmed") {
        onNext("game");
      }
      // P1 ได้รับ "game_cancelled" → กลับไป setup ใหม่
      if (msg.event === "game_cancelled") {
        setStatus("rejected");
        setTimeout(() => onBack(), 2000);
      }
    });
    return () => setMessageHandler(null);
  }, [myId, setMessageHandler, onNext, onBack]);

  // ── UI สำหรับ P2: ยืนยัน config ──
  if (myId === "p2" && status === "confirm") {
    return (
      <div style={{
        width:"100vw", height:"100vh", display:"flex", flexDirection:"column",
        alignItems:"center", justifyContent:"center",
        background:"radial-gradient(ellipse at center, #1e1b4b 0%, #0f0c29 40%, #0a0a1a 100%)",
        fontFamily:"'Cinzel', serif", gap: 20,
      }}>
        <div style={{ color:"#c4b5fd", fontSize:14, letterSpacing:3 }}>P1's SETUP</div>
        <h2 style={{ color:"#e2d9f3", fontSize:28, fontWeight:900, textShadow:"0 0 20px #c4b5fd" }}>
          ยืนยัน Config นี้?
        </h2>

        {/* แสดง configs ของ P1 */}
        <div style={{ display:"flex", gap:12, flexWrap:"wrap", justifyContent:"center" }}>
          {minionConfigs.map((cfg, i) => {
            const m = MINIONS.find(x => x.id === cfg.minionId);
            return (
              <div key={i} style={{
                background:"rgba(255,255,255,0.08)", border:"1px solid rgba(196,181,253,0.3)",
                borderRadius:16, padding:"16px 20px", minWidth:140, textAlign:"center",
              }}>
                <div style={{ fontSize:36 }}>{m?.emoji}</div>
                <div style={{ color:"#e2d9f3", fontWeight:700, marginTop:6 }}>{m?.name}</div>
                <div style={{ color:"#fbbf24", fontSize:11, marginTop:4 }}>DEF {cfg.defense}</div>
                <div style={{
                  color:"#a5f3fc", fontSize:10, marginTop:6,
                  background:"rgba(0,0,0,0.3)", borderRadius:8, padding:"4px 8px",
                  fontFamily:"monospace", maxWidth:160, wordBreak:"break-all",
                }}>{cfg.strategy || "done"}</div>
              </div>
            );
          })}
        </div>

        <div style={{ display:"flex", gap:12, marginTop:16 }}>
          <button onClick={() => send("confirm", {})} style={{
            padding:"12px 36px", borderRadius:30, cursor:"pointer",
            border:"1.5px solid rgba(134,239,172,0.6)",
            background:"rgba(134,239,172,0.15)", color:"#86efac",
            fontFamily:"'Cinzel', serif", fontSize:14, fontWeight:700,
          }}>✓ ACCEPT</button>
          <button onClick={() => send("cancel", {})} style={{
            padding:"12px 36px", borderRadius:30, cursor:"pointer",
            border:"1.5px solid rgba(239,68,68,0.5)",
            background:"rgba(239,68,68,0.1)", color:"#fca5a5",
            fontFamily:"'Cinzel', serif", fontSize:14, fontWeight:700,
          }}>✕ REJECT</button>
        </div>
      </div>
    );
  }

  // ── UI สำหรับ P1: กำลังรอ ──
  if (status === "rejected") {
    return (
      <div style={{ width:"100vw", height:"100vh", display:"flex", alignItems:"center", justifyContent:"center",
        background:"radial-gradient(ellipse at center, #1e1b4b 0%, #0a0a1a 100%)", fontFamily:"'Cinzel', serif" }}>
        <div style={{ textAlign:"center" }}>
          <div style={{ fontSize:48, marginBottom:16 }}>❌</div>
          <div style={{ color:"#fca5a5", fontSize:20, fontWeight:700 }}>P2 ปฏิเสธ Config</div>
          <div style={{ color:"rgba(255,255,255,0.4)", fontSize:13, marginTop:8 }}>กลับไป setup ใหม่...</div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ width:"100vw", height:"100vh", display:"flex", flexDirection:"column",
      alignItems:"center", justifyContent:"center",
      background:"radial-gradient(ellipse at center, #1e1b4b 0%, #0a0a1a 100%)",
      fontFamily:"'Cinzel', serif", gap:20 }}>
      <div style={{ fontSize:48 }}>⏳</div>
      <div style={{ color:"#e2d9f3", fontSize:22, fontWeight:700, letterSpacing:2 }}>
        {myId === "p1" ? "รอ P2 ยืนยัน..." : "รอ P1 สร้างเกม..."}
      </div>
      <div style={{ color:"rgba(255,255,255,0.4)", fontSize:13 }}>
        {myId === "p1" ? "P2 กำลังดู config ของคุณ" : "กำลังโหลด config..."}
      </div>
    </div>
  );
}