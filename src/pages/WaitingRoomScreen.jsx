import { useState, useEffect } from "react";
import { useGame, MINIONS } from "../context/GameContext";

export default function WaitingRoomScreen({ onNext, onBack }) {
  const { gameState, updatePlayer, send, setMessageHandler } = useGame();

  const myId = gameState.myPlayerId || "p2";
  const [status,    setStatus]    = useState("waiting");
  const [p1Configs, setP1Configs] = useState(gameState.players[0]?.minionConfigs || []);

  // ── Register message handler ───────────────────────────────────────────────
  useEffect(() => {
  setMessageHandler((msg) => {

    if (msg.event === "configs") {
      const configs = msg.data?.minionConfigs || [];
      // ✅ ตรวจสอบว่า configs มีข้อมูลจริงก่อนค่อยขึ้น confirm
      if (configs.length === 0) return;

      setP1Configs(configs);
      updatePlayer(0, {
        minionConfigs:   configs,
        selectedMinions: configs.map(c => c.minionId),
      });
      if (myId === "p2") setStatus("confirm-game");
    }

    // ❌ ลบ case "created" ออก — ไม่ต้อง send("get-configs") เอง
    // server จะ push "configs" มาให้เองตอน P1 กด GAME START

    if (msg.event === "p2_confirmed") onNext("game");

    if (msg.event === "game_cancelled") {
      setStatus("rejected");
      setTimeout(() => onBack(), 2500);
    }
  });

  return () => setMessageHandler(null);
}, [myId, setMessageHandler, send, updatePlayer, onNext, onBack]);
 

  // ─────────────────────────────────────────────────────────────────────────
  // P2: ยืนยัน config
  if (myId === "p2" && status === "confirm-game") {
    return (
      <div style={{
        width: "100vw", height: "100vh", display: "flex", flexDirection: "column",
        alignItems: "center", justifyContent: "center",
        background: "radial-gradient(ellipse at center, #1e1b4b 0%, #0f0c29 40%, #0a0a1a 100%)",
        fontFamily: "'Cinzel', serif", gap: 20,
      }}>
        <div style={{ color: "#c4b5fd", fontSize: 12, letterSpacing: 4, textTransform: "uppercase" }}>
          P1's Setup
        </div>
        <h2 style={{ color: "#e2d9f3", fontSize: 26, fontWeight: 900, margin: 0,
          textShadow: "0 0 20px #c4b5fd" }}>
          ยืนยัน Config นี้?
        </h2>

        {/* Config cards */}
        <div style={{ display: "flex", gap: 12, flexWrap: "wrap", justifyContent: "center", maxWidth: 700 }}>
          {p1Configs.map((cfg, i) => {
            const m = MINIONS.find(x => x.id === cfg.minionId);
            return (
              <div key={i} style={{
                background: "rgba(255,255,255,0.08)", border: "1px solid rgba(196,181,253,0.3)",
                borderRadius: 16, padding: "16px 20px", minWidth: 130, textAlign: "center",
              }}>
                <div style={{ fontSize: 34 }}>{m?.emoji || "❓"}</div>
                <div style={{ color: "#e2d9f3", fontWeight: 700, marginTop: 6, fontSize: 14 }}>
                  {m?.name || cfg.minionId}
                </div>
                <div style={{ color: "#fbbf24", fontSize: 11, marginTop: 4 }}>DEF {cfg.defense}</div>
                <div style={{
                  color: "#a5f3fc", fontSize: 9, marginTop: 6,
                  background: "rgba(0,0,0,0.3)", borderRadius: 8, padding: "3px 6px",
                  fontFamily: "monospace", wordBreak: "break-all",
                }}>{cfg.strategy || "done"}</div>
              </div>
            );
          })}
        </div>

        {/* Buttons */}
        <div style={{ display: "flex", gap: 12, marginTop: 8 }}>
          <button onClick={() => send("confirm-game")} style={{
            padding: "12px 36px", borderRadius: 30, cursor: "pointer",
            border: "1.5px solid rgba(134,239,172,0.6)",
            background: "rgba(134,239,172,0.15)", color: "#86efac",
            fontFamily: "'Cinzel', serif", fontSize: 14, fontWeight: 700,
          }}>✓ ACCEPT</button>
          <button onClick={() => send("cancel-game")} style={{
            padding: "12px 36px", borderRadius: 30, cursor: "pointer",
            border: "1.5px solid rgba(239,68,68,0.5)",
            background: "rgba(239,68,68,0.1)", color: "#fca5a5",
            fontFamily: "'Cinzel', serif", fontSize: 14, fontWeight: 700,
          }}>✕ REJECT</button>
        </div>
      </div>
    );
  }

  // Rejected screen
  if (status === "rejected") {
    return (
      <div style={{
        width: "100vw", height: "100vh", display: "flex",
        alignItems: "center", justifyContent: "center",
        background: "radial-gradient(ellipse at center, #1e1b4b 0%, #0a0a1a 100%)",
        fontFamily: "'Cinzel', serif",
      }}>
        <div style={{ textAlign: "center" }}>
          <div style={{ fontSize: 52, marginBottom: 16 }}>❌</div>
          <div style={{ color: "#fca5a5", fontSize: 20, fontWeight: 700 }}>P2 ปฏิเสธ Config</div>
          <div style={{ color: "rgba(255,255,255,0.4)", fontSize: 13, marginTop: 8 }}>
            กลับไป setup ใหม่...
          </div>
        </div>
      </div>
    );
  }

  // Waiting screen (P1 รอ P2 / P2 รอ configs)
  return (
    <div style={{
      width: "100vw", height: "100vh", display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      background: "radial-gradient(ellipse at center, #1e1b4b 0%, #0a0a1a 100%)",
      fontFamily: "'Cinzel', serif", gap: 20,
    }}>
      <div style={{ fontSize: 52 }}>⏳</div>
      <div style={{ color: "#e2d9f3", fontSize: 22, fontWeight: 700, letterSpacing: 2 }}>
        {myId === "p1" ? "รอ P2 ยืนยัน..." : "รอ P1 สร้างเกม..."}
      </div>
      <div style={{ color: "rgba(255,255,255,0.4)", fontSize: 13 }}>
        {myId === "p1" ? "P2 กำลังดู config ของคุณ" : "กำลังโหลด config..."}
      </div>
      {gameState.roomCode && (
        <div style={{ color: "rgba(196,181,253,0.5)", fontSize: 12, marginTop: 4 }}>
          ห้อง: <span style={{ fontFamily: "monospace", color: "#c4b5fd", fontWeight: 700 }}>
            {gameState.roomCode}
          </span>
        </div>
      )}
      <button onClick={onBack} style={{
        marginTop: 16, padding: "8px 24px", background: "transparent",
        border: "none", color: "rgba(255,255,255,0.35)",
        cursor: "pointer", fontFamily: "'Cinzel', serif", fontSize: 12,
      }}>← BACK</button>
    </div>
  );
}