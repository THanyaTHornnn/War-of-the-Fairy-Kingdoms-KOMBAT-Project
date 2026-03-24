import { useState } from "react";
import { useGame } from "../context/GameContext";

const WS_URL = "ws://localhost:8080/ws/game";

export default function SelectPlayerScreen({ onNext, onBack }) {
  const { setGameState } = useGame();
  const [waiting, setWaiting] = useState(false);
  const [assignedId, setAssignedId] = useState(null);

  const handleJoin = () => {
    setWaiting(true);
    const ws = new WebSocket(WS_URL);

    ws.onopen = () => {
      console.log("✅ connected, sending join...");
      ws.send(JSON.stringify({ action: "join" }));
    };
    ws.onmessage = (e) => {
      console.log("📩 received:", e.data);
      const msg = JSON.parse(e.data);
      if (msg.event === "joined") {
        const pid = msg.data.playerId;
        setAssignedId(pid);
        setGameState(prev => ({ ...prev, myPlayerId: pid }));
        ws.close();
        setTimeout(() => {
          if (pid === "p1") onNext("selectMode"); // P1 ไปเลือก mode
          else onNext("game");                     // P2 ไป Game Board รอ
        }, 800);
      }
      if (!msg.ok) {
        setWaiting(false);
      }
    };
    ws.onerror = (e) => {
      console.error("WebSocket error", e);
      setWaiting(false);
      alert("เชื่อมต่อ backend ไม่ได้");
    };
    ws.onclose = (e) => console.log("closed", e.code);
  };

  return (
    <div style={{
      width: "100vw", height: "100vh",
      display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      background: "radial-gradient(ellipse at center, #1e1b4b 0%, #0f0c29 40%, #0a0a1a 100%)",
      fontFamily: "'Cinzel', serif",
    }}>
      <h1 style={{
        color: "#e2d9f3", fontSize: "clamp(24px,3vw,48px)",
        textShadow: "0 0 20px #c4b5fd", marginBottom: 16, letterSpacing: "0.1em",
      }}>JOIN GAME</h1>

      <p style={{ color: "rgba(255,255,255,0.4)", fontSize: 13, marginBottom: 40 }}>
        คนแรกที่เข้าจะเป็น Player 1 · คนที่สองจะเป็น Player 2
      </p>

      {assignedId ? (
        <div style={{ color: assignedId === "p1" ? "#4ade80" : "#f87171", fontSize: 28, fontWeight: 700 }}>
          {assignedId === "p1" ? "🟢 คุณคือ PLAYER 1" : "🔴 คุณคือ PLAYER 2"}
          <div style={{ color: "rgba(255,255,255,0.4)", fontSize: 13, marginTop: 8, textAlign: "center" }}>
            กำลังเข้าเกม...
          </div>
        </div>
      ) : (
        <button onClick={handleJoin} disabled={waiting} style={{
          padding: "16px 60px", fontSize: 18,
          fontFamily: "'Cinzel', serif", fontWeight: 700, letterSpacing: "0.15em",
          background: "rgba(196,181,253,0.2)", border: "1.5px solid rgba(196,181,253,0.6)",
          borderRadius: 40, color: "#c4b5fd",
          cursor: waiting ? "not-allowed" : "pointer",
          opacity: waiting ? 0.6 : 1,
        }}>
          {waiting ? "กำลังเชื่อมต่อ..." : "JOIN →"}
        </button>
      )}

      <button onClick={onBack} style={{
        marginTop: 32, padding: "8px 24px", background: "transparent",
        border: "none", color: "rgba(255,255,255,0.4)",
        cursor: "pointer", fontFamily: "'Cinzel', serif", fontSize: 12,
      }}>← BACK</button>
    </div>
  );
}