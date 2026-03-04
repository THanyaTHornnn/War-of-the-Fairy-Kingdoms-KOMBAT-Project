// import { useState } from "react";
// import { GameProvider } from "./context/GameContext";

// // Pages
// import StartScreen from "./pages/StartScreen";
// import SelectModeScreen from "./pages/SelectModeScreen";
// import SelectMinionCountScreen from "./pages/SelectMinionCountScreen";
// import CollectionScreen from "./pages/CollectionScreen";
// import SetStrategyScreen from "./pages/SetStrategyScreen";
// import GameBoardScreen from "./pages/GameBoardScreen";
// import GameOverScreen from "./pages/GameOverScreen";

// // ============================================================
// // 🔧 EDIT HERE: Flow ของหน้าทั้งหมด
// // เพิ่มหน้าใหม่ได้ที่นี่
// // ============================================================
// const SCREENS = {
//   start:        "start",
//   selectMode:   "selectMode",
//   selectMinion: "selectMinion",
//   collection:   "collection",
//   strategy:     "strategy",
//   game:         "game",
//   gameover:     "gameover",
// };

// function AppContent() {
//   const [screen, setScreen] = useState(SCREENS.start);
//   const [winner, setWinner] = useState(null);

//   const goTo = (s) => setScreen(s);

//   const handleGameEnd = (winnerIdx) => {
//     setWinner(winnerIdx);
//     goTo(SCREENS.gameover);
//   };

//   return (
//     <>
//       {/* 
//         ============================================================
//         🔧 EDIT HERE: ใส่ Google Fonts import ใน index.html
//         <link href="https://fonts.googleapis.com/css2?family=Cinzel:wght@400;700;900&display=swap" rel="stylesheet">
//         ============================================================
//       */}

//       {screen === SCREENS.start && (
//         <StartScreen onNext={goTo} />
//         // ❌ ไม่มีปุ่ม Back บนหน้า START — ตามสเปค
//       )}

//       {screen === SCREENS.selectMode && (
//         <SelectModeScreen
//           onNext={goTo}
//           onBack={() => goTo(SCREENS.start)}
//           // ✅ มีปุ่ม Back → ไป Start
//         />
//       )}

//       {screen === SCREENS.selectMinion && (
//         <SelectMinionCountScreen
//           onNext={goTo}
//           onBack={() => goTo(SCREENS.selectMode)}
//           // ✅ มีปุ่ม Back → ไป SelectMode
//         />
//       )}

//       {screen === SCREENS.collection && (
//         <CollectionScreen
//           onNext={goTo}
//           onBack={() => goTo(SCREENS.selectMinion)}
//           // ✅ มีปุ่ม Back → ไป SelectMinionCount
//         />
//       )}

//       {screen === SCREENS.strategy && (
//         <SetStrategyScreen
//           onNext={goTo}
//           onBack={() => goTo(SCREENS.collection)}
//           // ✅ มีปุ่ม Back → ไป Collection
//         />
//       )}

//       {screen === SCREENS.game && (
//         <GameBoardScreen
//           onGameEnd={handleGameEnd}
//           // ❌ ไม่มีปุ่ม Back บนหน้า Hex — ตามสเปค
//         />
//       )}

//       {screen === SCREENS.gameover && (
//         <GameOverScreen
//           winner={winner}
//           onPlayAgain={() => goTo(SCREENS.start)}
//           // ❌ ไม่มีปุ่ม Back บนหน้า End — ตามสเปค
//         />
//       )}
//     </>
//   );
// }

// export default function App() {
//   return (
//     <GameProvider>
//       <AppContent />
//     </GameProvider>
//   );
// }
import { useState, useEffect } from "react";
import { GameProvider } from "./context/GameContext";

import StartScreen from "./pages/StartScreen";
import SelectModeScreen from "./pages/SelectModeScreen";
import SelectMinionCountScreen from "./pages/SelectMinionCountScreen";
import CollectionScreen from "./pages/CollectionScreen";
import GameBoardScreen from "./pages/GameBoardScreen";
import GameOverScreen from "./pages/GameOverScreen";

function AppContent() {
  const [screen, setScreen] = useState("start");
  const [winner, setWinner] = useState(null);
  useEffect(() => {
  fetch("http://localhost:8080/")
    .then(res => res.text())
    .then(data => console.log("Backend:", data))
    .catch(err => console.log("Error:", err));
}, []);

  const goTo = (s) => setScreen(s);

  return (
    <>
      {screen === "start"        && <StartScreen onNext={goTo} />}
      {screen === "selectMode"   && <SelectModeScreen onNext={goTo} onBack={() => goTo("start")} />}
      {screen === "selectMinion" && <SelectMinionCountScreen onNext={goTo} onBack={() => goTo("selectMode")} />}
      {screen === "collection"   && <CollectionScreen onNext={goTo} onBack={() => goTo("selectMinion")} />}
      {screen === "game"         && <GameBoardScreen onGameEnd={(w) => { setWinner(w); goTo("gameover"); }} />}
      {screen === "gameover"     && <GameOverScreen winner={winner} onPlayAgain={() => goTo("start")} />}
    </>
  );
}

export default function App() {
  return <GameProvider><AppContent /></GameProvider>;
}