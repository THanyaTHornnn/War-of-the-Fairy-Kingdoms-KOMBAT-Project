import { hexToPixel, hexPoints, HEX_SIZE, GRID_ROWS, GRID_COLS } from "../utils/hexUtils";

const ZONE_STYLE = {
  1:      { fill: 'rgb(208, 179, 231)', stroke: '#532064' },
  2:      { fill: 'rgba(235, 243, 176, 0.97)', stroke: '#e1c855' },
  0:      { fill: 'rgba(7, 7, 7, 0.66)', stroke: 'rgb(76, 76, 81)' },
  sel:    { fill: 'rgba(239,68,68,0.4)',    stroke: '#ef4444' },
  valid:  { fill: 'rgba(250,204,21,0.22)',  stroke: 'rgba(250,204,21,0.85)' },
  canBuy: { fill: 'rgba(250,204,21,0.15)',  stroke: 'rgba(250,204,21,0.6)' },
};

const MINION_EMOJI = { verdant:'🌸', celestia:'💜', ivy:'🍀', nyx:'🐉', mibi:'⚔️' };
const HP_COLOR = ['#dbb4f5', '#efef88'];

function getMinionEmoji(type) {S
  if (!type) return '?';
  const key = type.replace(/^Minion/i, '').toLowerCase();
  return MINION_EMOJI[key] || '?';
}

function getMinionImageKey(type) {
  if (!type) return null;
  return type.replace(/^Minion/i, '').toLowerCase();
}

export default function HexGrid({ hexes, onHexClick, selectedHex, mode, currentTurn, onSpawnHex }) {
  const S  = HEX_SIZE;
  const HH = Math.sqrt(3) / 2 * S;
  const LABEL = 24;

  const allPixels = hexes.map(h => hexToPixel(h.row, h.col, S));
  const minX = Math.min(...allPixels.map(p => p.x)) - S;
  const minY = Math.min(...allPixels.map(p => p.y)) - HH;
  const maxX = Math.max(...allPixels.map(p => p.x)) + S;
  const maxY = Math.max(...allPixels.map(p => p.y)) + HH;

  const vbX = minX - LABEL - 2;
  const vbY = minY - LABEL - 2;
  const vbW = maxX - minX + LABEL*2 + 4;
  const vbH = maxY - minY + LABEL*2 + 4;

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
          fontSize={10} fill="rgba(255,255,255,0.45)" fontFamily="Emilys Candy, serif">{r}</text>;
      })}

      {hexes.map(hex => {
        const { x, y } = hexToPixel(hex.row, hex.col, S);
        const pts   = hexPoints(x, y, S * 0.97);
        const isSel = selectedHex === hex.id;

        const isValidSpawn = mode === 'spawn' && hex.zone === currentTurn && !hex.minion;
        const isValidBuy   = mode === 'hex' && hex.canBuy && !hex.minion;

        const st = isSel
          ? ZONE_STYLE.sel
          : isValidSpawn || isValidBuy
            ? ZONE_STYLE.valid
            : ZONE_STYLE[hex.zone] || ZONE_STYLE[0];

        const handleClick = () => {
          if (mode === 'spawn' && hex.zone === currentTurn && !hex.minion) {
            onSpawnHex?.(hex);
          } else {
            onHexClick?.(hex);
          }
        };

      return (
  <g key={hex.id} onClick={handleClick} style={{ cursor:'pointer' }}>
    {/* กำหนด clipPath เป็นหกเหลี่ยม */}
    <defs>
      <clipPath id={`clip-${hex.id}`}>
        <polygon points={pts} />
      </clipPath>
    </defs>

    <polygon
      points={pts}
      fill={st.fill}
      stroke={st.stroke}
      strokeWidth={st.strokeWidth || 1.5}
    />

    {hex.minion && getMinionImageKey(hex.minion.type) && (
      <>
        <image
          href={`/images/${getMinionImageKey(hex.minion.type)}.png`}
          x={x - S * 0.9}
          y={y - S * 0.9}
          width={S * 1.8}
          height={S * 1.8}
          clipPath={`url(#clip-${hex.id})`} // ← ตัดให้เป็น hex
          style={{ pointerEvents:'none' }}
        />
        <text
          x={x}
          y={y + S*0.62}
          textAnchor="middle"
          dominantBaseline="middle"
          fontSize={15}
          fill={HP_COLOR[hex.minion.player - 1]}
          fontWeight="bold"
          fontFamily="Emilys Candy, serif"
          style={{ pointerEvents:'none' }}
        >
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