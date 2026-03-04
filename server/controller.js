/**
 * ============================================================
 *  KOMBAT Game - controller.js
 *  Person 3: Controller + Turn Flow + Integration
 * ============================================================
 *  จัดการ Turn Flow ทั้งหมด:
 *  1. Budget increase + interest
 *  2. Optional hex purchase
 *  3. Optional minion spawn
 *  4. Execute all minion strategies (oldest → newest)
 *  5. Check end-game conditions
 * ============================================================
 */

const {
  hk, parseHk, isValid, getAdjacentHexes,
  createInitialGameState, cloneGameState,
  P1_SPAWN, P2_SPAWN,
} = require('./hexGrid');

const { parseStrategy, Evaluator } = require('./evaluator');

// ---- Default Configuration ----
const DEFAULT_CFG = {
  spawn_cost: 20,
  hex_purchase_cost: 30,
  init_budget: 25,
  init_hp: 100,
  turn_budget: 15,
  max_budget: 200,
  interest_pct: 5,
  max_turns: 100,
  max_spawns: 10,
};

/**
 * Parse config string (key=value format) เป็น config object
 * @param {string} text - config text
 * @returns {object} config
 */
function parseConfig(text) {
  const cfg = { ...DEFAULT_CFG };
  if (!text) return cfg;

  const lines = text.split('\n');
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    const eqIdx = trimmed.indexOf('=');
    if (eqIdx < 0) continue;
    const key = trimmed.substring(0, eqIdx).trim();
    const val = parseInt(trimmed.substring(eqIdx + 1).trim(), 10);
    if (key in cfg && !isNaN(val)) {
      cfg[key] = val;
    }
  }
  return cfg;
}

/**
 * ============================================================
 *  GAME CONTROLLER CLASS
 *  จัดการ game flow ทั้งหมด รับ input จาก UI ส่งผลกลับ
 * ============================================================
 */
class GameController {
  constructor() {
    this.gs = null;        // game state
    this.mode = 'duel';    // 'duel', 'solitaire', 'auto'
    this.phase = 'config'; // 'config', 'setupKinds', 'spawn', 'play', 'over'
    this.turnPhase = null; // 'start', 'buy', 'spawn', 'execute'
    this.kinds = [[], []]; // [player1Kinds, player2Kinds]
    this.spectators = [];  // spectator socket IDs
  }

  // ---- Phase 1: Configuration ----

  /**
   * เริ่มเกมด้วย config
   * @param {string} configText - configuration text
   * @param {string} mode - 'duel', 'solitaire', 'auto'
   */
  initGame(configText, mode) {
    const cfg = parseConfig(configText);
    this.mode = mode || 'duel';
    this.cfg = cfg;
    this.phase = 'setupKinds';
    return { phase: this.phase, cfg };
  }

  // ---- Phase 2: Setup Minion Kinds ----

  /**
   * ตั้งค่า minion kind สำหรับผู้เล่น
   * @param {number} playerIdx - 0 หรือ 1
   * @param {Array} kinds - [{ name, defense, script }]
   * @returns {object} ผลลัพธ์
   */
  setKinds(playerIdx, kinds) {
    // parse strategy ของแต่ละ kind
    const parsed = kinds.map(k => {
      try {
        const ast = parseStrategy(k.script);
        return { name: k.name.substring(0, 3), defense: parseInt(k.defense) || 0, ast, script: k.script };
      } catch (e) {
        throw new Error(`Parse error in kind "${k.name}": ${e.message}`);
      }
    });

    this.kinds[playerIdx] = parsed;

    // ถ้า solitaire/auto → สร้าง kind สำหรับ bot player 2
    if (this.mode === 'solitaire' && playerIdx === 0) {
      this.kinds[1] = this._generateBotKinds();
    }
    if (this.mode === 'auto') {
      this.kinds[0] = this._generateBotKinds();
      this.kinds[1] = this._generateBotKinds();
    }

    // ตรวจว่าทั้งสองฝ่ายมี kinds แล้ว
    if (this.kinds[0].length > 0 && this.kinds[1].length > 0) {
      // สร้าง game state
      this.gs = createInitialGameState(this.cfg, [...this.kinds[0], ...this.kinds[1]]);
      // แยก kinds ตาม player index
      this.gs.playerKinds = [this.kinds[0], this.kinds[1]];
      this.phase = 'spawn';
      this.spawnPhasePlayer = 0;
      return { phase: 'spawn', playerTurn: 0 };
    }

    return { phase: 'setupKinds', waitingFor: playerIdx === 0 ? 1 : 0 };
  }

  /**
   * สร้าง bot kinds (สำหรับ solitaire/auto mode)
   */
  _generateBotKinds() {
    const botScript = `
# Bot Strategy: Simple aggressive
t = 0
while (t - t) move up
t = t + 1
if (opponent) then {
  x = opponent
  d = x % 10
  if (d - 1) then {
    if (d - 2) then {
      if (d - 3) then {
        if (d - 4) then {
          if (d - 5) then shoot upleft Budget % 10 + 3
          else shoot downleft Budget % 10 + 3
        } else shoot down Budget % 10 + 3
      } else shoot downright Budget % 10 + 3
    } else shoot upright Budget % 10 + 3
  } else shoot up Budget % 10 + 3
} else {
  r = random % 6
  if (r % 6) then {
    if (r - 1) then {
      if (r - 2) then {
        if (r - 3) then {
          if (r - 4) then move upleft
          else move downleft
        } else move down
      } else move downright
    } else move upright
  } else move up
}
done
`;
    try {
      const ast = parseStrategy(botScript);
      return [{ name: 'BOT', defense: 2, ast, script: botScript }];
    } catch (e) {
      // fallback simple strategy
      const simple = 'done';
      return [{ name: 'BOT', defense: 0, ast: parseStrategy(simple), script: simple }];
    }
  }

  // ---- Phase 3: Initial Spawn ----

  /**
   * วาง minion เริ่มต้น (1 ตัวฟรี ต่อผู้เล่น)
   * @param {number} playerIdx
   * @param {number} r - row
   * @param {number} c - col
   * @param {number} kindIdx - index ของ kind (ภายใน kinds ของผู้เล่น)
   */
  initialSpawn(playerIdx, r, c, kindIdx) {
    if (this.phase !== 'spawn') throw new Error('Not in spawn phase');
    if (playerIdx !== this.spawnPhasePlayer) throw new Error('Not your turn to spawn');

    const key = hk(r, c);
    const player = this.gs.ps[playerIdx];

    // ตรวจว่าอยู่ใน spawn zone
    if (!player.sh.has(key)) throw new Error('Not in spawn zone');
    // ตรวจว่าไม่มี minion อยู่
    if (this.gs.grid[key]) throw new Error('Hex occupied');

    // สร้าง minion
    const kinds = this.gs.playerKinds[playerIdx];
    if (kindIdx < 0 || kindIdx >= kinds.length) throw new Error('Invalid kind index');

    const kind = kinds[kindIdx];
    const minion = {
      id: this.gs.nid++,
      pi: playerIdx,
      ki: kindIdx,
      nm: kind.name,
      r, c,
      hp: this.gs.cfg.init_hp,
      def: kind.defense,
      lv: {}, // local variables
    };

    this.gs.grid[key] = minion;
    this.gs.mins[minion.id] = minion;
    player.mids.push(minion.id);
    player.sc++;

    this.gs.log.push(`P${playerIdx + 1} spawned ${kind.name} at (${r},${c})`);

    // ถัดไป
    if (this.spawnPhasePlayer === 0) {
      this.spawnPhasePlayer = 1;

      // ถ้า bot → spawn อัตโนมัติ
      if (this.mode === 'solitaire' || this.mode === 'auto') {
        this._botInitialSpawn(1);
        // ถ้า auto → spawn P1 ด้วย
        if (this.mode === 'auto') {
          // P1 spawn แล้ว, P2 spawn แล้ว → เริ่มเกม
        }
        this.phase = 'play';
        this.gs.cp = 0;
        this.turnPhase = 'start';
        return this._getPlayState();
      }

      return { phase: 'spawn', playerTurn: 1 };
    } else {
      // ทั้งสองฝ่าย spawn แล้ว → เริ่มเกม
      this.phase = 'play';
      this.gs.cp = 0;
      this.turnPhase = 'start';
      return this._getPlayState();
    }
  }

  /**
   * Bot auto spawn
   */
  _botInitialSpawn(playerIdx) {
    const player = this.gs.ps[playerIdx];
    const spawnHexes = [...player.sh];

    // หาช่องว่าง
    for (const key of spawnHexes) {
      if (!this.gs.grid[key]) {
        const [r, c] = parseHk(key);
        const kinds = this.gs.playerKinds[playerIdx];
        const kind = kinds[0]; // ใช้ kind แรก

        const minion = {
          id: this.gs.nid++,
          pi: playerIdx,
          ki: 0,
          nm: kind.name,
          r, c,
          hp: this.gs.cfg.init_hp,
          def: kind.defense,
          lv: {},
        };

        this.gs.grid[key] = minion;
        this.gs.mins[minion.id] = minion;
        player.mids.push(minion.id);
        player.sc++;
        this.gs.log.push(`P${playerIdx + 1} (Bot) spawned ${kind.name} at (${r},${c})`);
        return;
      }
    }
  }

  // ---- Phase 4: Play (Main Game Loop) ----

  /**
   * เริ่มเทิร์นใหม่ → เพิ่ม budget + interest
   */
  startTurn() {
    if (this.phase !== 'play') throw new Error('Not in play phase');
    if (this.turnPhase !== 'start') throw new Error('Not in start phase');

    const pi = this.gs.cp;
    const player = this.gs.ps[pi];
    player.tc++;
    this.gs.turn++;

    // เพิ่ม turn budget
    player.budget += this.gs.cfg.turn_budget;

    // คำนวณ interest: interest_pct × log10(budget) × ln(turn)
    if (player.budget > 0 && player.tc > 0) {
      const interest = this.gs.cfg.interest_pct
        * Math.log10(player.budget)
        * Math.log(player.tc);
      player.budget += interest;
    }

    // cap at max_budget
    if (player.budget > this.gs.cfg.max_budget) {
      player.budget = this.gs.cfg.max_budget;
    }

    this.gs.log.push(`--- P${pi + 1} Turn ${player.tc} | Budget: ${Math.floor(player.budget)} ---`);

    this.turnPhase = 'buy';
    return this._getPlayState();
  }

  /**
   * ซื้อ hex เพิ่ม spawn zone (optional)
   * @param {number} r - row
   * @param {number} c - col
   */
  buyHex(r, c) {
    if (this.turnPhase !== 'buy') throw new Error('Not in buy phase');

    const pi = this.gs.cp;
    const player = this.gs.ps[pi];
    const key = hk(r, c);
    const cost = this.gs.cfg.hex_purchase_cost;

    // ตรวจ budget
    if (player.budget < cost) throw new Error('Not enough budget');
    // ตรวจว่ายังไม่ได้เป็น spawn zone
    if (player.sh.has(key)) throw new Error('Already in spawn zone');
    // ตรวจว่าอยู่ติดกับ spawn zone ที่มีอยู่
    const [tr, tc] = [r, c];
    const adj = getAdjacentHexes(tr, tc);
    const isAdjacentToSpawn = adj.some(([ar, ac]) => player.sh.has(hk(ar, ac)));
    if (!isAdjacentToSpawn) throw new Error('Must be adjacent to existing spawn zone');

    player.budget -= cost;
    player.sh.add(key);
    this.gs.log.push(`P${pi + 1} bought hex (${r},${c})`);

    return this._getPlayState();
  }

  /**
   * ข้ามขั้นตอนซื้อ hex
   */
  skipBuy() {
    if (this.turnPhase !== 'buy') throw new Error('Not in buy phase');
    this.turnPhase = 'spawn';
    return this._getPlayState();
  }

  /**
   * Spawn minion ใหม่ (optional)
   * @param {number} r
   * @param {number} c
   * @param {number} kindIdx
   */
  spawnMinion(r, c, kindIdx) {
    if (this.turnPhase !== 'spawn') throw new Error('Not in spawn phase');

    const pi = this.gs.cp;
    const player = this.gs.ps[pi];
    const key = hk(r, c);
    const cost = this.gs.cfg.spawn_cost;

    // ตรวจสอบ
    if (player.budget < cost) throw new Error('Not enough budget');
    if (player.sc >= this.gs.cfg.max_spawns) throw new Error('Max spawns reached');
    if (!player.sh.has(key)) throw new Error('Not in spawn zone');
    if (this.gs.grid[key]) throw new Error('Hex occupied');

    const kinds = this.gs.playerKinds[pi];
    if (kindIdx < 0 || kindIdx >= kinds.length) throw new Error('Invalid kind');

    const kind = kinds[kindIdx];
    const minion = {
      id: this.gs.nid++,
      pi,
      ki: kindIdx,
      nm: kind.name,
      r, c,
      hp: this.gs.cfg.init_hp,
      def: kind.defense,
      lv: {},
    };

    player.budget -= cost;
    this.gs.grid[key] = minion;
    this.gs.mins[minion.id] = minion;
    player.mids.push(minion.id);
    player.sc++;

    this.gs.log.push(`P${pi + 1} spawned ${kind.name} at (${r},${c}) [cost: ${cost}]`);

    return this._getPlayState();
  }

  /**
   * ข้ามขั้นตอน spawn
   */
  skipSpawn() {
    if (this.turnPhase !== 'spawn') throw new Error('Not in spawn phase');
    this.turnPhase = 'execute';
    return this._getPlayState();
  }

  /**
   * รัน strategy ของ minion ทั้งหมด (oldest → newest)
   * นี่คือหัวใจของ turn execution
   */
  executeStrategies() {
    if (this.turnPhase !== 'execute') throw new Error('Not in execute phase');

    const pi = this.gs.cp;
    const player = this.gs.ps[pi];
    const kinds = this.gs.playerKinds[pi];
    const execLog = [];

    // execute ตามลำดับ oldest → newest (ตาม mids array)
    const minionIds = [...player.mids]; // copy เพราะอาจถูกแก้ไขระหว่าง execute

    for (const mid of minionIds) {
      const minion = this.gs.mins[mid];
      if (!minion) continue; // อาจถูกฆ่าแล้ว

      const kind = kinds[minion.ki];
      if (!kind || !kind.ast) continue;

      const evaluator = new Evaluator(this.gs, minion, pi);
      const result = evaluator.run(kind.ast);

      for (const action of result.actions) {
        switch (action.type) {
          case 'move':
            execLog.push(`  ${minion.nm}#${mid}: moved dir${action.dir} → (${action.to[0]},${action.to[1]})`);
            break;
          case 'shoot':
            execLog.push(`  ${minion.nm}#${mid}: shot dir${action.dir} exp=${action.expenditure} → ${action.targetName} dmg=${action.damage} hp=${action.targetHp}`);
            break;
          case 'kill':
            execLog.push(`  ★ ${action.killer} killed ${action.targetName}#${action.targetId}!`);
            break;
          case 'move_fail':
            execLog.push(`  ${minion.nm}#${mid}: move failed (${action.reason})`);
            break;
          case 'shoot_miss':
            execLog.push(`  ${minion.nm}#${mid}: shot missed dir${action.dir}`);
            break;
          case 'error':
            execLog.push(`  ${minion.nm}#${mid}: ERROR: ${action.message}`);
            break;
          case 'done':
            execLog.push(`  ${minion.nm}#${mid}: done`);
            break;
        }
      }
    }

    if (execLog.length > 0) {
      this.gs.log.push(...execLog);
    }

    // ตรวจ end-game
    const endResult = this._checkEndGame();
    if (endResult) {
      this.phase = 'over';
      this.gs.over = true;
      this.gs.winner = endResult.winner;
      this.gs.log.push(`=== GAME OVER: ${endResult.message} ===`);
      return this._getPlayState();
    }

    // สลับผู้เล่น
    this.gs.cp = 1 - this.gs.cp;
    this.turnPhase = 'start';

    // ถ้า bot turn → auto execute
    if (this._isBotTurn()) {
      return this._executeBotTurn();
    }

    return this._getPlayState();
  }

  /**
   * ตรวจสอบเงื่อนไข end-game
   */
  _checkEndGame() {
    const p1 = this.gs.ps[0];
    const p2 = this.gs.ps[1];

    // ตรวจว่า minion หมดหรือยัง
    const p1Alive = p1.mids.filter(id => this.gs.mins[id]).length;
    const p2Alive = p2.mids.filter(id => this.gs.mins[id]).length;

    // ผู้เล่นที่ minion หมดก่อน → แพ้
    if (p1Alive === 0 && p2Alive > 0) {
      return { winner: 'P2', message: 'P2 wins! P1 has no minions left.' };
    }
    if (p2Alive === 0 && p1Alive > 0) {
      return { winner: 'P1', message: 'P1 wins! P2 has no minions left.' };
    }
    if (p1Alive === 0 && p2Alive === 0) {
      return { winner: 'draw', message: 'Draw! Both players lost all minions.' };
    }

    // ตรวจ max turns
    if (p1.tc >= this.gs.cfg.max_turns && p2.tc >= this.gs.cfg.max_turns) {
      // Tiebreaker: จำนวน minion → total HP → budget
      if (p1Alive !== p2Alive) {
        const w = p1Alive > p2Alive ? 'P1' : 'P2';
        return { winner: w, message: `${w} wins by minion count (${p1Alive} vs ${p2Alive})` };
      }

      const p1Hp = p1.mids.reduce((s, id) => s + (this.gs.mins[id]?.hp || 0), 0);
      const p2Hp = p2.mids.reduce((s, id) => s + (this.gs.mins[id]?.hp || 0), 0);
      if (p1Hp !== p2Hp) {
        const w = p1Hp > p2Hp ? 'P1' : 'P2';
        return { winner: w, message: `${w} wins by total HP (${p1Hp} vs ${p2Hp})` };
      }

      if (Math.floor(p1.budget) !== Math.floor(p2.budget)) {
        const w = p1.budget > p2.budget ? 'P1' : 'P2';
        return { winner: w, message: `${w} wins by budget (${Math.floor(p1.budget)} vs ${Math.floor(p2.budget)})` };
      }

      return { winner: 'draw', message: 'Draw! Perfectly tied.' };
    }

    return null;
  }

  /**
   * ตรวจว่าเป็น bot turn หรือไม่
   */
  _isBotTurn() {
    if (this.mode === 'auto') return true;
    if (this.mode === 'solitaire' && this.gs.cp === 1) return true;
    return false;
  }

  /**
   * Execute bot turn อัตโนมัติ
   */
  _executeBotTurn() {
    // Start turn
    this.turnPhase = 'start';
    this.startTurn();

    // Skip buy
    this.turnPhase = 'buy';
    this.skipBuy();

    // Optionally spawn
    const pi = this.gs.cp;
    const player = this.gs.ps[pi];
    const kinds = this.gs.playerKinds[pi];

    if (player.budget >= this.gs.cfg.spawn_cost &&
        player.sc < this.gs.cfg.max_spawns &&
        Math.random() > 0.4) {
      // หาช่อง spawn ว่าง
      const spawnHexes = [...player.sh];
      const emptySpawn = spawnHexes.find(k => !this.gs.grid[k]);
      if (emptySpawn) {
        const [r, c] = parseHk(emptySpawn);
        const kindIdx = Math.floor(Math.random() * kinds.length);
        try {
          this.turnPhase = 'spawn';
          this.spawnMinion(r, c, kindIdx);
        } catch (e) { /* ignore */ }
      }
    }

    // Execute strategies
    this.turnPhase = 'execute';
    return this.executeStrategies();
  }

  /**
   * รวม state ทั้งหมดเพื่อส่งให้ client
   */
  _getPlayState() {
    if (!this.gs) return { phase: this.phase };

    const players = this.gs.ps.map((p, i) => {
      const kinds = this.gs.playerKinds ? this.gs.playerKinds[i] : [];
      const aliveMids = p.mids.filter(id => this.gs.mins[id]);
      const totalHp = aliveMids.reduce((s, id) => s + (this.gs.mins[id]?.hp || 0), 0);
      return {
        budget: Math.floor(p.budget),
        turnCount: p.tc,
        spawnCount: p.sc,
        spawnsLeft: this.gs.cfg.max_spawns - p.sc,
        minionCount: aliveMids.length,
        totalHp,
        spawnZone: [...p.sh],
        kinds: kinds.map(k => ({ name: k.name, defense: k.defense })),
        minions: aliveMids.map(id => {
          const m = this.gs.mins[id];
          return m ? { id: m.id, name: m.nm, r: m.r, c: m.c, hp: m.hp, def: m.def, kindIdx: m.ki } : null;
        }).filter(Boolean),
      };
    });

    // สร้าง grid data สำหรับ rendering
    const grid = {};
    for (const [key, m] of Object.entries(this.gs.grid)) {
      grid[key] = {
        id: m.id,
        name: m.nm,
        hp: m.hp,
        def: m.def,
        player: m.pi,
      };
    }

    return {
      phase: this.phase,
      turnPhase: this.turnPhase,
      currentPlayer: this.gs.cp,
      turn: this.gs.turn,
      over: this.gs.over,
      winner: this.gs.winner,
      players,
      grid,
      log: this.gs.log.slice(-50), // ส่ง 50 บรรทัดล่าสุด
      cfg: this.gs.cfg,
      mode: this.mode,
    };
  }

  /**
   * Get full state (for spectators)
   */
  getState() {
    return this._getPlayState();
  }
}

module.exports = { GameController, parseConfig, DEFAULT_CFG };
