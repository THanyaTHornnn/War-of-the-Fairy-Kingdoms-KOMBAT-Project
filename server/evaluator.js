/**
 * ============================================================
 *  KOMBAT Game - evaluator.js
 *  Person 2: Tokenizer + Parser + Evaluator
 * ============================================================
 *  ระบบประมวลผลภาษาสคริปต์ของ minion
 *  - Tokenizer: แปลงข้อความเป็น token
 *  - Parser: สร้าง AST (Abstract Syntax Tree)
 *  - Evaluator: รัน AST พร้อม game context
 * ============================================================
 *
 *  Grammar (ตามสเปค):
 *    Strategy    → Statement+
 *    Statement   → Command | Block | IfStatement | WhileStatement
 *    Command     → Assignment | DoneCommand | MoveCommand | ShootCommand
 *    Assignment  → Identifier '=' Expression
 *    DoneCommand → 'done'
 *    MoveCommand → 'move' Direction
 *    ShootCommand→ 'shoot' Direction Expression
 *    Block       → '{' Statement+ '}'
 *    IfStatement → 'if' '(' Expression ')' 'then' Statement 'else' Statement
 *    WhileStatement → 'while' '(' Expression ')' Statement
 *    Direction   → 'up' | 'upright' | 'downright' | 'down' | 'downleft' | 'upleft'
 *    Expression  → Term (('+' | '-') Term)*
 *    Term        → Factor (('*' | '%') Factor)*
 *    Factor      → Power ('^' Power)*
 *    Power       → Number | Identifier | '(' Expression ')' | InfoExpression
 *    InfoExpression → 'opponent' | 'ally' | 'nearby' Direction
 */

const { DIR_MAP, scanForMinion, nearbyDir } = require('./hexGrid');

// ---- ค่าคงที่ ----
const RESERVED = new Set([
  'ally', 'done', 'down', 'downleft', 'downright',
  'else', 'if', 'move', 'nearby', 'opponent',
  'shoot', 'then', 'up', 'upleft', 'upright', 'while'
]);

const SPECIAL_VARS = new Set([
  'row', 'col', 'Budget', 'Int', 'MaxBudget', 'SpawnsLeft', 'random'
]);

// จำลอง Java long overflow: [-2^63, 2^63-1]
const LONG_MAX = 9223372036854775807n;
const LONG_MIN = -9223372036854775808n;

/**
 * จำลอง overflow ของ Java long
 * @param {bigint} v
 * @returns {number}
 */
function LV(v) {
  let bv = BigInt(v);
  // wrap around ถ้าเกิน range
  const range = LONG_MAX - LONG_MIN + 1n;
  bv = ((bv - LONG_MIN) % range + range) % range + LONG_MIN;
  // convert กลับเป็น number (อาจสูญเสียความแม่นยำถ้าค่ามาก)
  if (bv >= BigInt(Number.MIN_SAFE_INTEGER) && bv <= BigInt(Number.MAX_SAFE_INTEGER)) {
    return Number(bv);
  }
  return Number(bv);
}

/**
 * Integer power with safety limit
 */
function ipow(base, exp) {
  if (exp < 0n) return 0n;
  if (exp === 0n) return 1n;
  let result = 1n;
  let b = base;
  let e = exp;
  let iters = 0;
  while (e > 0n) {
    if (iters++ > 100) return result; // safety limit
    if (e & 1n) result *= b;
    b *= b;
    e >>= 1n;
  }
  return result;
}

// ============================================================
//  TOKENIZER
// ============================================================

/**
 * แปลงซอร์สโค้ดเป็น array ของ token
 * Token format: { t: type, v: value }
 * Types: 'NUM', 'ID', 'KW', 'OP', 'PAREN', 'BRACE', 'DIR'
 */
function tokenize(src) {
  // ลบ comment (# จนจบบรรทัด)
  const lines = src.split('\n');
  const cleaned = lines.map(line => {
    const idx = line.indexOf('#');
    return idx >= 0 ? line.substring(0, idx) : line;
  }).join('\n');

  const tokens = [];
  let i = 0;
  const s = cleaned;

  while (i < s.length) {
    // ข้ามช่องว่าง
    if (/\s/.test(s[i])) { i++; continue; }

    // ตัวเลข
    if (/\d/.test(s[i])) {
      let num = '';
      while (i < s.length && /\d/.test(s[i])) num += s[i++];
      tokens.push({ t: 'NUM', v: parseInt(num, 10) });
      continue;
    }

    // ตัวอักษร (identifier หรือ keyword)
    if (/[a-zA-Z_]/.test(s[i])) {
      let word = '';
      while (i < s.length && /[a-zA-Z_0-9]/.test(s[i])) word += s[i++];

      if (DIR_MAP[word] !== undefined) {
        tokens.push({ t: 'DIR', v: word });
      } else if (RESERVED.has(word)) {
        tokens.push({ t: 'KW', v: word });
      } else {
        tokens.push({ t: 'ID', v: word });
      }
      continue;
    }

    // operators และวงเล็บ
    if ('+-*%^='.includes(s[i])) {
      tokens.push({ t: 'OP', v: s[i++] });
      continue;
    }
    if ('()'.includes(s[i])) {
      tokens.push({ t: 'PAREN', v: s[i++] });
      continue;
    }
    if ('{}'.includes(s[i])) {
      tokens.push({ t: 'BRACE', v: s[i++] });
      continue;
    }

    // ข้ามตัวอักษรที่ไม่รู้จัก
    i++;
  }

  return tokens;
}

// ============================================================
//  PARSER (Recursive Descent)
// ============================================================

class Parser {
  constructor(tokens) {
    this.tokens = tokens;
    this.pos = 0;
  }

  peek() { return this.pos < this.tokens.length ? this.tokens[this.pos] : null; }
  advance() { return this.tokens[this.pos++]; }

  expect(type, value) {
    const t = this.advance();
    if (!t || t.t !== type || (value !== undefined && t.v !== value)) {
      throw new Error(`Parse error: expected ${type}${value ? '=' + value : ''} at pos ${this.pos - 1}, got ${t ? t.t + '=' + t.v : 'EOF'}`);
    }
    return t;
  }

  /**
   * Strategy → Statement+
   * คืนค่า: { T:'S', body: [Statement] }
   */
  parseAll() {
    const stmts = [];
    while (this.peek()) {
      stmts.push(this.stmt());
    }
    if (stmts.length === 0) throw new Error('Empty strategy');
    return { T: 'S', body: stmts };
  }

  /**
   * Statement → Command | Block | IfStatement | WhileStatement
   */
  stmt() {
    const p = this.peek();
    if (!p) throw new Error('Unexpected end of input');

    if (p.t === 'BRACE' && p.v === '{') return this.block();
    if (p.t === 'KW' && p.v === 'if') return this.ifStmt();
    if (p.t === 'KW' && p.v === 'while') return this.whileStmt();
    return this.cmd();
  }

  /**
   * Block → '{' Statement+ '}'
   */
  block() {
    this.expect('BRACE', '{');
    const stmts = [];
    while (this.peek() && !(this.peek().t === 'BRACE' && this.peek().v === '}')) {
      stmts.push(this.stmt());
    }
    this.expect('BRACE', '}');
    return { T: 'B', body: stmts };
  }

  /**
   * IfStatement → 'if' '(' Expression ')' 'then' Statement 'else' Statement
   */
  ifStmt() {
    this.expect('KW', 'if');
    this.expect('PAREN', '(');
    const cond = this.expr();
    this.expect('PAREN', ')');
    this.expect('KW', 'then');
    const then = this.stmt();
    this.expect('KW', 'else');
    const el = this.stmt();
    return { T: 'IF', cond, then, el };
  }

  /**
   * WhileStatement → 'while' '(' Expression ')' Statement
   */
  whileStmt() {
    this.expect('KW', 'while');
    this.expect('PAREN', '(');
    const cond = this.expr();
    this.expect('PAREN', ')');
    const body = this.stmt();
    return { T: 'W', cond, body };
  }

  /**
   * Command → Assignment | DoneCommand | MoveCommand | ShootCommand
   */
  cmd() {
    const p = this.peek();

    if (p.t === 'KW' && p.v === 'done') {
      this.advance();
      return { T: 'D' };
    }

    if (p.t === 'KW' && p.v === 'move') {
      this.advance();
      const dir = this.expect('DIR');
      return { T: 'MV', dir: DIR_MAP[dir.v] };
    }

    if (p.t === 'KW' && p.v === 'shoot') {
      this.advance();
      const dir = this.expect('DIR');
      const exp = this.expr();
      return { T: 'SH', dir: DIR_MAP[dir.v], exp };
    }

    // Assignment: Identifier '=' Expression
    if (p.t === 'ID') {
      const id = this.advance();
      this.expect('OP', '=');
      const exp = this.expr();
      return { T: 'A', name: id.v, exp };
    }

    throw new Error(`Unexpected token: ${p.t}=${p.v} at pos ${this.pos}`);
  }

  /**
   * Expression → Term (('+' | '-') Term)*
   */
  expr() {
    let left = this.term();
    while (this.peek() && this.peek().t === 'OP' && (this.peek().v === '+' || this.peek().v === '-')) {
      const op = this.advance().v;
      const right = this.term();
      left = { T: 'OP', op, l: left, r: right };
    }
    return left;
  }

  /**
   * Term → Factor (('*' | '%') Factor)*
   */
  term() {
    let left = this.factor();
    while (this.peek() && this.peek().t === 'OP' && (this.peek().v === '*' || this.peek().v === '%')) {
      const op = this.advance().v;
      const right = this.factor();
      left = { T: 'OP', op, l: left, r: right };
    }
    return left;
  }

  /**
   * Factor → Power ('^' Power)*  (right-associative)
   */
  factor() {
    const base = this.power();
    if (this.peek() && this.peek().t === 'OP' && this.peek().v === '^') {
      this.advance();
      const exp = this.factor(); // right-associative: recursion
      return { T: 'OP', op: '^', l: base, r: exp };
    }
    return base;
  }

  /**
   * Power → Number | Identifier | '(' Expression ')' | InfoExpression
   */
  power() {
    const p = this.peek();
    if (!p) throw new Error('Unexpected end of expression');

    if (p.t === 'NUM') {
      this.advance();
      return { T: 'NUM', v: p.v };
    }

    if (p.t === 'ID') {
      this.advance();
      return { T: 'VAR', name: p.v };
    }

    if (p.t === 'PAREN' && p.v === '(') {
      this.advance();
      const e = this.expr();
      this.expect('PAREN', ')');
      return e;
    }

    // InfoExpression
    if (p.t === 'KW' && p.v === 'opponent') {
      this.advance();
      return { T: 'OPP' };
    }
    if (p.t === 'KW' && p.v === 'ally') {
      this.advance();
      return { T: 'ALLY' };
    }
    if (p.t === 'KW' && p.v === 'nearby') {
      this.advance();
      const dir = this.expect('DIR');
      return { T: 'NB', dir: DIR_MAP[dir.v] };
    }

    throw new Error(`Unexpected in expression: ${p.t}=${p.v}`);
  }
}

/**
 * Parse strategy source code into AST
 */
function parseStrategy(src) {
  const tokens = tokenize(src);
  const parser = new Parser(tokens);
  return parser.parseAll();
}

// ============================================================
//  EVALUATOR
// ============================================================

class Evaluator {
  /**
   * @param {object} gs - game state
   * @param {object} minion - minion ที่กำลัง evaluate
   * @param {number} playerIdx - index ผู้เล่น (0 หรือ 1)
   */
  constructor(gs, minion, playerIdx) {
    this.gs = gs;
    this.m = minion;
    this.pi = playerIdx;
    this.done = false;     // minion เรียก done แล้ว
    this.acted = false;    // minion ทำ action (move/shoot) แล้ว
    this.alive = true;     // minion ยังมีชีวิต
    this.actions = [];     // บันทึก action ที่เกิดขึ้น
  }

  /**
   * อ่านค่าตัวแปร (Resolve Variable)
   * - ตัวพิมพ์เล็ก = local variable ของ minion
   * - ตัวพิมพ์ใหญ่ = global variable ของ player
   * - Special vars: row, col, Budget, Int, MaxBudget, SpawnsLeft, random
   */
  rv(name) {
    switch (name) {
      case 'row': return this.m.r;
      case 'col': return this.m.c;
      case 'Budget': return Math.floor(this.gs.ps[this.pi].budget);
      case 'Int': {
        // Interest = interest_pct × log10(budget) × ln(turn)
        const budget = this.gs.ps[this.pi].budget;
        const turn = this.gs.ps[this.pi].tc;
        if (budget <= 0 || turn <= 0) return 0;
        return Math.floor(
          this.gs.cfg.interest_pct * Math.log10(budget) * Math.log(turn)
        );
      }
      case 'MaxBudget': return this.gs.cfg.max_budget;
      case 'SpawnsLeft': return this.gs.cfg.max_spawns - this.gs.ps[this.pi].sc;
      case 'random': return Math.floor(Math.random() * 1000);
      default:
        // ตัวพิมพ์ใหญ่ตัวแรก = global
        if (name[0] >= 'A' && name[0] <= 'Z') {
          return this.gs.ps[this.pi].gv[name] || 0;
        }
        // ตัวพิมพ์เล็ก = local
        return this.m.lv[name] || 0;
    }
  }

  /**
   * ตั้งค่าตัวแปร (Set Variable)
   * - Special vars เป็น read-only → ไม่ทำอะไร
   */
  sv(name, val) {
    if (SPECIAL_VARS.has(name)) return; // read-only
    if (name[0] >= 'A' && name[0] <= 'Z') {
      this.gs.ps[this.pi].gv[name] = val;
    } else {
      this.m.lv[name] = val;
    }
  }

  /**
   * Evaluate Expression → return number
   */
  ee(node) {
    switch (node.T) {
      case 'NUM': return node.v;
      case 'VAR': return this.rv(node.name);
      case 'OPP': return scanForMinion(this.m.r, this.m.c, this.gs.grid, this.pi, 'opponent');
      case 'ALLY': return scanForMinion(this.m.r, this.m.c, this.gs.grid, this.pi, 'ally');
      case 'NB': return nearbyDir(this.m.r, this.m.c, node.dir, this.gs.grid, this.pi);
      case 'OP': {
        const l = BigInt(this.ee(node.l));
        const r = BigInt(this.ee(node.r));
        switch (node.op) {
          case '+': return LV(l + r);
          case '-': return LV(l - r);
          case '*': return LV(l * r);
          case '%': return r === 0n ? 0 : LV(l % r);
          case '^': return LV(ipow(l, r));
          default: return 0;
        }
      }
      default: return 0;
    }
  }

  /**
   * Evaluate Statement
   * คืนค่า: true ถ้ายังดำเนินต่อได้, false ถ้าต้องหยุด (done หรือ action)
   */
  es(node) {
    if (this.done || this.acted || !this.alive) return false;

    switch (node.T) {
      case 'S': // Strategy (Statement+)
      case 'B': // Block
        for (const s of node.body) {
          if (!this.es(s)) return false;
        }
        return true;

      case 'D': // done
        this.done = true;
        this.actions.push({ type: 'done' });
        return false;

      case 'MV': { // move <dir>
        const { hk, getNeighbor } = require('./hexGrid');
        const player = this.gs.ps[this.pi];

        // ตรวจ budget (move costs 1)
        if (player.budget < 1) {
          this.actions.push({ type: 'move_fail', reason: 'no_budget' });
          this.done = true;
          return false;
        }

        const nb = getNeighbor(this.m.r, this.m.c, node.dir);
        if (!nb) {
          // เดินออกนอกกริด → ไม่เกิดอะไร แต่ยังเสีย budget
          player.budget -= 1;
          this.actions.push({ type: 'move_fail', reason: 'out_of_bounds' });
          this.acted = true;
          return false;
        }

        const [nr, nc] = nb;
        const targetKey = hk(nr, nc);

        if (this.gs.grid[targetKey]) {
          // ช่องมี minion อยู่แล้ว → ไม่ขยับ แต่เสีย budget
          player.budget -= 1;
          this.actions.push({ type: 'move_fail', reason: 'occupied' });
          this.acted = true;
          return false;
        }

        // เดินสำเร็จ
        const oldKey = hk(this.m.r, this.m.c);
        delete this.gs.grid[oldKey];
        this.m.r = nr;
        this.m.c = nc;
        this.gs.grid[targetKey] = this.m;
        player.budget -= 1;
        this.actions.push({ type: 'move', dir: node.dir, to: [nr, nc] });
        this.acted = true;
        return false;
      }

      case 'SH': { // shoot <dir> <expenditure>
        const { hk: hkF, getNeighbor: gnF } = require('./hexGrid');
        const player = this.gs.ps[this.pi];
        const expenditure = this.ee(node.exp);

        if (expenditure < 0) {
          this.done = true;
          return false;
        }

        const totalCost = expenditure + 1;
        if (player.budget < totalCost) {
          this.actions.push({ type: 'shoot_fail', reason: 'no_budget' });
          this.done = true;
          return false;
        }

        // หา target: minion แรกในทิศที่กำหนด
        let cr = this.m.r, cc = this.m.c;
        let target = null;
        let dist = 0;
        while (true) {
          const nb = gnF(cr, cc, node.dir);
          if (!nb) break;
          cr = nb[0]; cc = nb[1]; dist++;
          const m = this.gs.grid[hkF(cr, cc)];
          if (m) { target = m; break; }
        }

        player.budget -= totalCost;

        if (!target) {
          this.actions.push({ type: 'shoot_miss', dir: node.dir, expenditure });
          this.acted = true;
          return false;
        }

        // คำนวณ damage: max(1, expenditure - target.defense)
        const damage = Math.max(1, expenditure - target.def);
        target.hp -= damage;

        this.actions.push({
          type: 'shoot',
          dir: node.dir,
          expenditure,
          target: target.id,
          targetName: target.nm,
          damage,
          targetHp: target.hp,
        });

        // ถ้า target ตาย
        if (target.hp <= 0) {
          const targetKey = hkF(target.r, target.c);
          delete this.gs.grid[targetKey];
          delete this.gs.mins[target.id];
          // ลบจากรายการ minion ของผู้เล่น
          const targetPlayer = this.gs.ps[target.pi];
          targetPlayer.mids = targetPlayer.mids.filter(id => id !== target.id);
          this.actions.push({
            type: 'kill',
            targetId: target.id,
            targetName: target.nm,
            killer: this.m.nm,
          });
        }

        this.acted = true;
        return false;
      }

      case 'A': // Assignment
        this.sv(node.name, this.ee(node.exp));
        return true;

      case 'IF': // if (cond) then stmt else stmt
        if (this.ee(node.cond) !== 0) {
          return this.es(node.then);
        } else {
          return this.es(node.el);
        }

      case 'W': { // while (cond) stmt
        let iters = 0;
        while (this.ee(node.cond) !== 0) {
          if (iters++ >= 10000) break; // ป้องกัน infinite loop
          if (!this.es(node.body)) return false;
        }
        return true;
      }

      default:
        return true;
    }
  }

  /**
   * รัน strategy ทั้งหมด
   * @param {object} ast - AST จาก parser
   * @returns {{ done: boolean, actions: Array }}
   */
  run(ast) {
    try {
      this.es(ast);
    } catch (e) {
      this.actions.push({ type: 'error', message: e.message });
    }
    return {
      done: this.done,
      acted: this.acted,
      actions: this.actions,
    };
  }
}

module.exports = {
  tokenize,
  parseStrategy,
  Evaluator,
  RESERVED,
  SPECIAL_VARS,
};
