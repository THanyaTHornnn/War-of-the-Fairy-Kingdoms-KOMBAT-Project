import { createContext, useContext, useState, useCallback, useRef } from "react";
import { useGameSocket } from "../hooks/useGameSocket";
export const MINIONS = [
  { id: "verdant",  name: "Verdant",  emoji: "🌸", color: "#f9a8d4", defense: 10 },
  { id: "celestia", name: "Celestia", emoji: "💜", color: "#c4b5fd", defense: 14 },
  { id: "ivy",      name: "Ivy",      emoji: "🍀", color: "#86efac", defense: 18 },
  { id: "nyx",      name: "Nyx",      emoji: "🐉", color: "#67e8f9", defense: 8  },
  { id: "mibi",     name: "MiBi",     emoji: "⚔️", color: "#fcd34d", defense: 12 },
];

export const BASE_HP     = 100;
export const BASE_BUDGET = 500;

const GameContext = createContext(null);
const createPlayer = (name) => ({
  name,
  hp:              BASE_HP,
  budget:          BASE_BUDGET,
  strategy:        "",
  minionCounts:    Object.fromEntries(MINIONS.map(m => [m.id, 0])),
  minionDefense:   Object.fromEntries(MINIONS.map(m => [m.id, m.defense])),
  selectedMinions: [],
  minionConfigs:   [],
});

export function GameProvider({ children }) {
  const [gameState, setGameState] = useState({
    mode:        null,
    minionCount: null,
    round:       1,
    currentTurn: 1,
    myPlayerId:  null,   // "p1" | "p2" — ตั้งโดย SelectPlayerScreen
    players:     [createPlayer("Player 1"), createPlayer("Player 2")],
    hexGrid:     [],
    phase:       "setup",
    winner:      null,
  });

  const externalHandlerRef = useRef(null);
  // ฟังก์ชันรับข้อความจาก Server
const handleMessage = useCallback((msg) => {
  console.log("📩 Context Received:", msg);

  if (msg.event === "joined") {
    const newId = msg.data.playerId;
    sessionStorage.setItem("playerId", newId);
    setGameState(prev => {
      if (prev.myPlayerId === newId) return prev;
      return { ...prev, myPlayerId: newId };
    });
  }

  // 2. 📢 กรณีมีคนออกจากเกม (Disconnect)
  if (msg.event === "player_left") {
    const leftId = msg.data.playerId;

    setGameState(prev => {
      // ✅ เพิ่มเงื่อนไข: ต้องมี myPlayerId แล้ว และ ID ที่ออก "ต้องไม่ใช่ตัวเรา"
      // และเราต้องไม่ใช่คนดู (ถ้าอยากให้คนดูไม่โดนแจ้งเตือนกวนใจ)
      const isNotMe = prev.myPlayerId && leftId !== prev.myPlayerId;
      const isImportantPlayer = leftId === "p1" || leftId === "p2";

      if (isNotMe && isImportantPlayer) {
        alert(`ผู้เล่น ${leftId.toUpperCase()} ออกจากเกมแล้ว!`);
      }
      return prev; 
    });
  }

  // 3. กรณีมีการวางยูนิต หรือ อัปเดตสถานะเกมจาก Server
 if (msg.event === "spawned" || msg.event === "state") {
    const serverData = msg.data?.state || msg.data;
    setGameState(prev => ({
      ...prev,
      ...serverData,
      myPlayerId: prev.myPlayerId, // ✅ lock ไว้
      phase: serverData.phase?.toLowerCase() || prev.phase,
    }));
  }

  if (msg.event === "p2_confirmed") {
  const configs = msg.data?.minionConfigs || [];
  setGameState(prev => ({
    ...prev,
    players: prev.players.map((p, i) =>
      i === 0 ? { ...p, minionConfigs: configs } : p
    ),
  }));
}

  if (msg.event === "error") {
    console.error("❌ Server Error:", msg.message);
  }
    // ✅ ส่งต่อให้ handler ภายนอก (GameBoardScreen) ด้วย
  externalHandlerRef.current?.(msg);

}, []); // [] ว่างไว้เพื่อให้ฟังก์ชันนิ่งที่สุด;

  // เรียกใช้ Socket ที่นี่ (เพื่อให้สายไม่หลุดตอนเปลี่ยนหน้า)
  // เป็น: (ส่งแค่ handleMessage)
const { send , getPlayerId} = useGameSocket(handleMessage);

  // ✅ function สำหรับให้หน้าอื่น register handler
const setMessageHandler = useCallback((fn) => {
  externalHandlerRef.current = fn;
}, []);
const updatePlayer = (idx, partial) => {
  setGameState(prev => {
    const players = [...prev.players];
    players[idx] = { ...players[idx], ...partial };
    return { ...prev, players };
  });
};

  // ใส่ send ลงใน value ด้วย เพื่อให้หน้าอื่นๆ (เช่น หน้าบอร์ด) สั่งยิงข้อมูลไปหา Server ได้
  const value = { 
    gameState, 
    setGameState, 
    updatePlayer, 
    send, 
    getPlayerId,
    setMessageHandler,
    MINIONS, 
    BASE_HP, 
    BASE_BUDGET 
  };

  return <GameContext.Provider value={value}>{children}</GameContext.Provider>;
}

export const useGame = () => useContext(GameContext);