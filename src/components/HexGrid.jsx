// import { useState } from "react";
// import { hexToPixel, hexPoints, HEX_SIZE, GRID_ROWS, GRID_COLS, isAdjacentToZone } from "../utils/hexUtils";
// import { MINIONS } from "../context/GameContext";

// const ZONE_STYLE = {
//   1:     { fill: 'rgba(134,239,172,0.35)', stroke: '#4ade80' },
//   2:     { fill: 'rgba(252,165,165,0.35)', stroke: '#f87171' },
//   0:     { fill: 'rgba(255,255,255,0.04)', stroke: 'rgba(180,180,200,0.25)' },
//   sel:   { fill: 'rgba(239,68,68,0.4)',    stroke: '#ef4444' },
//   valid: { fill: 'rgba(250,204,21,0.22)',  stroke: 'rgba(250,204,21,0.85)' },
// };

// const MINION_EMOJI = { verdant:'🌸', celestia:'💜', ivy:'🍀', nyx:'🐉', mibi:'⚔️' };
// // P1=เขียว P2=แดง — HP สองฝั่งคนละสีเสมอ
// const HP_COLOR = ['#4ade80', '#f87171'];

// export default function HexGrid({ hexes, onHexClick, selectedHex, mode, currentTurn, onSpawnHex }) {
//   const S  = HEX_SIZE;
//   const HH = Math.sqrt(3) / 2 * S;
//   const LABEL = 24;

//   const gridMap = {};
//   hexes.forEach(h => { gridMap[h.id] = h; });

//   // bounding box — ให้ hex พอดีใน SVG viewBox จริงๆ
//   const allPixels = hexes.map(h => hexToPixel(h.row, h.col, S));
//   const minX = Math.min(...allPixels.map(p => p.x)) - S;
//   const minY = Math.min(...allPixels.map(p => p.y)) - HH;
//   const maxX = Math.max(...allPixels.map(p => p.x)) + S;
//   const maxY = Math.max(...allPixels.map(p => p.y)) + HH;
//   const vbX  = minX - LABEL - 2;
//   const vbY  = minY - LABEL - 2;
//   const vbW  = maxX - minX + LABEL*2 + 4;
//   const vbH  = maxY - minY + LABEL*2 + 4;

//   return (
//     <svg
//       viewBox={`${vbX} ${vbY} ${vbW} ${vbH}`}
//       style={{ width: '100%', height: '100%', display: 'block' }}
//       preserveAspectRatio="xMidYMid meet"
//     >
//       {/* Col labels */}
//       {Array.from({ length: GRID_COLS }, (_, i) => i + 1).map(c => {
//         const { x } = hexToPixel(1, c, S);
//         return (
//           <text key={`cl-${c}`} x={x} y={minY - LABEL*0.55}
//             textAnchor="middle" dominantBaseline="middle"
//             fontSize={10} fill="rgba(255,255,255,0.45)" fontFamily="sans-serif">{c}</text>
//         );
//       })}

//       {/* Row labels */}
//       {Array.from({ length: GRID_ROWS }, (_, i) => i + 1).map(r => {
//         const { y } = hexToPixel(r, 1, S);
//         return (
//           <text key={`rl-${r}`} x={minX - LABEL*0.6} y={y}
//             textAnchor="middle" dominantBaseline="middle"
//             fontSize={10} fill="rgba(255,255,255,0.45)" fontFamily="sans-serif">{r}</text>
//         );
//       })}

//       {/* Hexes */}
//       {hexes.map(hex => {
//         const { x, y } = hexToPixel(hex.row, hex.col, S);
//         // ใช้ S*0.97 ให้ชิดกันพอดี ไม่มีช่องว่างเกิน
//         const pts = hexPoints(x, y, S * 0.97);
//         const isSel = selectedHex === hex.id;

//         let isValid = false;
//         if (mode === 'hex'   && hex.zone === 0 && isAdjacentToZone(hex.row, hex.col, currentTurn, gridMap)) isValid = true;
//         if (mode === 'spawn' && hex.zone === currentTurn && !hex.minion) isValid = true;

//         const st = isSel ? ZONE_STYLE.sel : isValid ? ZONE_STYLE.valid : ZONE_STYLE[hex.zone];

//         // ถ้า mode=spawn และเป็น zone ของ turn ให้คลิกเพื่อเปิด spawn panel
//         const handleClick = () => {
//           if (mode === 'spawn' && hex.zone === currentTurn && !hex.minion) {
//             onSpawnHex?.(hex);
//           } else {
//             onHexClick?.(hex);
//           }
//         };

//         return (
//           <g key={hex.id} onClick={handleClick} style={{ cursor: 'pointer' }}>
//             <polygon points={pts} fill={st.fill} stroke={st.stroke}
//               strokeWidth={isSel ? 2.5 : 1.5} />
//             {hex.minion && (
//               <>
//                 <text x={x} y={y + 4} textAnchor="middle" dominantBaseline="middle"
//                   fontSize={S * 0.46} style={{ pointerEvents:'none', userSelect:'none' }}>
//                   {MINION_EMOJI[hex.minion.type] || '?'}
//                 </text>
//                 <text x={x} y={y + S * 0.68} textAnchor="middle" dominantBaseline="middle"
//                   fontSize={8} fill={HP_COLOR[hex.minion.player - 1]} fontWeight="bold"
//                   style={{ pointerEvents:'none' }}>
//                   {hex.minion.hp}hp
//                 </text>
//               </>
//             )}
//           </g>
//         );
//       })}
//     </svg>
//   );
// }



// import { hexToPixel, hexPoints, HEX_SIZE, GRID_ROWS, GRID_COLS, isAdjacentToZone } from "../utils/hexUtils";

// const ZONE_STYLE = {
//   1:     { fill: 'rgba(134,239,172,0.35)', stroke: '#4ade80' },
//   2:     { fill: 'rgba(252,165,165,0.35)', stroke: '#f87171' },
//   0:     { fill: 'rgba(255,255,255,0.04)', stroke: 'rgba(180,180,200,0.25)' },
//   sel:   { fill: 'rgba(239,68,68,0.4)',    stroke: '#ef4444' },
//   valid: { fill: 'rgba(250,204,21,0.22)',  stroke: 'rgba(250,204,21,0.85)' },
// };

// // backend ส่ง type เป็น "Minionverdant" → แปลงก่อน lookup
// const MINION_EMOJI = {
//   verdant:  '🌸',
//   celestia: '💜',
//   ivy:      '🍀',
//   nyx:      '🐉',
//   mibi:     '⚔️',
// };

// const HP_COLOR = ['#4ade80', '#f87171'];

// // แปลง type จาก backend → emoji key
// function getMinionEmoji(type) {
//   if (!type) return '?';
//   // "Minionverdant" → "verdant", "verdant" → "verdant"
//   const key = type.replace(/^Minion/i, '').toLowerCase();
//   return MINION_EMOJI[key] || '?';
// }

// export default function HexGrid({ hexes, onHexClick, selectedHex, mode, currentTurn, onSpawnHex }) {
//   const S  = HEX_SIZE;
//   const HH = Math.sqrt(3) / 2 * S;
//   const LABEL = 24;

//   const gridMap = {};
//   hexes.forEach(h => { gridMap[h.id] = h; });

//   const allPixels = hexes.map(h => hexToPixel(h.row, h.col, S));
//   const minX = Math.min(...allPixels.map(p => p.x)) - S;
//   const minY = Math.min(...allPixels.map(p => p.y)) - HH;
//   const maxX = Math.max(...allPixels.map(p => p.x)) + S;
//   const maxY = Math.max(...allPixels.map(p => p.y)) + HH;
//   const vbX  = minX - LABEL - 2;
//   const vbY  = minY - LABEL - 2;
//   const vbW  = maxX - minX + LABEL*2 + 4;
//   const vbH  = maxY - minY + LABEL*2 + 4;

//   return (
//     <svg viewBox={`${vbX} ${vbY} ${vbW} ${vbH}`}
//       style={{ width:'100%', height:'100%', display:'block' }}
//       preserveAspectRatio="xMidYMid meet">

//       {/* Col labels */}
//       {Array.from({ length: GRID_COLS }, (_, i) => i+1).map(c => {
//         const { x } = hexToPixel(1, c, S);
//         return <text key={`cl-${c}`} x={x} y={minY - LABEL*0.55}
//           textAnchor="middle" dominantBaseline="middle"
//           fontSize={10} fill="rgba(255,255,255,0.45)" fontFamily="sans-serif">{c}</text>;
//       })}

//       {/* Row labels */}
//       {Array.from({ length: GRID_ROWS }, (_, i) => i+1).map(r => {
//         const { y } = hexToPixel(r, 1, S);
//         return <text key={`rl-${r}`} x={minX - LABEL*0.6} y={y}
//           textAnchor="middle" dominantBaseline="middle"
//           fontSize={10} fill="rgba(255,255,255,0.45)" fontFamily="sans-serif">{r}</text>;
//       })}

//       {/* Hexes */}
//       {hexes.map(hex => {
//         const { x, y } = hexToPixel(hex.row, hex.col, S);
//         const pts  = hexPoints(x, y, S * 0.97);
//         const isSel = selectedHex === hex.id;

//         let isValid = false;
//         if (mode === 'hex'   && hex.zone === 0 && isAdjacentToZone(hex.row, hex.col, currentTurn, gridMap)) isValid = true;
//         if (mode === 'spawn' && hex.zone === currentTurn && !hex.minion) isValid = true;

//         const st = isSel ? ZONE_STYLE.sel : isValid ? ZONE_STYLE.valid : ZONE_STYLE[hex.zone];

//         const handleClick = () => {
//           if (mode === 'spawn' && hex.zone === currentTurn && !hex.minion) {
//             onSpawnHex?.(hex);
//           } else {
//             onHexClick?.(hex);
//           }
//         };

//         return (
//           <g key={hex.id} onClick={handleClick} style={{ cursor:'pointer' }}>
//             <polygon points={pts} fill={st.fill} stroke={st.stroke}
//               strokeWidth={isSel ? 2.5 : 1.5} />
//             {hex.minion && (
//               <>
//                 <text x={x} y={y+4} textAnchor="middle" dominantBaseline="middle"
//                   fontSize={S * 0.46} style={{ pointerEvents:'none', userSelect:'none' }}>
//                   {getMinionEmoji(hex.minion.type)}
//                 </text>
//                 <text x={x} y={y + S*0.68} textAnchor="middle" dominantBaseline="middle"
//                   fontSize={8} fill={HP_COLOR[hex.minion.player - 1]} fontWeight="bold"
//                   style={{ pointerEvents:'none' }}>
//                   {hex.minion.hp}hp
//                 </text>
//               </>
//             )}
//           </g>
//         );
//       })}
//     </svg>
//   );
// }

import { useState } from "react";
import { hexToPixel, hexPoints, HEX_SIZE, GRID_ROWS, GRID_COLS, isAdjacentToZone } from "../utils/hexUtils";
import { MINIONS } from "../context/GameContext";

const ZONE_STYLE = {
1:     { fill: 'rgba(134,239,172,0.35)', stroke: '#4ade80' },
2:     { fill: 'rgba(252,165,165,0.35)', stroke: '#f87171' },
0:     { fill: 'rgba(255,255,255,0.04)', stroke: 'rgba(180,180,200,0.25)' },
sel:   { fill: 'rgba(239,68,68,0.4)',    stroke: '#ef4444' },
valid: { fill: 'rgba(250,204,21,0.22)',  stroke: 'rgba(250,204,21,0.85)' },
};

// แก้: backend ส่ง type เป็น “Minionverdant” → ต้อง strip “Minion” ออกก่อน
const MINION_EMOJI = { verdant:'🌸', celestia:'💜', ivy:'🍀', nyx:'🐉', mibi:'⚔️' };
const HP_COLOR = ['#4ade80', '#f87171'];

function getMinionEmoji(type) {
if (!type) return '?';
const key = type.replace(/^Minion/i, '').toLowerCase();
return MINION_EMOJI[key] || '❓';
}

export default function HexGrid({ hexes, onHexClick, selectedHex, mode, currentTurn, onSpawnHex }) {
const S  = HEX_SIZE;
const HH = Math.sqrt(3) / 2 * S;
const LABEL = 24;

const gridMap = {};
hexes.forEach(h => { gridMap[h.id] = h; });

const allPixels = hexes.map(h => hexToPixel(h.row, h.col, S));
const minX = Math.min(...allPixels.map(p => p.x)) - S;
const minY = Math.min(...allPixels.map(p => p.y)) - HH;
const maxX = Math.max(...allPixels.map(p => p.x)) + S;
const maxY = Math.max(...allPixels.map(p => p.y)) + HH;
const vbX  = minX - LABEL - 2;
const vbY  = minY - LABEL - 2;
const vbW  = maxX - minX + LABEL*2 + 4;
const vbH  = maxY - minY + LABEL*2 + 4;

return (
<svg viewBox={`${vbX} ${vbY} ${vbW} ${vbH}`}
style={{ width:'100%', height:'100%', display:'block' }}
preserveAspectRatio="xMidYMid meet">

  {Array.from({ length: GRID_COLS }, (_, i) => i+1).map(c => {
    const { x } = hexToPixel(1, c, S);
    return <text key={`cl-${c}`} x={x} y={minY - LABEL*0.55}
      textAnchor="middle" dominantBaseline="middle"
      fontSize={10} fill="rgba(255,255,255,0.45)" fontFamily="sans-serif">{c}</text>;
  })}

  {Array.from({ length: GRID_ROWS }, (_, i) => i+1).map(r => {
    const { y } = hexToPixel(r, 1, S);
    return <text key={`rl-${r}`} x={minX - LABEL*0.6} y={y}
      textAnchor="middle" dominantBaseline="middle"
      fontSize={10} fill="rgba(255,255,255,0.45)" fontFamily="sans-serif">{r}</text>;
  })}

  {hexes.map(hex => {
    const { x, y } = hexToPixel(hex.row, hex.col, S);
    const pts   = hexPoints(x, y, S * 0.97);
    const isSel = selectedHex === hex.id;

    let isValid = false;
    if (mode === 'hex'   && hex.zone === 0 && isAdjacentToZone(hex.row, hex.col, currentTurn, gridMap)) isValid = true;
    if (mode === 'spawn' && hex.zone === currentTurn && !hex.minion) isValid = true;

    const st = isSel ? ZONE_STYLE.sel : isValid ? ZONE_STYLE.valid : ZONE_STYLE[hex.zone];

    const handleClick = () => {
      if (mode === 'spawn' && hex.zone === currentTurn && !hex.minion) {
        onSpawnHex?.(hex);
      } else {
        onHexClick?.(hex);
      }
    };

    return (
      <g key={hex.id} onClick={handleClick} style={{ cursor:'pointer' }}>
        <polygon points={pts} fill={st.fill} stroke={st.stroke}
          strokeWidth={isSel ? 2.5 : 1.5} />
        {hex.minion && (
          <>
            <text x={x} y={y+4} textAnchor="middle" dominantBaseline="middle"
              fontSize={S * 0.46} style={{ pointerEvents:'none', userSelect:'none' }}>
              {getMinionEmoji(hex.minion.type)}
            </text>
            <text x={x} y={y + S*0.68} textAnchor="middle" dominantBaseline="middle"
              fontSize={8} fill={HP_COLOR[hex.minion.player - 1]} fontWeight="bold"
              style={{ pointerEvents:'none' }}>
              {hex.minion.hp}hp
            </text>
          </>
        )}
      </g>
    );
  })}
</svg>

);
}