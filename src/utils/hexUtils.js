// ============================================================
// hexUtils.js — flat-top hexagons ตรงตามรูปใน PDF
// 8×8 grid, 1-indexed (row 1-8, col 1-8)
// flat-top: odd col เยื้องลง (shift down by half hex height)
// ============================================================

export const GRID_ROWS = 8;
export const GRID_COLS = 8;
export const HEX_SIZE  = 36;

// Zone เริ่มต้นตาม PDF:
// P1 (เขียว บนซ้าย):  (1,1)(1,2)(1,3)(2,1)(2,2)
// P2 (แดง ล่างขวา): (7,6)(7,7)(7,8)(8,7)(8,8)
export const P1_ZONE = new Set(['1-1','1-2','1-3','2-1','2-2']);
export const P2_ZONE = new Set(['7-7','7-8','8-6','8-7','8-8']);

export function generateHexGrid() {
  const hexes = [];
  for (let r = 1; r <= GRID_ROWS; r++) {
    for (let c = 1; c <= GRID_COLS; c++) {
      const key = `${r}-${c}`;
      hexes.push({
        id:     key,
        row:    r,
        col:    c,
        zone:   P1_ZONE.has(key) ? 1 : P2_ZONE.has(key) ? 2 : 0,
        minion: null, // { player:1|2, type:'verdant'|..., hp:number }
      });
    }
  }
  return hexes;
}

// flat-top hex: odd col เยื้องลง HH = (√3/2)*S
export function hexToPixel(row, col, size = HEX_SIZE) {
  const HH = Math.sqrt(3) / 2 * size;
  const x  = (col - 1) * 1.5 * size + size;
  const y  = (row - 1) * 2 * HH + HH + (col % 2 !== 0 ? HH : 0);
  return { x, y };
}

// flat-top polygon points: vertex angles 0°,60°,120°...
export function hexPoints(cx, cy, size = HEX_SIZE) {
  const pts = [];
  for (let i = 0; i < 6; i++) {
    const angle = (Math.PI / 180) * (60 * i);
    pts.push(`${cx + size * Math.cos(angle)},${cy + size * Math.sin(angle)}`);
  }
  return pts.join(' ');
}

// Neighbors (flat-top offset grid)
export function getNeighbors(row, col, gridMap) {
  // directions คำนวณจาก pixel distance จริง (flat-top, odd col เยื้องลง)
  const isOdd = col % 2 !== 0;
  const dirs = isOdd
    ? [ [-1,0],[0,-1],[0,+1],[+1,-1],[+1,0],[+1,+1] ]
    : [ [-1,-1],[-1,0],[0,-1],[0,+1],[+1,0],[+1,-1] ];
  return dirs
    .map(([dr,dc]) => `${row+dr}-${col+dc}`)
    .filter(k => gridMap[k]);
}

export function isAdjacentToZone(row, col, playerNum, gridMap) {
  return getNeighbors(row, col, gridMap)
    .some(k => gridMap[k]?.zone === playerNum);
}

