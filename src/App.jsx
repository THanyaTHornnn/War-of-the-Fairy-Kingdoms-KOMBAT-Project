import { useState } from "react";
import { GameProvider } from "./context/GameContext";
import StartScreen from "./pages/StartScreen";
import SelectModeScreen from "./pages/SelectModeScreen";
import SelectMinionCountScreen from "./pages/SelectMinionCountScreen";
import SelectPlayerScreen from "./pages/SelectPlayerScreen";
import CollectionScreen from "./pages/CollectionScreen";
import GameBoardScreen from "./pages/GameBoardScreen";
import GameOverScreen from "./pages/GameOverScreen";

function AppContent() {
  const [screen, setScreen] = useState("start");
  const [winnerData, setWinnerData] = useState(null);
  const goTo = (s) => setScreen(s);

  return (
    <>
      {screen === "start"        && <StartScreen onNext={() => goTo("selectPlayer")} />}
      {screen === "selectPlayer" && <SelectPlayerScreen onNext={goTo} onBack={() => goTo("start")} />}
      {screen === "selectMinion" && <SelectMinionCountScreen onNext={goTo} onBack={() => goTo("start")} />}
      {screen === "collection"   && <CollectionScreen onNext={goTo} onBack={() => goTo("selectMinion")} />}
      {screen === "game"         && <GameBoardScreen onGameEnd={(data) => { setWinnerData(data); goTo("gameover"); }} />}
      {screen === "gameover"     && <GameOverScreen winnerData={winnerData} onPlayAgain={() => goTo("start")} />}
    </>
  );
}

export default function App() {
  return <GameProvider><AppContent /></GameProvider>;
}