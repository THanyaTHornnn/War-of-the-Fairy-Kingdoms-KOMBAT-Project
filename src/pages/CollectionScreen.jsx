import { useState } from "react";
import BackButton from "../components/BackButton";
import { useGame, MINIONS, BASE_HP } from "../context/GameContext";
import MinionCharacter from "../components/MinionCharacter";
import { PRESET_STRATEGIES } from "../utils/strategyValidator";

const WS_URL = "ws://localhost:8080/ws/game";

// validate strategy ผ่าน WebSocket
async function checkStrategy(code) {
  return new Promise((resolve) => {
    const ws = new WebSocket(WS_URL);
    ws.onopen = () => {
      ws.send(JSON.stringify({ action: "validate", strategy: code }));
    };
    ws.onmessage = (e) => {
      const data = JSON.parse(e.data);
      ws.close();
      resolve({
        valid: data.ok && data.data?.valid,
        message: data.ok && data.data?.valid
          ? "✅ Strategy ถูกต้อง! กด SAVE เพื่อบันทึก"
          : "❌ Strategy ไม่ถูกต้อง กรุณาตรวจสอบ syntax",
      });
    };
    ws.onerror = () => {
      resolve({ valid: false, message: "❌ เชื่อมต่อ backend ไม่ได้" });
    };
  });
}

function StrategyDetailPage({ preset, onSelect, onBack }) {
  return (
    <div style={{
      width:"100vw", height:"100vh", display:"flex", flexDirection:"column",
      alignItems:"center", justifyContent:"center",
      background:"radial-gradient(ellipse at center, #1e1b4b 0%, #0f0c29 40%, #0a0a1a 100%)",
      fontFamily:"'Cinzel', serif", position:"relative",
    }}>
      <BackButton onClick={onBack} />
      <div style={{
        background:"rgba(255,255,255,0.08)", backdropFilter:"blur(16px)",
        border:"1px solid rgba(255,255,255,0.2)", borderRadius:24,
        padding:"36px 44px", maxWidth:480, width:"90%",
      }}>
        <div style={{ color:"#c4b5fd", fontSize:12, letterSpacing:3, marginBottom:6 }}>PRESET STRATEGY</div>
        <h2 style={{ color:"#e2d9f3", fontSize:26, fontWeight:900, textShadow:"0 0 20px #c4b5fd", marginBottom:20 }}>
          {preset.name}
        </h2>
        <pre style={{
          background:"rgba(0,0,0,0.4)", border:"1px solid rgba(196,181,253,0.3)",
          borderRadius:12, padding:"14px 18px", color:"#a5f3fc",
          fontSize:13, fontFamily:"monospace", whiteSpace:"pre-wrap", lineHeight:1.7, marginBottom:14,
        }}>{preset.code}</pre>
        <div style={{
          background:"rgba(196,181,253,0.08)", border:"1px solid rgba(196,181,253,0.2)",
          borderRadius:10, padding:"10px 14px", marginBottom:24,
          color:"rgba(255,255,255,0.65)", fontSize:12, lineHeight:1.6,
        }}>
          {preset.description || "Strategy สำเร็จรูป — ใช้ได้ทันที"}
        </div>
        <button onClick={() => onSelect(preset)} style={{
          width:"100%", padding:"13px", borderRadius:30,
          border:"1.5px solid rgba(196,181,253,0.6)",
          background:"rgba(196,181,253,0.2)", color:"#c4b5fd",
          cursor:"pointer", fontFamily:"'Cinzel', serif", fontSize:15, fontWeight:700,
        }}>SELECT ✓</button>
      </div>
    </div>
  );
}

function CustomStrategyPage({ onSave, onBack }) {
  const [code, setCode] = useState("");
  const [checkResult, setCheckResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleCheck = async () => {
    setLoading(true);
    const result = await checkStrategy(code);
    setCheckResult(result);
    setLoading(false);
  };

  const handleSave = async () => {
    setLoading(true);
    const result = await checkStrategy(code);
    setCheckResult(result);
    setLoading(false);
    if (result.valid) onSave(code);
  };

  return (
    <div style={{
      width:"100vw", height:"100vh", display:"flex", flexDirection:"column",
      alignItems:"center", justifyContent:"center",
      background:"radial-gradient(ellipse at center, #1e1b4b 0%, #0f0c29 40%, #0a0a1a 100%)",
      fontFamily:"'Cinzel', serif", position:"relative",
    }}>
      <BackButton onClick={onBack} />
      <div style={{
        background:"rgba(255,255,255,0.08)", backdropFilter:"blur(16px)",
        border:"1px solid rgba(255,255,255,0.2)", borderRadius:24,
        padding:"36px 44px", maxWidth:520, width:"90%",
      }}>
        <div style={{ color:"#c4b5fd", fontSize:12, letterSpacing:3, marginBottom:6 }}>CUSTOM STRATEGY</div>
        <h2 style={{ color:"#e2d9f3", fontSize:26, fontWeight:900, textShadow:"0 0 20px #c4b5fd", marginBottom:20 }}>
          Set Your Strategy
        </h2>
        <textarea value={code}
          onChange={e => { setCode(e.target.value); setCheckResult(null); }}
          placeholder="your strategy…" rows={8}
          style={{
            width:"100%", padding:"14px", borderRadius:12,
            border:"1px solid rgba(196,181,253,0.3)",
            background:"rgba(255,255,255,0.07)", color:"#fff",
            fontSize:14, fontFamily:"monospace", resize:"none",
            outline:"none", boxSizing:"border-box", marginBottom:12,
          }}
        />
        {checkResult && (
          <div style={{
            padding:"8px 14px", borderRadius:10, marginBottom:12, fontSize:13,
            background:checkResult.valid?"rgba(34,197,94,0.15)":"rgba(239,68,68,0.15)",
            border:`1px solid ${checkResult.valid?"#22c55e":"#ef4444"}`,
            color:checkResult.valid?"#86efac":"#fca5a5",
          }}>{checkResult.message}</div>
        )}
        <div style={{ display:"flex", gap:10 }}>
          <button onClick={handleCheck} disabled={loading} style={{
            flex:1, padding:"12px", borderRadius:20,
            border:"1px solid rgba(196,181,253,0.5)", background:"rgba(196,181,253,0.1)",
            color:"#c4b5fd", cursor:"pointer", fontFamily:"'Cinzel', serif", fontSize:14, fontWeight:700,
          }}>{loading ? "…" : "CHECK ✓"}</button>
          <button onClick={handleSave} disabled={loading} style={{
            flex:1, padding:"12px", borderRadius:20,
            border:"1.5px solid rgba(134,239,172,0.6)", background:"rgba(134,239,172,0.15)",
            color:"#86efac", cursor:"pointer", fontFamily:"'Cinzel', serif", fontSize:14, fontWeight:700,
          }}>{loading ? "…" : "SAVE →"}</button>
        </div>
      </div>
    </div>
  );
}

function StrategyPanel({ value, onChange, onViewPreset, onViewCustom }) {
  const [panel, setPanel] = useState(null);

  const label = () => {
    if (!value) return null;
    const found = PRESET_STRATEGIES.find(p => p.code === value);
    return found ? found.name : "Custom";
  };

  return (
    <div>
      <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:6 }}>
        <div style={{ color:"rgba(255,255,255,0.55)", fontSize:11, letterSpacing:1 }}>STRATEGY</div>
        {label() && (
          <div style={{ color:"#86efac", fontSize:10, background:"rgba(134,239,172,0.1)", padding:"2px 8px", borderRadius:10 }}>
            ✓ {label()}
          </div>
        )}
      </div>
      {panel === null && (
        <button onClick={() => setPanel('list')} style={{
          width:"100%", padding:"7px 10px", borderRadius:10,
          border:"1px solid rgba(196,181,253,0.4)",
          background:"rgba(196,181,253,0.1)", color:"#c4b5fd",
          cursor:"pointer", fontFamily:"'Cinzel', serif", fontSize:11,
        }}>
          {value ? "Change ▾" : "Set Strategy ▾"}
        </button>
      )}
      {panel === 'list' && (
        <div style={{ display:"flex", flexDirection:"column", gap:5 }}>
          {PRESET_STRATEGIES.map(ps => (
            <button key={ps.id} onClick={() => { onViewPreset(ps); setPanel(null); }} style={{
              padding:"7px 10px", borderRadius:9, textAlign:"left",
              border:"1px solid rgba(255,255,255,0.12)",
              background:"rgba(255,255,255,0.05)", color:"#fff",
              cursor:"pointer", fontFamily:"'Cinzel', serif", fontSize:11,
              display:"flex", justifyContent:"space-between",
            }}>
              <span>{ps.name}</span><span style={{ opacity:0.4 }}>›</span>
            </button>
          ))}
          <button onClick={() => { onViewCustom(); setPanel(null); }} style={{
            padding:"7px 10px", borderRadius:9, textAlign:"left",
            border:"1px solid rgba(196,181,253,0.3)",
            background:"rgba(196,181,253,0.07)", color:"#c4b5fd",
            cursor:"pointer", fontFamily:"'Cinzel', serif", fontSize:11,
            display:"flex", justifyContent:"space-between",
          }}>
            <span>Custom</span><span>✏️</span>
          </button>
          <button onClick={() => setPanel(null)} style={{
            padding:"4px", border:"none", background:"transparent",
            color:"rgba(255,255,255,0.3)", cursor:"pointer", fontSize:11,
          }}>✕ ยกเลิก</button>
        </div>
      )}
    </div>
  );
}

export default function CollectionScreen({ onNext, onBack }) {
  const { gameState, updatePlayer } = useGame();
  const minionCount = gameState.minionCount || 1;

  const [slots, setSlots]                   = useState(() => Array(minionCount).fill(null));
  const [activeSlot, setActiveSlot]         = useState(0);
  const [defenseValues, setDefenseValues]   = useState(() => Array(minionCount).fill(null).map(() => ({})));
  const [strategyValues, setStrategyValues] = useState(() => Array(minionCount).fill(""));
  const [viewingPreset, setViewingPreset]   = useState(null);
  const [viewingCustom, setViewingCustom]   = useState(false);
  const [starting, setStarting]             = useState(false);

  const curMinionId    = slots[activeSlot];
  const curMinion      = MINIONS.find(m => m.id === curMinionId);
  const filledSlots    = slots.filter(s => s !== null).length;
  const filledStrategy = strategyValues.filter(s => s !== "").length;
  const allReady       = filledSlots === minionCount && filledStrategy === minionCount;

  const statusMsg = () => {
    if (filledSlots < minionCount) return `เลือกตัวละครยังไม่ครบ (${filledSlots}/${minionCount})`;
    if (filledStrategy < minionCount) return `ยังไม่ได้ set strategy ครบ (${filledStrategy}/${minionCount})`;
    return null;
  };

  const handleSelectMinion = (minionId) => {
    if (slots.some((s, i) => s === minionId && i !== activeSlot)) return;
    setSlots(prev => { const n = [...prev]; n[activeSlot] = minionId; return n; });
  };

  const handleDefenseChange = (minionId, val) => {
    setDefenseValues(prev => {
      const n = [...prev]; n[activeSlot] = { ...n[activeSlot], [minionId]: Number(val) }; return n;
    });
  };

  const handleStrategyChange = (val) => {
    setStrategyValues(prev => { const n = [...prev]; n[activeSlot] = val; return n; });
  };

  // ── GAME START → สร้างเกมผ่าน WebSocket ──────────────────
  
 const handleStart = async () => {
    setStarting(true);
    try {
      const minionConfigs = slots.map((minionId, i) => ({
        minionId,
        defense:  defenseValues[i]?.[minionId] ?? 10,
        strategy: strategyValues[i] || "done",
      }));

      await new Promise((resolve, reject) => {
        const ws = new WebSocket("ws://localhost:8080/ws/game");
        ws.onopen = () => {
          ws.send(JSON.stringify({ action: "join", playerId: gameState.myPlayerId || "p1" }));
        };
        ws.onmessage = (e) => {
          const msg = JSON.parse(e.data);
          if (msg.event === "joined") {
            ws.send(JSON.stringify({
              action: "create",
              mode: gameState.mode?.toUpperCase() || "DUEL",
              minionConfigs,  // ← ส่ง configs ไปเก็บที่ backend ให้ P2 ใช้
            }));
          }
          if (msg.event === "created") {
            ws.close();
            resolve();
          }
        };
        ws.onerror = () => reject(new Error("เชื่อมต่อไม่ได้"));
      });

      updatePlayer(0, {
        selectedMinions: slots,
        minionDefense:   defenseValues[activeSlot],
        strategy:        strategyValues[0] || "done",
        minionConfigs,
      });

      onNext("game");
    } catch (e) {
      alert("เชื่อมต่อ backend ไม่ได้: " + e.message);
    }
    setStarting(false);
  };


  if (viewingCustom) {
    return (
      <CustomStrategyPage
        onSave={(code) => { handleStrategyChange(code); setViewingCustom(false); }}
        onBack={() => setViewingCustom(false)}
      />
    );
  }
  if (viewingPreset) {
    return (
      <StrategyDetailPage
        preset={viewingPreset}
        onSelect={(p) => { handleStrategyChange(p.code); setViewingPreset(null); }}
        onBack={() => setViewingPreset(null)}
      />
    );
  }

  return (
    <div style={{
      width:"100vw", height:"100vh", display:"flex", flexDirection:"column",
      alignItems:"center",
      background:"radial-gradient(ellipse at center, #1e1b4b 0%, #0f0c29 40%, #0a0a1a 100%)",
      fontFamily:"'Cinzel', serif", position:"relative", overflow:"hidden", paddingTop:"2.5vh",
    }}>
      <BackButton onClick={onBack} />
      <h1 style={{
        fontSize:"clamp(18px, 2.8vw, 38px)", color:"#e2d9f3",
        textShadow:"0 0 20px #c4b5fd", marginBottom:"1vh", letterSpacing:"0.1em", fontWeight:700,
      }}>COLLECTION OF MINION</h1>

      <div style={{ display:"flex", gap:8, marginBottom:"1.5vh" }}>
        {slots.map((s, i) => (
          <button key={i} onClick={() => setActiveSlot(i)} style={{
            padding:"5px 16px", borderRadius:20, cursor:"pointer",
            fontFamily:"'Cinzel', serif", fontSize:12,
            border: activeSlot===i ? "2px solid #c4b5fd" : "1px solid rgba(255,255,255,0.2)",
            background: activeSlot===i ? "rgba(196,181,253,0.2)" : "rgba(255,255,255,0.05)",
            color: s ? "#c4b5fd" : "rgba(255,255,255,0.5)",
          }}>
            {s ? `Minion ${i+1} ✓` : `Minion ${i+1}`}
          </button>
        ))}
      </div>

      <div style={{
        display:"flex", alignItems:"center", justifyContent:"flex-start",
        gap:"20vw", flex:1, width:"100%", padding:"0 0 0 18vw", overflow:"hidden",
      }}>
        <div style={{ display:"flex", flexDirection:"column", gap:9, flexShrink:0, alignSelf:"stretch", justifyContent:"center" }}>
          <div style={{ color:"rgba(255,255,255,0.35)", fontSize:9, letterSpacing:2, textAlign:"center", marginBottom:2 }}>SELECT</div>
          {MINIONS.map(m => {
            const isSelected = curMinionId === m.id;
            const usedOther  = slots.some((s, i) => s === m.id && i !== activeSlot);
            return (
              <button key={m.id} onClick={() => handleSelectMinion(m.id)} disabled={usedOther} style={{
                width:100, height:100, borderRadius:"50%",
                border: isSelected ? "2px solid #c4b5fd" : usedOther ? "1px solid rgba(255,255,255,0.08)" : "1px solid rgba(255,255,255,0.15)",
                background: isSelected ? "rgba(196,181,253,0.2)" : usedOther ? "rgba(255,255,255,0.02)" : "rgba(255,255,255,0.04)",
                cursor: usedOther ? "not-allowed" : "pointer", opacity: usedOther ? 0.3 : 1,
                display:"flex", alignItems:"center", justifyContent:"center", transition:"all 0.2s",
              }}>
                <MinionCharacter minionId={m.id} size={81} spin={false} selected={isSelected} />
              </button>
            );
          })}
        </div>

        <div style={{ textAlign:"center", flexShrink:0 }}>
          {curMinionId ? (
            <>
              <MinionCharacter minionId={curMinionId} size={140} spin={true} />
              <div style={{ color:"#e2d9f3", fontSize:"clamp(14px,1.8vw,24px)", fontWeight:700, marginTop:10 }}>
                {curMinion?.name}
              </div>
            </>
          ) : (
            <div style={{
              width:140, height:140, borderRadius:"50%",
              border:"2px dashed rgba(255,255,255,0.2)",
              display:"flex", alignItems:"center", justifyContent:"center",
              color:"rgba(255,255,255,0.3)", fontSize:12,
            }}>เลือกตัว</div>
          )}
        </div>

        <div style={{
          background:"rgba(255,255,255,0.08)", backdropFilter:"blur(16px)",
          border:"1px solid rgba(255,255,255,0.18)", borderRadius:20,
          padding:"18px 20px", minWidth:220, maxWidth:260, flexShrink:0,
        }}>
          {curMinionId ? (
            <>
              <div style={{ color:"#c4b5fd", fontSize:25, marginBottom:10, letterSpacing:2 }}>STATS</div>
              <StatRow label="HP" value={BASE_HP} color="#22c55e" />
              <div style={{ marginBottom:12 }}>
                <div style={{ color:"rgba(255,255,255,0.55)", fontSize:11, marginBottom:4 }}>DEF</div>
                <input type="number" min={0} max={100}
                  value={defenseValues[activeSlot]?.[curMinionId] ?? 10}
                  onChange={e => handleDefenseChange(curMinionId, e.target.value)}
                  style={{
                    width:"100%", padding:"6px 10px", borderRadius:9,
                    border:"1px solid rgba(196,181,253,0.5)",
                    background:"rgba(196,181,253,0.1)", color:"#fff",
                    fontSize:20, fontWeight:700, outline:"none", fontFamily:"monospace", textAlign:"center",
                  }}
                />
              </div>
              <div style={{ borderTop:"1px solid rgba(255,255,255,0.1)", paddingTop:10 }}>
                <StrategyPanel
                  value={strategyValues[activeSlot]}
                  onChange={handleStrategyChange}
                  onViewPreset={setViewingPreset}
                  onViewCustom={() => setViewingCustom(true)}
                />
              </div>
            </>
          ) : (
            <div style={{ color:"rgba(255,255,255,0.3)", fontSize:12, textAlign:"center", padding:"30px 0" }}>
              ← เลือก minion ก่อน
            </div>
          )}
        </div>
      </div>

      <div style={{ display:"flex", gap:"clamp(50px,2.5vw,44px)", marginBottom:"3vh", justifyContent:"center" }}>
        {MINIONS.map(m => (
          <div key={m.id} onClick={() => handleSelectMinion(m.id)} style={{
            textAlign:"center", cursor:"pointer",
            color: curMinionId===m.id ? "#c4b5fd" : slots.includes(m.id) ? "#86efac" : "rgba(255,255,255,0.45)",
            fontSize:"clamp(18px,1.2vw,13px)", fontWeight: curMinionId===m.id ? 700 : 400,
          }}>{m.name}</div>
        ))}
      </div>

      <div style={{ display:"flex", flexDirection:"column", alignItems:"center", gap:6, marginBottom:"1.2vh" }}>
        <div style={{ display:"flex", gap:6 }}>
          {slots.map((s, i) => (
            <div key={i} style={{
              width:7, height:7, borderRadius:"50%",
              background: s && strategyValues[i] ? "#86efac" : s ? "#fbbf24" : "rgba(255,255,255,0.2)",
            }}/>
          ))}
        </div>
        {statusMsg() && (
          <div style={{
            color:"#fbbf24", fontSize:12, letterSpacing:1,
            background:"rgba(251,191,36,0.1)", padding:"4px 14px",
            borderRadius:20, border:"1px solid rgba(251,191,36,0.3)",
          }}>⚠️ {statusMsg()}</div>
        )}
      </div>

      {slots[activeSlot] && strategyValues[activeSlot] && activeSlot < minionCount - 1 && (
        <button onClick={() => setActiveSlot(activeSlot + 1)} style={{
          marginBottom:"1vh", padding:"10px 36px", fontSize:14,
          fontFamily:"'Cinzel', serif", fontWeight:700, letterSpacing:"0.12em",
          background:"rgba(196,181,253,0.2)", border:"1.5px solid rgba(196,181,253,0.5)",
          borderRadius:40, color:"#c4b5fd", cursor:"pointer",
        }}>
          NEXT → Minion {activeSlot + 2}
        </button>
      )}

      <button onClick={handleStart} disabled={!allReady || starting} style={{
        marginBottom:"4vh", padding:"11px 44px", fontSize:14,
        fontFamily:"'Cinzel', serif", fontWeight:700, letterSpacing:"0.15em",
        background: allReady ? "rgba(255,255,255,0.15)" : "rgba(255,255,255,0.05)",
        border: allReady ? "1px solid rgba(255,255,255,0.35)" : "1px solid rgba(255,255,255,0.1)",
        borderRadius:40, color: allReady ? "#fff" : "rgba(255,255,255,0.3)",
        cursor: allReady ? "pointer" : "not-allowed",
      }}>
        {starting ? "กำลังสร้างเกม..." : allReady ? "GAME START →" : `ยังไม่ครบ (${filledSlots}/${minionCount} ตัว · ${filledStrategy}/${minionCount} strategy)`}
      </button>
    </div>
  );
}

function StatRow({ label, value, color="#fff" }) {
  return (
    <div style={{ marginBottom:9 }}>
      <div style={{ color:"rgba(255,255,255,0.55)", fontSize:11, marginBottom:3 }}>{label}</div>
      <div style={{
        padding:"5px 10px", borderRadius:8,
        background:"rgba(255,255,255,0.05)", border:"1px solid rgba(255,255,255,0.08)",
        color, fontSize:15, fontWeight:700, textAlign:"center",
      }}>{value}</div>
    </div>
  );
}