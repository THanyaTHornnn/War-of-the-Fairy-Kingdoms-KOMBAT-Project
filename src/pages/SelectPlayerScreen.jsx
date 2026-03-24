import { useState, useEffect } from "react";
import { useGame } from "../context/GameContext";

export default function SelectPlayerScreen({ onNext, onBack }) {
  const { gameState, send, getPlayerId } = useGame();
  const [waiting, setWaiting] = useState(false);

  useEffect(() => {
    if (gameState.myPlayerId) {
      const timer = setTimeout(() => {
        if (gameState.myPlayerId === "p1") onNext("selectMinion");
        else if (gameState.myPlayerId === "p2") onNext("waitingRoom");
        else onNext("game");
      }, 800);
      return () => clearTimeout(timer);
    }
  }, [gameState.myPlayerId, onNext]);

  // ✅ ตอน mount — เช็คว่า socket assign role ไปแล้วหรือยัง
  useEffect(() => {
    const alreadyId = getPlayerId();
    if (alreadyId && !gameState.myPlayerId) {
      // socket เชื่อมแล้ว มี id แล้ว แต่ context ยังไม่รู้ — sync ให้
      // จะ trigger useEffect ข้างบนเอง
    }
  }, []);

  const handleJoin = () => {
    setWaiting(true);
    // ✅ socket join ไปแล้วตอน onopen อัตโนมัติ
    // แต่ถ้า id ยังไม่มา ให้ส่งซ้ำได้
    const currentId = getPlayerId();
    if (!currentId) {
      send("join", {}); // ส่ง join ซ้ำกรณี socket เปิดก่อนแต่ยังไม่ได้ id
    }
  };

  return (
    <div style={{
      width: "100vw", height: "100vh", display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      background: "radial-gradient(ellipse at center, #1e1b4b 0%, #0f0c29 40%, #0a0a1a 100%)",
      fontFamily: "'Cinzel', serif", gap: 20,
    }}>
      <h1 style={{ color: "#e2d9f3", fontSize: "clamp(24px,3vw,48px)", textShadow: "0 0 20px #c4b5fd" }}>
        JOIN GAME
      </h1>

      {gameState.myPlayerId ? (
        <div style={{ textAlign: "center" }}>
          <div style={{ fontSize: 28, fontWeight: 700, color: gameState.myPlayerId === "p1" ? "#4ade80" : "#f87171" }}>
            {gameState.myPlayerId === "p1" ? "🟢 คุณคือ PLAYER 1" :
             gameState.myPlayerId === "p2" ? "🔴 คุณคือ PLAYER 2" : "⚪ คุณคือ SPECTATOR"}
          </div>
          <div style={{ fontSize: 13, color: "rgba(255,255,255,0.4)", marginTop: 8 }}>
            กำลังเข้าสู่โลกแห่ง Fairy Kingdoms...
          </div>
        </div>
      ) : (
        <button onClick={handleJoin} disabled={waiting} style={{
          padding: "14px 48px", fontSize: 16, fontFamily: "'Cinzel', serif",
          fontWeight: 700, letterSpacing: "0.15em",
          background: waiting ? "rgba(255,255,255,0.05)" : "rgba(196,181,253,0.2)",
          border: "1.5px solid rgba(196,181,253,0.6)",
          borderRadius: 40, color: "#c4b5fd", cursor: waiting ? "not-allowed" : "pointer",
        }}>
          {waiting ? "กำลังร้องขอสิทธิ์..." : "JOIN →"}
        </button>
      )}

      <button onClick={onBack} style={{
        marginTop: 16, padding: "8px 24px", borderRadius: 20,
        border: "1px solid rgba(255,255,255,0.2)", background: "transparent",
        color: "rgba(255,255,255,0.4)", cursor: "pointer", fontFamily: "'Cinzel', serif",
      }}>← BACK</button>
    </div>
  );
}