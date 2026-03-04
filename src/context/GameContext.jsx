import { createContext, useContext, useState } from "react";

// ============================================================
// 🔧 EDIT HERE: เพิ่ม/ลด minion types ได้ตรงนี้
// ============================================================
export const MINIONS = [
  { id: "verdant",  name: "Verdant",  emoji: "🌸", color: "#f9a8d4", defense: 10 },
  { id: "celestia", name: "Celestia", emoji: "💜", color: "#c4b5fd", defense: 14 },
  { id: "ivy",      name: "Ivy",      emoji: "🍀", color: "#86efac", defense: 18 },
  { id: "nyx",      name: "Nyx",      emoji: "🐉", color: "#67e8f9", defense: 8  },
  { id: "mibi",     name: "MiBi",     emoji: "⚔️", color: "#fcd34d", defense: 12 },
];

// ============================================================
// 🔧 EDIT HERE: ค่า HP เริ่มต้น (ทุกตัวเท่ากันตามสเปค)
// ============================================================
export const BASE_HP = 100;

// ============================================================
// 🔧 EDIT HERE: Budget เริ่มต้นของแต่ละผู้เล่น
// ============================================================
export const BASE_BUDGET = 500;

const GameContext = createContext(null);

const createPlayer = (name) => ({
  name,
  hp: BASE_HP,
  budget: BASE_BUDGET,
  strategy: "",
  minionCounts: Object.fromEntries(MINIONS.map(m => [m.id, 0])),
  // defense ต่อตัว — ผู้เล่นกรอกเองในหน้า Collection
  minionDefense: Object.fromEntries(MINIONS.map(m => [m.id, m.defense])),
});

export function GameProvider({ children }) {
  // ============================================================
  // 🔧 EDIT HERE: โครงสร้าง gameState ทั้งหมด
  // ============================================================
  const [gameState, setGameState] = useState({
    mode: null,          // "pvp" | "pvb" | "bvb"
    minionCount: null,   // 1-5
    round: 1,
    currentTurn: 1,      // 1 = player1, 2 = player2
    players: [createPlayer("Player 1"), createPlayer("Player 2")],
    hexGrid: [],         // [{id, owner:1|2|null, minion:minionId|null}]
    phase: "setup",      // "setup" | "battle" | "end"
    winner: null,
  });

  const updatePlayer = (idx, partial) => {
    setGameState(prev => {
      const players = [...prev.players];
      players[idx] = { ...players[idx], ...partial };
      return { ...prev, players };
    });
  };

  const value = { gameState, setGameState, updatePlayer, MINIONS, BASE_HP, BASE_BUDGET };
  return <GameContext.Provider value={value}>{children}</GameContext.Provider>;
}

export const useGame = () => useContext(GameContext);
