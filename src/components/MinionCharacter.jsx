import { useState, useEffect } from "react";

// ============================================================
// 🔧 EDIT HERE: แทนที่ emoji ด้วยรูปจริง
// เปลี่ยน <div className="emoji-char"> เป็น <img src={...} />
// หรือ integrate Three.js 3D model ตรงนี้
// ============================================================

const MINION_VISUALS = {
  verdant:  { emoji: "🌸", bg: "#fce7f3", glow: "#f9a8d4", label: "V" },
  celestia: { emoji: "💜", bg: "#ede9fe", glow: "#c4b5fd", label: "C" },
  ivy:      { emoji: "🍀", bg: "#dcfce7", glow: "#86efac", label: "I" },
  nyx:      { emoji: "🐉", bg: "#cffafe", glow: "#67e8f9", label: "N" },
  mibi:     { emoji: "⚔️", bg: "#fefce8", glow: "#fcd34d", label: "M" },
};

export default function MinionCharacter({ minionId, size = 120, spin = true, selected = false }) {
  const [rotation, setRotation] = useState(0);
  const vis = MINION_VISUALS[minionId] || { emoji: "❓", bg: "#e5e7eb", glow: "#9ca3af" };

  useEffect(() => {
    if (!spin) return;
    const interval = setInterval(() => {
      setRotation(r => (r + 1) % 360);
    }, 16);
    return () => clearInterval(interval);
  }, [spin]);

  const scale = selected ? 1.1 : 1;

  return (
    <div style={{
      width: size,
      height: size,
      position: "relative",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      transition: "transform 0.2s",
      transform: `scale(${scale})`,
    }}>
      {/* Glow ring */}
      <div style={{
        position: "absolute",
        width: size,
        height: size,
        borderRadius: "50%",
        background: `radial-gradient(circle, ${vis.glow}44 0%, transparent 70%)`,
        animation: "pulse 2s ease-in-out infinite",
      }} />

      {/* Character circle */}
      <div style={{
        width: size * 0.85,
        height: size * 0.85,
        borderRadius: "50%",
        background: `radial-gradient(135deg, ${vis.bg}, ${vis.glow}88)`,
        border: selected ? `3px solid ${vis.glow}` : "2px solid rgba(255,255,255,0.4)",
        boxShadow: selected ? `0 0 20px ${vis.glow}` : `0 4px 15px rgba(0,0,0,0.3)`,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: size * 0.4,
        // ============================================================
        // 🔧 EDIT HERE: เปลี่ยน CSS 3D spin ด้วย Three.js หรือ Lottie
        // transform: `rotateY(${rotation}deg)` ทำ 3D flip effect
        // ============================================================
        transform: spin ? `rotateY(${rotation}deg)` : "none",
        transformStyle: "preserve-3d",
        transition: spin ? "none" : "transform 0.3s",
        userSelect: "none",
      }}>
        {vis.emoji}
      </div>

      {selected && (
        <div style={{
          position: "absolute",
          bottom: -4,
          width: "60%",
          height: 4,
          background: vis.glow,
          borderRadius: 2,
          boxShadow: `0 0 8px ${vis.glow}`,
        }} />
      )}
    </div>
  );
}
