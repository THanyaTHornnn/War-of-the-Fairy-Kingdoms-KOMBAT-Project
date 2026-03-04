import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";

// ============================================================
// 🔧 EDIT HERE: Global CSS resets ใส่ที่นี่
// ============================================================
const globalStyle = document.createElement("style");
globalStyle.innerHTML = `
  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    background: #0a0a1a;
    overflow: hidden;
    font-family: 'Cinzel', serif;
  }
  button { cursor: pointer; }
  textarea, input { caret-color: #c4b5fd; }

  /* Scrollbar */
  ::-webkit-scrollbar { width: 4px; }
  ::-webkit-scrollbar-track { background: rgba(255,255,255,0.05); }
  ::-webkit-scrollbar-thumb { background: rgba(196,181,253,0.4); border-radius: 2px; }

  /* Keyframes used globally */
  @keyframes pulse {
    0%, 100% { opacity: 0.5; transform: scale(1); }
    50% { opacity: 1; transform: scale(1.08); }
  }
  @keyframes twinkle { 0%,100%{opacity:0.3} 50%{opacity:1} }
  @keyframes float {
    0%,100%{transform:translateX(-50%) translateY(0)}
    50%{transform:translateX(-50%) translateY(-12px)}
  }
`;
document.head.appendChild(globalStyle);

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
