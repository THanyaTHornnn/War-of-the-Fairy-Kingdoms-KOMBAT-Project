import { useState } from "react";
import { GameProvider, useGame } from "./context/GameContext";
import StartScreen from "./pages/StartScreen";
import SelectModeScreen from "./pages/SelectModeScreen";
import SelectMinionCountScreen from "./pages/SelectMinionCountScreen";
import WaitingRoomScreen from "./pages/WaitingRoomScreen";
import CollectionScreen from "./pages/CollectionScreen";
import GameBoardScreen from "./pages/GameBoardScreen";
import GameOverScreen from "./pages/GameOverScreen";

function AppContent() {
  const [screen, setScreen] = useState("start");
  const [winnerData, setWinnerData] = useState(null);
  const { gameState } = useGame();

  const goTo = (s) => setScreen(s);

  const mode = gameState.mode;
  const myPlayerId = gameState.myPlayerId;

  return (
    <>
      {screen === "start"        && <StartScreen onNext={() => goTo("selectMode")} />}
      {screen === "selectMode"   && <SelectModeScreen onNext={goTo} onBack={() => goTo("start")} />}
      {screen === "selectMinion" && <SelectMinionCountScreen onNext={goTo} onBack={() => goTo("selectMode")} />}
      {screen === "collection"   && <CollectionScreen onNext={goTo} onBack={() => goTo("selectMinion")} />}
      {screen === "waiting"      && <WaitingRoomScreen onNext={goTo} onBack={() => goTo("selectMode")} />}
      {screen === "game"         && <GameBoardScreen onGameEnd={(data) => { setWinnerData(data); goTo("gameover"); }} />}
      {screen === "gameover"     && <GameOverScreen winnerData={winnerData} onPlayAgain={() => goTo("start")} />}
    </>
  );
}

export default function App() {
  return <GameProvider><AppContent /></GameProvider>;
}