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
        backgroundImage: "url('/public/selectCharacter.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
        fontFamily: "'Emilys Candy', serif", gap: 20,
      }}>
        <div style={{ color: "#c4b5fd", fontSize: 45, letterSpacing: 4, textTransform: "uppercase" }}>
          P1's Setup
        </div>
        <h2 style={{ color: "#e2d9f3", fontSize: 40, fontWeight: 900, margin: 0,
          textShadow: "0 0 20px #0d0d0e" }}>
          Confirm this Config To Start The Game 
        </h2>

        {/* Config cards */}
        <div style={{ display: "flex", gap: 12, flexWrap: "wrap", justifyContent: "center", maxWidth: 700 }}>
          {p1Configs.map((cfg, i) => {
            const m = MINIONS.find(x => x.id === cfg.minionId);
            return (
              <div key={i} style={{
                background: "rgba(255, 255, 255, 0.76)", border: "1px solid rgba(60, 59, 65, 0.77)",
                borderRadius: 16, padding: "16px 20px", minWidth: 130, textAlign: "center",
              }}>
                <img
  src={`/images/${cfg.minionId}.png`}
  style={{ width: 60, height: 60, objectFit: "contain" }}
  onError={e => { e.target.style.display = "none"; }}
/>
                <div style={{ color: "#4194a6", fontWeight: 700, marginTop: 6, fontSize: 20 }}>
                  {m?.name || cfg.minionId}
                </div>
                <div style={{ color: "#58481a", fontSize:20, marginTop: 4 }}>DEF {cfg.defense}</div>
               <pre style={{
  color: "#264649",
  fontSize: 15,
  marginTop: 6,
  background: "rgba(0,0,0,0.3)",
  borderRadius: 8,
  padding: "20px 30px",
  fontFamily: "monospace",
  whiteSpace: "pre-wrap",
  wordBreak: "break-word",
  textAlign: "left",
  maxHeight: 120,
  overflowY: "auto",
  lineHeight: 1.6,
  margin: 0,
}}>{cfg.strategy || "done"}</pre>
              </div>
            );
          })}
        </div>

        {/* Buttons */}
        <div style={{ display: "flex", gap: 12, marginTop: 8 }}>
          <button onClick={() => send("confirm-game")} style={{
            padding: "12px 36px", borderRadius: 30, cursor: "pointer",
            border: "1.5px solid rgb(29, 97, 54)",
            background: "rgb(134, 239, 173)", color: "#114f28",
            fontFamily: "'Emilys Candy', serif", fontSize: 20, fontWeight: 700,
          }}> ACCEPT</button>
          <button onClick={() => send("cancel-game")} style={{
            padding: "12px 36px", borderRadius: 30, cursor: "pointer",
            border: "1.5px solid rgb(123, 43, 43)",
            background: "rgb(232, 159, 159)", color: "#7f2727",
            fontFamily: "'Emilys Candy', serif",fontSize: 20,fontWeight: 700,
          }}> REJECT</button>
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
        backgroundImage: "url('/public/Gameroom.jpg')",
        backgroundSize: "cover",
        backgroundPosition: "center",
        fontFamily: "'Emilys Candy', serif",
      }}>
        <div style={{ textAlign: "center" }}>
          <div style={{ fontSize: 52, marginBottom: 16 }}>Rejected</div>
          <div style={{ color: "#fca5a5", fontSize: 20, fontWeight: 700 }}>P2 Rejected Config</div>
          <div style={{ color: "rgba(255,255,255,0.4)", fontSize: 13, marginTop: 8 }}>
            back to setup room...
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
      backgroundImage: "url('/public/selectCharacter.jpg')",
      backgroundSize: "cover",
      backgroundPosition: "center",
      fontFamily: "'Emilys Candy', serif", gap: 20,
    }}>
      <div style={{ fontSize: 52 }}></div>
      <div style={{ color: "#ffffff", fontSize: 60, fontWeight: 700, letterSpacing: 2 }}>
        {myId === "p1" ? "waiting for confirm . . ." : "waiting for P1 to create game . . ."}
      </div>
      <div style={{ color: "rgba(255, 255, 255, 0.78)", fontSize: 30}}>
        {myId === "p1" ? "P2 is watching your config . . . " : "Loading config . . ."}
      </div>
      {gameState.roomCode && (
        <div style={{ color: "rgba(255, 255, 255, 0.78)", fontSize: 30,marginTop: 4 }}>
          Room Code : <span style={{ fontFamily: "monospace", color: "#c4b5fd", fontWeight: 700 }}>
            {gameState.roomCode}
          </span>
        </div>
      )}
      <button onClick={onBack} style={{
        marginTop: 16, padding: "8px 24px", background: "transparent",
        border: "none", color: "rgba(255, 255, 255, 0.87)",
        cursor: "pointer", fontFamily: "'Emilys Candy', serif", fontSize: 30,
      }}>← BACK</button>
    </div>
  );
}