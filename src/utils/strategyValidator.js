
// export const PRESET_STRATEGIES = [
//   {
//     id: "strategy1",
//     name: "Strategy 1",
//     description: "โจมตีศัตรูที่ใกล้ที่สุดก่อน ถ้าไม่มีศัตรูให้เคลื่อนที่ไปตรงกลาง",
//     code: `if enemy.nearest then
//   attack enemy.nearest
// else
//   move hex.center
// end`,
//   },
//   {
//     id: "strategy2",
//     name: "Strategy 2",
//     description: "ถ้า HP ต่ำกว่า 30 ให้ตั้งรับก่อน ไม่งั้นโจมตีศัตรูที่ใกล้ที่สุด",
//     code: `if hp < 30 then
//   defend
// else if enemy.nearest then
//   attack enemy.nearest
// else
//   move hex.nearest_ally
// end`,
//   },
// ];

// const API = "http://localhost:8080/api/game";

// // เรียก backend จริง — async
// export async function validateStrategy(code) {
//   if (!code || code.trim().length === 0) {
//     return { valid: false, message: "❌ กรุณากรอก strategy ก่อน" };
//   }

//   try {
//     const res = await fetch(`${API}/strategy/validate`, {
//       method: "POST",
//       headers: { "Content-Type": "application/json" },
//       body: JSON.stringify({ strategy: code }),
//     });
//     const data = await res.json();
//     return {
//       valid: data.ok && data.data?.valid,
//       message: data.ok && data.data?.valid
//         ? "✅ Strategy ถูกต้อง! กด SAVE เพื่อบันทึก"
//         : "❌ Strategy ไม่ถูกต้อง กรุณาตรวจสอบ syntax",
//     };
//   } catch {
//     return { valid: false, message: "❌ เชื่อมต่อ backend ไม่ได้ ตรวจสอบว่า server รันอยู่" };
//   }
// }
export const PRESET_STRATEGIES = [
  {
    id: "strategy1",
    name: "Strategy 1",
    description: "โจมตีศัตรูที่ใกล้ที่สุดก่อน ถ้าไม่มีศัตรูให้เคลื่อนที่ไปตรงกลาง",
    code: `if enemy.nearest then
  attack enemy.nearest
else
  move hex.center
end`,
  },
  {
    id: "strategy2",
    name: "Strategy 2",
    description: "ถ้า HP ต่ำกว่า 30 ให้ตั้งรับก่อน ไม่งั้นโจมตีศัตรูที่ใกล้ที่สุด",
    code: `if hp < 30 then
  defend
else if enemy.nearest then
  attack enemy.nearest
else
  move hex.nearest_ally
end`,
  },
];

// ให้ backend เช็คอย่างเดียว ไม่มี frontend logic เลย
export async function validateStrategy(code) {
  if (!code || code.trim().length === 0) {
    return { valid: false, message: "❌ กรุณากรอก strategy ก่อน" };
  }
  try {
    const res = await fetch("http://localhost:8080/api/game/strategy/validate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ strategy: code }),
    });
    const data = await res.json();
    return {
      valid: data.ok && data.data?.valid,
      message: data.ok && data.data?.valid
        ? "✅ Strategy ถูกต้อง!"
        : "❌ Strategy ไม่ถูกต้อง กรุณาตรวจสอบ syntax",
    };
  } catch {
    return { valid: false, message: "❌ เชื่อมต่อ backend ไม่ได้" };
  }
}