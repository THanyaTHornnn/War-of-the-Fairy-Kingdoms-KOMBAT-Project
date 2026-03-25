import { useState } from "react";
import { GameProvider, useGame } from "./context/GameContext";
import StartScreen            from "./pages/StartScreen";
import SelectModeScreen       from "./pages/SelectModeScreen";
import SelectMinionCountScreen from "./pages/SelectMinionCountScreen";
import CollectionScreen       from "./pages/CollectionScreen";
import RoomScreen             from "./pages/RoomScreen";
import WaitingRoomScreen      from "./pages/WaitingRoomScreen";
import GameBoardScreen        from "./pages/GameBoardScreen";
import GameOverScreen         from "./pages/GameOverScreen";

function AppContent() {
  const [screen,     setScreen]     = useState("start");
  const [winnerData, setWinnerData] = useState(null);
  const { gameState } = useGame();

  const goTo = (s) => setScreen(s);

  return (
    <>
      {/* ── Lobby ── */}
      {screen === "start"      && <StartScreen      onNext={() => goTo("selectMode")} />}
      {screen === "selectMode" && <SelectModeScreen  onNext={goTo} onBack={() => goTo("start")} />}

      {/* ── PVP only: room → waitingRoom ── */}
      {screen === "room"        && <RoomScreen         onNext={goTo} onBack={() => goTo("selectMode")} />}
      {screen === "waitingRoom" && <WaitingRoomScreen  onNext={goTo} onBack={() => goTo("room")} />}

      {/* ── Setup (P1 in PVP, or anyone in PVB/BVB) ── */}
      {screen === "selectMinion" && <SelectMinionCountScreen onNext={goTo} onBack={() => {
        // PVP: กลับไป room — PVB/BVB: กลับไป selectMode
        goTo(gameState.mode === "pvp" ? "room" : "selectMode");
      }} />}
      {screen === "collection" && <CollectionScreen onNext={goTo} onBack={() => goTo("selectMinion")} />}

      {/* ── Game ── */}
      {screen === "game"     && <GameBoardScreen onGameEnd={(data) => { setWinnerData(data); goTo("gameover"); }} />}
      {screen === "gameover" && <GameOverScreen  winnerData={winnerData} onPlayAgain={() => goTo("start")} />}
    </>
  );
}

export default function App() {
  return (
    <GameProvider>
      <AppContent />
    </GameProvider>
  );
}