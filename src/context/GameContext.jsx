import { createContext, useContext, useState } from "react";

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