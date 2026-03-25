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
    myPlayerId:  null,
    roomCode:    null,
    players:     [createPlayer("Player 1"), createPlayer("Player 2")],
    hexGrid:     [],
    phase:       "setup",
    winner:      null,
  });

  // External handler — ให้แต่ละหน้า register เพื่อรับ message เพิ่มเติม
  const externalHandlerRef = useRef(null);

  const setMessageHandler = useCallback((fn) => {
    externalHandlerRef.current = fn;
  }, []);

  // ── Central message handler ───────────────────────────────────────────────
  const handleMessage = useCallback((msg) => {
    console.log("📩 Context received:", msg.event, msg);

    // ── Room events ──
    if (msg.event === "room_created" || msg.event === "room_joined") {
      const pid  = msg.data?.playerId;
      const code = msg.data?.roomCode;
      setGameState(prev => ({
        ...prev,
        ...(pid  ? { myPlayerId: pid  } : {}),
        ...(code ? { roomCode:   code } : {}),
      }));
      console.log(`🏠 ${msg.event}: playerId=${msg.data?.playerId} roomCode=${msg.data?.roomCode}`);
    }

    // ── P1 ขยับขึ้นมาแทน (P1 ออกไป P2 กลายเป็น P1) ──
    if (msg.event === "joined") {
      const pid = msg.data?.playerId;
      if (pid) {
        setGameState(prev => ({ ...prev, myPlayerId: pid }));
      }
    }

    // ── Game state updates ──
    if (msg.event === "spawned" || msg.event === "state") {
      const serverData = msg.data?.state || msg.data;
      setGameState(prev => ({
        ...prev,
        ...serverData,
        myPlayerId: prev.myPlayerId, // ✅ lock — ห้ามให้ server ทับ
        roomCode:   prev.roomCode,   // ✅ lock
        phase: serverData.phase?.toLowerCase() || prev.phase,
      }));
    }

    // ── P1 configs ส่งให้ P2 ──
    if (msg.event === "configs") {
      const configs = msg.data?.minionConfigs || [];
      setGameState(prev => {
        const players = [...prev.players];
        players[0] = { ...players[0], minionConfigs: configs, selectedMinions: configs.map(c => c.minionId) };
        return { ...prev, players };
      });
    }

    // ── player_left ──
    if (msg.event === "player_left") {
      console.log("👋 Player left:", msg.data?.playerId);
    }

    // ── forward ให้ external handler (RoomScreen, WaitingRoomScreen, GameBoardScreen) ──
    externalHandlerRef.current?.(msg);
  }, []);

  // ── Single WebSocket ──────────────────────────────────────────────────────
  const { send, getPlayerId, getRoomCode, setRoomCode } = useGameSocket(handleMessage);

  // ── updatePlayer helper ───────────────────────────────────────────────────
  const updatePlayer = useCallback((idx, partial) => {
    setGameState(prev => {
      const players = [...prev.players];
      players[idx] = { ...players[idx], ...partial };
      return { ...prev, players };
    });
  }, []);

  const value = {
    gameState,
    setGameState,
    updatePlayer,
    send,
    getPlayerId,
    getRoomCode,
    setRoomCode,
    setMessageHandler,
    MINIONS,
    BASE_HP,
    BASE_BUDGET,
  };

  return <GameContext.Provider value={value}>{children}</GameContext.Provider>;
}

export const useGame = () => useContext(GameContext);