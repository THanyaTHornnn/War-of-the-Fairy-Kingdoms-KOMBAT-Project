/**
 * ============================================================
 *  HexBoard.jsx - Hex Grid Renderer
 *  เรนเดอร์กริดฐานสิบหก 8×8 ด้วย SVG
 * ============================================================
 */
import React, { useMemo } from 'react';

const HEX_SIZE = 32;
const HH = Math.sqrt(3) * HEX_SIZE;
const HW = HEX_SIZE * 2;
const PAD = 20;

/**
 * แปลงพิกัด hex (r,c) เป็นพิกัด pixel (cx, cy)
 * ใช้ offset column: คอลัมน์คู่เลื่อนขึ้น HH/2
 */
function hexToPixel(r, c) {
  const cx = PAD + (c - 1) * HEX_SIZE * 1.5 + HEX_SIZE;
  const cy = PAD + (r - 1) * HH + HH / 2 + (c % 2 === 0 ? -HH / 2 : 0);
  return [cx, cy];
}

/**
 * สร้างจุดหกเหลี่ยม (flat-top)
 */
function hexPoints(cx, cy) {
  const pts = [];
  for (let i = 0; i < 6; i++) {
    const angle = (Math.PI / 180) * (60 * i);
    pts.push(`${cx + HEX_SIZE * Math.cos(angle)},${cy + HEX_SIZE * Math.sin(angle)}`);
  }
  return pts.join(' ');
}

export default function HexBoard({
  grid = {},
  players = [],
  currentPlayer,
  turnPhase,
  phase,
  highlightHexes = [],
  onHexClick,
  myPlayer,
}) {
  // สร้าง Set สำหรับ lookup เร็ว
  const p1Spawn = useMemo(() => new Set(players[0]?.spawnZone || []), [players]);
  const p2Spawn = useMemo(() => new Set(players[1]?.spawnZone || []), [players]);
  const highlightSet = useMemo(() => new Set(highlightHexes), [highlightHexes]);

  // คำนวณขนาด SVG
  const svgW = PAD * 2 + 8 * HEX_SIZE * 1.5 + HEX_SIZE;
  const svgH = PAD * 2 + 8 * HH + HH / 2;

  // กำหนดว่า hex ไหนคลิกได้
  const isClickable = (r, c) => {
    if (!onHexClick) return false;
    if (phase === 'over') return false;
    return true;
  };

  const cells = [];
  for (let r = 1; r <= 8; r++) {
    for (let c = 1; c <= 8; c++) {
      const [cx, cy] = hexToPixel(r, c);
      const key = `${r},${c}`;
      const m = grid[key];
      const isP1Spawn = p1Spawn.has(key);
      const isP2Spawn = p2Spawn.has(key);
      const isHighlight = highlightSet.has(key);
      const clickable = isClickable(r, c);

      // สีพื้นหลัง
      let fillColor = '#151e2d';
      let fillOpacity = 1;
      if (isHighlight) {
        fillColor = '#ffd700';
        fillOpacity = 0.2;
      } else if (isP1Spawn && isP2Spawn) {
        fillColor = '#886600';
        fillOpacity = 0.15;
      } else if (isP1Spawn) {
        fillColor = '#00e5ff';
        fillOpacity = 0.08;
      } else if (isP2Spawn) {
        fillColor = '#ff3d5a';
        fillOpacity = 0.08;
      }

      cells.push(
        <g
          key={key}
          className={`hex-cell${clickable ? ' clickable' : ''}`}
          onClick={() => onHexClick && onHexClick(r, c)}
        >
          {/* Hex background */}
          <polygon
            className="hex-bg"
            points={hexPoints(cx, cy)}
            fill={fillColor}
            fillOpacity={fillOpacity}
            stroke="#2a3a52"
            strokeWidth="1"
          />

          {/* Coordinate label (small) */}
          <text
            x={cx}
            y={cy + HEX_SIZE - 6}
            textAnchor="middle"
            fill="#334455"
            fontSize="7"
            fontFamily="Orbitron"
          >
            {r},{c}
          </text>

          {/* Minion */}
          {m && (
            <>
              {/* Minion body circle */}
              <circle
                cx={cx}
                cy={cy - 4}
                r={HEX_SIZE * 0.55}
                fill={m.player === 0 ? 'rgba(0,229,255,0.2)' : 'rgba(255,61,90,0.2)'}
                stroke={m.player === 0 ? '#00e5ff' : '#ff3d5a'}
                strokeWidth="2"
              />

              {/* Minion name */}
              <text
                x={cx}
                y={cy - 6}
                textAnchor="middle"
                dominantBaseline="middle"
                fill={m.player === 0 ? '#00e5ff' : '#ff3d5a'}
                fontSize="10"
                fontFamily="Orbitron"
                fontWeight="700"
              >
                {m.name}
              </text>

              {/* HP text */}
              <text
                x={cx}
                y={cy + 10}
                textAnchor="middle"
                fill="#00ff88"
                fontSize="8"
                fontFamily="Orbitron"
              >
                {m.hp}
              </text>

              {/* Defense indicator */}
              {m.def > 0 && (
                <text
                  x={cx + HEX_SIZE * 0.4}
                  y={cy - HEX_SIZE * 0.3}
                  textAnchor="middle"
                  fill="#ffd700"
                  fontSize="7"
                  fontFamily="Orbitron"
                >
                  🛡{m.def}
                </text>
              )}
            </>
          )}
        </g>
      );
    }
  }

  return (
    <svg
      className="hex-board"
      width={svgW}
      height={svgH}
      viewBox={`0 0 ${svgW} ${svgH}`}
    >
      {/* Background pattern */}
      <defs>
        <radialGradient id="boardGlow" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="rgba(0,229,255,0.03)" />
          <stop offset="100%" stopColor="rgba(0,0,0,0)" />
        </radialGradient>
      </defs>
      <rect x="0" y="0" width={svgW} height={svgH} fill="url(#boardGlow)" />

      {cells}
    </svg>
  );
}
