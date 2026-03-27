export const PRESET_STRATEGIES = [
  {
    id: "strategy1",
    name: "STRATEGY 1 — AGGRESSIVE",
    description: "ถ้าเจอศัตรู: ถ้าอยู่ติดกันให้สุ่มยิงหรือเดินเข้าหา ถ้าอยู่ไกลให้ยิง ถ้าไม่เจอศัตรูให้เดินสุ่ม",
    code: `opp = opponent
if (opp) then {
  dist = opp / 10
  dir = opp % 10
  if (dist - 1) then {
    r = random % 3
    if (r) then {
      if (dir - 6) then move upleft
      else if (dir - 5) then move downleft
      else if (dir - 4) then move down
      else if (dir - 3) then move downright
      else if (dir - 2) then move upright
      else move up
    } else {
      cost = random % 200 + 50
      if (Budget - cost) then {
        if (dir - 6) then shoot upleft cost
        else if (dir - 5) then shoot downleft cost
        else if (dir - 4) then shoot down cost
        else if (dir - 3) then shoot downright cost
        else if (dir - 2) then shoot upright cost
        else shoot up cost
      } else done
    }
  } else {
    cost = random % 100 + 20
    if (Budget - cost) then {
      if (dir - 6) then shoot upleft cost
      else if (dir - 5) then shoot downleft cost
      else if (dir - 4) then shoot down cost
      else if (dir - 3) then shoot downright cost
      else if (dir - 2) then shoot upright cost
      else shoot up cost
    } else done
  }
} else {
  dir = random % 6 + 1
  if (dir - 6) then move upleft
  else if (dir - 5) then move downleft
  else if (dir - 4) then move down
  else if (dir - 3) then move downright
  else if (dir - 2) then move upright
  else move up
}`,
  },
  {
    id: "strategy2",
    name: "STRATEGY 2 — SNIPER",
    description: "ถ้าเจอศัตรู: ถ้าอยู่ติดกันให้เดินหนี ถ้าอยู่ไกลให้ยิง cost 15 ถ้าไม่เจอศัตรูให้เดินสุ่ม",
    code: `opp = opponent
if (opp) then {
  dist = opp / 10
  dir = opp % 10

  if (dist - 1) then {
    if (dir - 5) then move upleft
    else if (dir - 4) then move downleft
    else if (dir - 3) then move down
    else if (dir - 2) then move downright
    else if (dir - 1) then move upright
    else move up
  } else {
    cost = 15
    if (Budget - cost) then {
      if (dir - 5) then shoot upleft cost
      else if (dir - 4) then shoot downleft cost
      else if (dir - 3) then shoot down cost
      else if (dir - 2) then shoot downright cost
      else if (dir - 1) then shoot upright cost
      else shoot up cost
    } else done
  }
} else {
  dir = random % 6
  if (dir - 4) then move upleft
  else if (dir - 3) then move downleft
  else if (dir - 2) then move down
  else if (dir - 1) then move downright
  else if (dir) then move upright
  else move up
}`,
  },
];

// validate ผ่าน WebSocket (ใช้ใน CollectionScreen)
export async function validateStrategy(code) {
  if (!code || code.trim().length === 0) {
    return { valid: false, message: "Please enter a strategy" };
  }
  return { valid: true, message: "Strategy submitted for validation" };
}