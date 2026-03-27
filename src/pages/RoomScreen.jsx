import { useState, useEffect } from "react";
import BackButton from "../components/BackButton";
import { useGame } from "../context/GameContext";

export default function RoomScreen({ onNext, onBack }) {
  const { send, gameState, setMessageHandler } = useGame();

  const [tab,       setTab]       = useState("create");
  const [roomCode,  setRoomCode]  = useState("");
  const [inputCode, setInputCode] = useState("");
  const [status,    setStatus]    = useState("idle"); // "idle" | "waiting" | "error"
  const [errorMsg,  setErrorMsg]  = useState("");

  // ── Register handler (ต้องใช้ useEffect ไม่ใช่ useState!) ─────────────────
 useEffect(() => {
  setMessageHandler((msg) => {

    if (msg.event === "room_created") {
      setRoomCode(msg.data.roomCode);
      setStatus("waiting");
      // ✅ ไม่ไปไหนก่อน รอ P2 เข้าห้อง
    }

    // ✅ P2 เข้าห้องแล้ว → P1 ได้รับ player_joined → ไป setup
    if (msg.event === "player_joined" && gameState.myPlayerId === "p1") {
      onNext("selectMinion");
    }

    // P2 / spectator เข้าห้องสำเร็จ → route ตาม role
    if (msg.event === "room_joined") {
      const pid = msg.data.playerId;
      if (pid === "p2")                               onNext("waitingRoom");
      else if (pid !== "p1")                          onNext("game"); // spectator
      // p1 ไม่ต้องทำอะไรตรงนี้ — รอ player_joined แทน
    }

    if (msg.event === "room_error") {
      setErrorMsg(msg.data?.message || "เกิดข้อผิดพลาด");
      setStatus("error");
    }
  });

  return () => setMessageHandler(null);
}, [setMessageHandler, gameState.myPlayerId, onNext]);

  const handleCreate = () => {
    setStatus("waiting");
    send("create-room", { mode: gameState.mode?.toUpperCase() || "PVP" });
  };

  const handleJoin = () => {
    if (!inputCode.trim()) return;
    setErrorMsg("");
    send("join-room", { roomCode: inputCode.trim().toUpperCase() });
  };

  // ─────────────────────────────────────────────────────────────────────────
  return (
    <div style={{
      width: "100vw", height: "100vh", display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      backgroundImage: "url('/public/Gameroom.jpg')",
      backgroundSize: "cover",
      backgroundPosition: "center",
      fontFamily: "'Emilys Candy', serif", position: "relative", gap: 24,
    }}>
      <BackButton onClick={onBack} />

      <h1 style={{
        color: "#e2d9f3", fontSize: "clamp(80px,3vw,42px)",
        textShadow: "0 0 20px #2b2155", letterSpacing: "0.1em", margin: 0,
      }}>
        GAME ROOM
      </h1>

      {/* Tab switcher */}
      <div style={{ display: "flex", gap: 8 }}>
        {["create", "join"].map(t => (
          <button key={t} onClick={() => { setTab(t); setStatus("idle"); setErrorMsg(""); }} style={{
            padding: "8px 28px", borderRadius: 30, cursor: "pointer",
            fontFamily: "'Emilys Candy', serif", fontSize: 25, fontWeight: 700,
            border:      tab === t ? "1.5px solid #c4b5fd" : "1px solid rgba(255,255,255,0.2)",
            background:  tab === t ? "rgb(219, 184, 227)" : "rgba(255,255,255,0.05)",
            color:       tab === t ? "#5b3b90" : "rgba(255,255,255,0.5)",
            transition: "all 0.2s",
          }}>
            {t === "create" ? "Create Room" : "Join Room"}
          </button>
        ))}
      </div>

      <div style={{
        background: "rgb(219, 184, 227)", backdropFilter: "blur(16px)",
        border: "1px solid rgba(244, 231, 231, 0.6)", borderRadius: 24,
        padding: "32px 40px", minWidth: 320, textAlign: "center",
      }}>

        {/* ── CREATE ── */}
        {tab === "create" && status === "idle" && (
          <>
            <div style={{ color: "rgb(77, 28, 101)", fontSize: 40, marginBottom: 20 }}>
              Create A New Room And Share The Code For Friends To Join
            </div>
            <button onClick={handleCreate} style={{
              padding: "13px 44px", borderRadius: 30, cursor: "pointer",
              fontFamily: "'Emilys Candy', serif", fontSize: 20, fontWeight: 700,
              border: "1.5px solid rgba(150, 134, 216, 0.6)",
              background: "rgb(115, 72, 125)", color: "#e0aaf2",
            }}>
              CREATE ROOM →
            </button>
          </>
        )}

        {tab === "create" && status === "waiting" && (
          <>
            <div style={{ color: "rgb(109, 51, 153)", fontSize: 25, marginBottom: 12 }}>
              Your Room Code
            </div>
            <div style={{
              fontSize: 48, fontWeight: 900, letterSpacing: 12,
              color: "rgb(77, 28, 101)", textShadow: "0 0 20px #aa9eda",
              background: "rgb(223, 216, 250)", border: "1.5px solid rgb(77, 28, 101)",
              borderRadius: 16, padding: "16px 32px", marginBottom: 20,
              fontFamily: "monospace",
            }}>
              {roomCode || "..."}
            </div>
            <div style={{ color: "rgb(109, 51, 153)", fontSize: 25 }}>
              Wait Player 2 To Join...
            </div>
            <div style={{ color: "rgb(109, 51, 153)", fontSize: 25, marginTop: 8 }}>
              Share This Code With Friends To Invite Them To Join
            </div>
          </>
        )}

        {/* ── JOIN ── */}
        {tab === "join" && (
          <>
            <div style={{ color: "rgba(255,255,255,0.5)", fontSize: 13, marginBottom: 16 }}>
              กรอกรหัสห้องที่ได้รับ
            </div>
            <input
              value={inputCode}
              onChange={e => { setInputCode(e.target.value.toUpperCase()); setErrorMsg(""); }}
              placeholder="1234"
              maxLength={4}
              style={{
                width: "100%", padding: "12px", borderRadius: 12, textAlign: "center",
                border: "1px solid rgba(196,181,253,0.4)",
                background: "rgba(196,181,253,0.08)", color: "#fff",
                fontSize: 36, fontWeight: 900, fontFamily: "monospace",
                letterSpacing: 12, outline: "none", boxSizing: "border-box", marginBottom: 16,
              }}
            />
            {errorMsg && (
              <div style={{ color: "#fca5a5", fontSize: 12, marginBottom: 12 }}> {errorMsg}</div>
            )}
            <button
              onClick={handleJoin}
              disabled={inputCode.trim().length < 4}
              style={{
                padding: "13px 44px", borderRadius: 30, cursor: "pointer",
                fontFamily: "'Cinzel', serif", fontSize: 15, fontWeight: 700,
                border: "1.5px solid rgba(134,239,172,0.6)",
                background: "rgba(134,239,172,0.15)", color: "#86efac",
                opacity: inputCode.trim().length === 4 ? 1 : 0.4,
              }}
            >
              JOIN →
            </button>
          </>
        )}
      </div>
    </div>
  );
}