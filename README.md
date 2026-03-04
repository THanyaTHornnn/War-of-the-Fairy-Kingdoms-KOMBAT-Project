# KOMBAT - Hex Strategy Game 🎮

เกมกลยุทธ์บนกริดฐานสิบหก (Hexagonal Grid) 8×8 ผู้เล่น 2 คนสร้างสคริปต์ควบคุม minion ให้ต่อสู้กันอัตโนมัติ

## 📁 โครงสร้างโปรเจค

```
kombat/
├── server/                     # Backend (Node.js)
│   ├── package.json
│   ├── server.js               # Express + Socket.IO server
│   ├── hexGrid.js              # Person 1: Game State + Hex Board
│   ├── evaluator.js            # Person 2: Tokenizer + Parser + Evaluator
│   └── controller.js           # Person 3: Controller + Turn Flow
├── client/                     # Frontend (React)
│   ├── package.json
│   ├── public/
│   │   └── index.html
│   └── src/
│       ├── index.jsx           # React entry point
│       ├── App.jsx             # Main game component
│       ├── App.css             # Styling (Tactical theme)
│       ├── socket.js           # Socket.IO connection
│       └── components/
│           └── HexBoard.jsx    # SVG hex grid renderer
└── README.md
```

## 🚀 วิธีรัน

### ติดตั้ง Dependencies

```bash
# Backend
cd server
npm install

# Frontend
cd ../client
npm install
```

### รันโปรเจค

**Terminal 1 - Backend:**
```bash
cd server
npm start
# Server runs on http://localhost:3001
```

**Terminal 2 - Frontend:**
```bash
cd client
npm start
# React app runs on http://localhost:3000
```

### เปิดเกม
1. เปิด browser tab แรก → `http://localhost:3000` → เป็น **Player 1**
2. เปิด browser tab ที่สอง → `http://localhost:3000` → เป็น **Player 2**
3. Tab ที่ 3 เป็นต้นไป → เป็น **Spectator** (ผู้ชม)

## 🎯 วิธีเล่น

### ขั้นตอนที่ 1: Config
- Player 1 ตั้งค่าเกม (mode, parameters)
- กด "Initialize Game"

### ขั้นตอนที่ 2: Define Minion Kinds
- แต่ละ player กำหนด minion type 1-5 ชนิด
- ตั้งชื่อ (3 ตัวอักษร), defense, และ strategy script
- กด "Submit Kinds"

### ขั้นตอนที่ 3: Initial Spawn
- คลิกช่อง spawn zone (สีฟ้า/แดงจาง) เพื่อวาง minion เริ่มต้น

### ขั้นตอนที่ 4: Play
แต่ละเทิร์นประกอบด้วย:
1. **Start Turn** → ได้ budget + interest
2. **Buy Hex** (optional) → ซื้อช่องเพิ่ม spawn zone
3. **Spawn Minion** (optional) → สร้าง minion ใหม่
4. **Execute Strategies** → minion ทุกตัวรัน script อัตโนมัติ

### ขั้นตอนที่ 5: Game Over
- ชนะเมื่อ: minion ฝ่ายตรงข้ามหมด
- หมดเทิร์น: ตัดสินด้วย จำนวน minion → total HP → budget

## 📝 Strategy Script Language

### คำสั่งพื้นฐาน
```
done                  # จบเทิร์น
move <direction>      # เดิน (cost: 1 budget)
shoot <direction> <n> # ยิง (cost: n+1 budget, damage: max(1, n-defense))
x = <expression>      # กำหนดค่าตัวแปร
```

### ทิศทาง (Direction)
```
up, upright, downright, down, downleft, upleft
```

### ตัวแปรพิเศษ (Read-only)
```
row         # แถวปัจจุบัน (1-8)
col         # คอลัมน์ปัจจุบัน (1-8)
Budget      # budget ของผู้เล่น
Int         # interest ที่จะได้
MaxBudget   # budget สูงสุด
SpawnsLeft  # จำนวน spawn ที่เหลือ
random      # ค่าสุ่ม 0-999
```

### Info Expressions
```
opponent           # distance×10 + direction ของศัตรูที่ใกล้ที่สุด (0 = ไม่มี)
ally               # distance×10 + direction ของพวกเดียวกันที่ใกล้ที่สุด
nearby <direction> # 100×HP_digit + 10×def_digit + distance (ลบ = พวกเดียวกัน)
```

### Control Flow
```
if (condition) then <statement> else <statement>
while (condition) <statement>
{ statement1  statement2  ... }    # block
```

### ตัวอย่าง Strategy
```
# ถ้าเจอศัตรู → ยิง, ไม่เจอ → เดินสุ่ม
if (opponent) then {
  x = opponent
  d = x % 10
  if (d - 1) then {
    if (d - 2) then shoot downright 5
    else shoot upright 5
  } else shoot up 5
} else {
  r = random % 6
  if (r - 3) then move up
  else move down
}
done
```

## 🏗 สถาปัตยกรรม

### Backend (Node.js)
- **server.js**: Express + Socket.IO จัดการ connection ผู้เล่น/ผู้ชม
- **hexGrid.js** (Person 1): ระบบพิกัด hex, ฟังก์ชัน scan ทิศทาง, game state
- **evaluator.js** (Person 2): Tokenizer → Parser → AST → Evaluator
- **controller.js** (Person 3): GameController class จัดการ turn flow ทั้งหมด

### Frontend (React)
- **App.jsx**: Main component จัดการทุก phase ของเกม
- **HexBoard.jsx**: SVG renderer สำหรับกริดฐานสิบหก
- **socket.js**: Socket.IO client connection

### Communication Flow
```
React (Client) ←→ Socket.IO ←→ Express (Server)
                                    ↓
                              GameController
                              ↙         ↘
                        hexGrid.js    evaluator.js
```

## 🔧 Configuration Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| spawn_cost | 20 | ค่า spawn minion |
| hex_purchase_cost | 30 | ค่าซื้อ hex เพิ่ม spawn zone |
| init_budget | 25 | budget เริ่มต้น |
| init_hp | 100 | HP เริ่มต้นของ minion |
| turn_budget | 15 | budget ที่ได้ต่อเทิร์น |
| max_budget | 200 | budget สูงสุด |
| interest_pct | 5 | เปอร์เซ็นต์ดอกเบี้ย |
| max_turns | 100 | จำนวนเทิร์นสูงสุด |
| max_spawns | 10 | จำนวน spawn สูงสุดต่อผู้เล่น |

## 📋 หน้าที่แต่ละคน

- **Person 1**: Game State + Hex Board → `hexGrid.js`
- **Person 2**: Tokenizer + Parser + Evaluator → `evaluator.js`  
- **Person 3**: Controller + Turn Flow + Integration → `controller.js`, `server.js`
