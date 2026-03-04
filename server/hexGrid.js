/**
 * ============================================================
 *  KOMBAT Game - hexGrid.js
 *  Person 1: Game State + Hex Board Utilities
 * ============================================================
 *  ระบบจัดการสถานะเกมและกริดฐานสิบหก (Hexagonal Grid)
 *  - ระบบพิกัด offset column (คอลัมน์คู่เลื่อนขึ้น)
 *  - 6 ทิศทาง: up, upright, downright, down, downleft, upleft
 *  - ฟังก์ชันสแกนทิศทาง opponent, ally, nearby
 *  - จัดการ spawn zone ของผู้เล่นทั้งสอง
 * ============================================================
 */

// ---- ค่าคงที่ทิศทาง (Direction Constants) ----
// ทิศทางทั้ง 6 ตามสเปค: 1=up, 2=upright, 3=downright, 4=down, 5=downleft, 6=upleft
const DIR_NAMES = ['up', 'upright', 'downright', 'down', 'downleft', 'upleft'];
const DIR_MAP = { up: 1, upright: 2, downright: 3, down: 4, downleft: 5, upleft: 6 };

/**
 * คำนวณ delta (การเปลี่ยนแปลงพิกัด) ของแต่ละทิศทาง
 * ระบบ offset column: คอลัมน์คู่ (even) เลื่อนขึ้นครึ่งช่อง
 * @param {number} dir - ทิศทาง 1-6
 * @param {number} col - คอลัมน์ปัจจุบัน (ใช้ตรวจว่าเป็นคู่หรือคี่)
 * @returns {[number, number]} [deltaRow, deltaCol]
 */
function dirDelta(dir, col) {
  const even = col % 2 === 0;
  // สำหรับ offset hex grid:
  // คอลัมน์คี่ (odd): upright = (r-1, c+1), downright = (r, c+1)
  // คอลัมน์คู่ (even): upright = (r, c+1), downright = (r+1, c+1)
  const deltas = {
    1: [-1, 0],  // up
    4: [1, 0],   // down
    2: even ? [0, 1] : [-1, 1],   // upright
    3: even ? [1, 1] : [0, 1],    // downright
    5: even ? [1, -1] : [0, -1],  // downleft
    6: even ? [0, -1] : [-1, -1], // upleft
  };
  return deltas[dir] || [0, 0];
}

/**
 * ตรวจว่าพิกัดอยู่ในกริด 8×8 หรือไม่ (1-indexed)
 */
function isValid(r, c) {
  return r >= 1 && r <= 8 && c >= 1 && c <= 8;
}

/**
 * สร้าง hex key จากพิกัด (ใช้เป็น key ของ object)
 */
function hk(r, c) {
  return `${r},${c}`;
}

/**
 * parse hex key กลับเป็นพิกัด [row, col]
 */
function parseHk(key) {
  const [r, c] = key.split(',').map(Number);
  return [r, c];
}

/**
 * หาเพื่อนบ้านในทิศทางที่กำหนด
 * @returns {[number, number]|null} พิกัดเพื่อนบ้าน หรือ null ถ้านอกกริด
 */
function getNeighbor(r, c, dir) {
  const [dr, dc] = dirDelta(dir, c);
  const nr = r + dr;
  const nc = c + dc;
  return isValid(nr, nc) ? [nr, nc] : null;
}

/**
 * หาเพื่อนบ้านทั้ง 6 ทิศของ hex
 * @returns {Array<[number, number]>} รายการพิกัดเพื่อนบ้านที่อยู่ในกริด
 */
function getAdjacentHexes(r, c) {
  const adj = [];
  for (let d = 1; d <= 6; d++) {
    const n = getNeighbor(r, c, d);
    if (n) adj.push(n);
  }
  return adj;
}

// ---- Spawn Zones ----
// P1 spawn zone: 5 ช่องมุมบนซ้าย
const P1_SPAWN = [[1,1], [1,2], [1,3], [2,1], [2,2]];
// P2 spawn zone: 5 ช่องมุมล่างขวา
const P2_SPAWN = [[7,7], [7,8], [8,6], [8,7], [8,8]];

/**
 * ============================================================
 *  ฟังก์ชัน Info Expression สำหรับ Evaluator
 *  ใช้ในภาษาสคริปต์ของ minion
 * ============================================================
 */

/**
 * สแกนทุกทิศทางหา minion ที่ใกล้ที่สุด (opponent หรือ ally)
 * คืนค่า: distance × 10 + direction (1-6)
 * ถ้าไม่เจอคืน 0
 * 
 * @param {number} r - row ของ minion ที่กำลังสแกน
 * @param {number} c - col ของ minion ที่กำลังสแกน
 * @param {object} grid - grid object { hexKey → minion }
 * @param {number} playerIdx - index ผู้เล่นของ minion ที่สแกน
 * @param {string} type - 'opponent' หรือ 'ally'
 * @returns {number} distance×10 + direction, หรือ 0
 */
function scanForMinion(r, c, grid, playerIdx, type) {
  let bestDist = Infinity;
  let bestDir = 0;

  for (let dir = 1; dir <= 6; dir++) {
    let cr = r, cc = c;
    let dist = 0;
    while (true) {
      const nb = getNeighbor(cr, cc, dir);
      if (!nb) break;
      cr = nb[0];
      cc = nb[1];
      dist++;
      const m = grid[hk(cr, cc)];
      if (m) {
        const isOpponent = (type === 'opponent') ? (m.pi !== playerIdx) : (m.pi === playerIdx);
        if (isOpponent && dist < bestDist) {
          bestDist = dist;
          bestDir = dir;
        }
        break; // หยุดที่ minion แรกที่เจอในทิศนี้
      }
    }
  }
  return bestDist === Infinity ? 0 : bestDist * 10 + bestDir;
}

/**
 * สแกนทิศทางเดียวสำหรับ nearby <dir>
 * คืนค่า: 100×(HP digit) + 10×(defense digit) + distance
 * ค่าเป็นลบถ้าเป็น ally, บวกถ้าเป็น opponent
 * ถ้าไม่เจอคืน 0
 * 
 * HP digit: HP÷10 (ปัดลง, max 9)
 * Defense digit: defense (max 9)
 * Distance: ระยะทาง hex
 */
function nearbyDir(r, c, dir, grid, playerIdx) {
  let cr = r, cc = c;
  let dist = 0;
  while (true) {
    const nb = getNeighbor(cr, cc, dir);
    if (!nb) break;
    cr = nb[0];
    cc = nb[1];
    dist++;
    const m = grid[hk(cr, cc)];
    if (m) {
      const hpDigit = Math.min(9, Math.floor(m.hp / 10));
      const defDigit = Math.min(9, m.def);
      const val = 100 * hpDigit + 10 * defDigit + dist;
      // ลบถ้าเป็น ally, บวกถ้าเป็น opponent
      return m.pi === playerIdx ? -val : val;
    }
  }
  return 0;
}

/**
 * ============================================================
 *  สร้าง Game State เริ่มต้น
 * ============================================================
 */
function createInitialGameState(cfg, kinds) {
  return {
    cfg: { ...cfg },
    kinds: kinds, // [{name, defense, ast, script}]
    ps: [
      // Player 1
      {
        budget: cfg.init_budget,
        sc: 0,           // spawn count
        tc: 0,           // turn count
        mids: [],        // minion IDs
        gv: {},          // global variables (ตัวพิมพ์ใหญ่)
        sh: new Set(P1_SPAWN.map(([r,c]) => hk(r,c))), // spawn hexes
      },
      // Player 2
      {
        budget: cfg.init_budget,
        sc: 0,
        tc: 0,
        mids: [],
        gv: {},
        sh: new Set(P2_SPAWN.map(([r,c]) => hk(r,c))),
      },
    ],
    grid: {},     // hexKey → minion reference
    mins: {},     // minionId → minion object
    nid: 1,       // next minion ID
    turn: 0,      // เทิร์นปัจจุบัน (รวมทั้งสองผู้เล่น)
    cp: 0,        // current player index (0 or 1)
    over: false,
    winner: null,  // 'P1', 'P2', 'draw', null
    log: [],
  };
}

/**
 * Deep clone game state (รักษา Set และ AST)
 */
function cloneGameState(gs) {
  const c = JSON.parse(JSON.stringify(gs, (key, val) => {
    if (val instanceof Set) return { __set: [...val] };
    return val;
  }));
  // restore Sets
  for (const p of c.ps) {
    if (p.sh && p.sh.__set) p.sh = new Set(p.sh.__set);
  }
  // restore ASTs from kinds (ไม่เปลี่ยน)
  c.kinds = gs.kinds;
  return c;
}

module.exports = {
  DIR_NAMES, DIR_MAP,
  dirDelta, isValid, hk, parseHk,
  getNeighbor, getAdjacentHexes,
  P1_SPAWN, P2_SPAWN,
  scanForMinion, nearbyDir,
  createInitialGameState, cloneGameState,
};
