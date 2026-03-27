import { useState , useEffect} from "react";
import BackButton from "../components/BackButton";
import { useGame, MINIONS, BASE_HP } from "../context/GameContext";
import MinionCharacter3D from "../components/MinionCharacter3D";
import { PRESET_STRATEGIES } from "../utils/strategyValidator";

async function checkStrategy(code) {
  try {
    const res = await fetch("http://localhost:8080/api/validate-strategy", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ strategy: code }),
    });
    const data = await res.json();
    return {
      valid:   data.valid ?? false,
      message: data.valid ? "THE  STRATEGY IS  VALID !" : "THE  STRATEGY IS  VALID",
    };
  } catch {
    return { valid: true, message: " (Cannot validate — will validate at start)" };
  }
}

function StrategyDetailPage({ preset, onSelect, onBack }) {
  return (
    <div style={{
      width: "100vw", height: "100vh", display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      backgroundImage: "url('/public/selectCharacter.jpg')",
      backgroundSize: "cover", backgroundPosition: "center",
      fontFamily: "'Emilys Candy', serif", position: "relative",
    }}>
      <BackButton onClick={onBack} />
      <div style={{
        background: "rgba(10,5,30,0.80)", backdropFilter: "blur(16px)",
        border: "1px solid rgba(196,181,253,0.35)", borderRadius: 24,
        padding: "44px 52px", maxWidth: 480, width: "90%",
        boxShadow: "0 8px 28px rgba(109,40,217,0.3)",
      }}>
        <div style={{ color: "#f4f2fc", fontSize: 22, letterSpacing: 3, marginBottom: 6 }}>PRESET STRATEGY</div>
        <h2 style={{ color: "#e2d9f3", fontSize: 20, fontWeight: 900, textShadow: "0 0 20px #160d3b, 0 2px 6px rgba(0,0,0,0.9)", marginBottom: 16 }}>
          {preset.name}
        </h2>
        <pre style={{
          background: "rgba(0,0,0,0.5)", border: "1px solid rgba(196,181,253,0.3)",
          borderRadius: 12, padding: "20px 24px", color: "#a5f3fc", fontSize: 11,
          fontFamily: "monospace", whiteSpace: "pre-wrap", lineHeight: 1.7, marginBottom: 12, maxHeight: "40vh", overflowY: "auto",
        }}>{preset.code}</pre>
        <div style={{
          background: "rgba(196,181,253,0.08)", border: "1px solid rgba(196,181,253,0.2)",
          borderRadius: 10, padding: "8px 12px", marginBottom: 16,
          color: "rgba(255,255,255,0.65)", fontSize: 12, lineHeight: 1.6,
        }}>{preset.description || "Preset Strategy"}</div>
        <button onClick={() => onSelect(preset)} style={{
          width: "100%", padding: "18px", borderRadius: 30,
          border: "1.5px solid rgba(196,181,253,0.6)", background: "rgba(109,40,217,0.35)",
          color: "#e9d5ff", cursor: "pointer", fontFamily: "'Emilys Candy', serif", fontSize: 18, fontWeight: 700,
          textShadow: "0 0 8px #1a1336",
        }}>S E L E C T</button>
      </div>
    </div>
  );
}

function CustomStrategyPage({ onSave, onBack }) {
  const [code, setCode]               = useState("");
  const [checkResult, setCheckResult] = useState(null);
  const [loading, setLoading]         = useState(false);

  const handleCheck = async () => {
    setLoading(true);
    const r = await checkStrategy(code);
    setCheckResult(r);
    setLoading(false);
  };

  const handleSave = async () => {
    setLoading(true);
    const r = await checkStrategy(code);
    setCheckResult(r);
    setLoading(false);
    if (r.valid) onSave(code);
  };

  return (
    <div style={{
      width: "100vw", height: "100vh", display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      backgroundImage: "url('/public/selectCharacter.jpg')",
      backgroundSize: "cover", backgroundPosition: "center",
      fontFamily: "'Emilys Candy', serif", position: "relative",
    }}>
      <BackButton onClick={onBack} />
      <div style={{
        background: "rgba(10,5,30,0.80)", backdropFilter: "blur(16px)",
        border: "1px solid rgba(196,181,253,0.35)", borderRadius: 24,
        padding: "50px 24px", maxWidth: 520, width: "90%",
        boxShadow: "0 4px 24px rgba(109,40,217,0.3)",
      }}>
        <div style={{ color: "#c4b5fd", fontSize: 22, letterSpacing: 3, marginBottom: 6 }}>CUSTOM STRATEGY</div>
        <h2 style={{ color: "#e2d9f3", fontSize: 26, fontWeight: 900, textShadow: "0 0 20px #0e091f, 0 2px 6px rgba(0,0,0,0.9)", marginBottom: 20 }}>
          SET UP YOUR  STRATEGY
        </h2>
        <textarea
          value={code}
          onChange={e => { setCode(e.target.value); setCheckResult(null); }}
          placeholder="your strategy…"
          rows={8}
          style={{
            width: "100%", padding: "14px 14px 150px 14px", borderRadius: 12,
            border: "1px solid rgba(196,181,253,0.3)", background: "rgba(8,4,24,0.7)",
            color: "#fff", fontSize: 16, fontFamily: "monospace", resize: "none",
            textAlign: "left", verticalAlign: "top",
            outline: "none", boxSizing: "border-box", marginBottom: 20,
          }}
        />
        {checkResult && (
          <div style={{
            padding: "8px 14px", borderRadius: 10, marginBottom: 12, fontSize: 13,
            background: checkResult.valid ? "rgba(34,197,94,0.15)" : "rgba(239,68,68,0.15)",
            border: `1px solid ${checkResult.valid ? "#22c55e" : "#ef4444"}`,
            color: checkResult.valid ? "#86efac" : "#fca5a5",
          }}>{checkResult.message}</div>
        )}
        <div style={{ display: "flex", gap: 10 }}>
          <button onClick={handleCheck} disabled={loading} style={{
            flex: 1, padding: "12px", borderRadius: 20,
            border: "1px solid rgba(196,181,253,0.5)", background: "rgba(109,40,217,0.25)",
            color: "#e9d5ff", cursor: "pointer", fontFamily: "'Emilys Candy', serif", fontSize: 18, fontWeight: 700,
          }}>{loading ? "…" : "CHECK "}</button>
          <button onClick={handleSave} disabled={loading} style={{
            flex: 1, padding: "12px", borderRadius: 20,
            border: "1.5px solid rgba(134,239,172,0.6)", background: "rgba(134,239,172,0.15)",
            color: "#86efac", cursor: "pointer", fontFamily: "'Emilys Candy', serif", fontSize: 18, fontWeight: 700,
          }}>{loading ? "…" : "SAVE "}</button>
        </div>
      </div>
    </div>
  );
}

function StrategyPanel({ value, onViewPreset, onViewCustom }) {
  const [panel, setPanel] = useState(null);

  const label = () => {
    if (!value) return null;
    const found = PRESET_STRATEGIES.find(p => p.code === value);
    return found ? found.name : "Custom";
  };

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 6 }}>
        <div style={{ color: "rgba(200,185,255,0.8)", fontSize: 11, letterSpacing: 1 }}>STRATEGY</div>
        {label() && (
          <div style={{ color: "#86efac", fontSize: 10, background: "rgba(134,239,172,0.1)", padding: "2px 8px", borderRadius: 10 }}>
            Correct {label()}
          </div>
        )}
      </div>
      {panel === null && (
        <button onClick={() => setPanel("list")} style={{
          width: "100%", padding: "7px 10px", borderRadius: 10,
          border: "1px solid rgba(196,181,253,0.4)", background: "rgba(109,40,217,0.25)",
          color: "#e9d5ff", cursor: "pointer", fontFamily: "'Cinzel', serif", fontSize: 11,
        }}>
          {value ? "Change ▾" : "Set Strategy ▾"}
        </button>
      )}
      {panel === "list" && (
        <div style={{ display: "flex", flexDirection: "column", gap: 5 }}>
          {PRESET_STRATEGIES.map(ps => (
            <button key={ps.id} onClick={() => { onViewPreset(ps); setPanel(null); }} style={{
              padding: "7px 10px", borderRadius: 9, textAlign: "left",
              border: "1px solid rgba(255,255,255,0.12)", background: "rgba(8,4,24,0.5)",
              color: "#e9d5ff", cursor: "pointer", fontFamily: "'Cinzel', serif", fontSize: 11,
              display: "flex", justifyContent: "space-between",
            }}>
              <span>{ps.name}</span><span style={{ opacity: 0.4 }}>›</span>
            </button>
          ))}
          <button onClick={() => { onViewCustom(); setPanel(null); }} style={{
            padding: "7px 10px", borderRadius: 9, textAlign: "left",
            border: "1px solid rgba(196,181,253,0.3)", background: "rgba(109,40,217,0.2)",
            color: "#e9d5ff", cursor: "pointer", fontFamily: "'Cinzel', serif", fontSize: 11,
            display: "flex", justifyContent: "space-between",
          }}>
            <span>Custom</span><span>✏️</span>
          </button>
          <button onClick={() => setPanel(null)} style={{
            padding: "4px", border: "none", background: "transparent",
            color: "rgba(200,185,255,0.4)", cursor: "pointer", fontSize: 11,
          }}>✕ Cancle</button>
        </div>
      )}
    </div>
  );
}

export default function CollectionScreen({ onNext, onBack }) {
  const { gameState, updatePlayer, send, setMessageHandler } = useGame();

  const minionCount = gameState.minionCount || 1;
  const mode        = gameState.mode || "pvp";

  const [slots,          setSlots]          = useState(() => Array(minionCount).fill(null));
  const [activeSlot,     setActiveSlot]     = useState(0);
  const [defenseValues,  setDefenseValues]  = useState(() => Array(minionCount).fill(null).map(() => ({})));
  const [strategyValues, setStrategyValues] = useState(() => Array(minionCount).fill(""));
  const [viewingPreset,  setViewingPreset]  = useState(null);
  const [viewingCustom,  setViewingCustom]  = useState(false);
  const [starting,       setStarting]       = useState(false);
  const [pendingStart,   setPendingStart]   = useState(null);

  const curMinionId    = slots[activeSlot];
  const curMinion      = MINIONS.find(m => m.id === curMinionId);
  const filledSlots    = slots.filter(s => s !== null).length;
  const filledStrategy = strategyValues.filter(s => s !== "").length;
  const allReady       = filledSlots === minionCount && filledStrategy === minionCount;

  useEffect(() => {
    if (!pendingStart) return;
    setMessageHandler((msg) => {
      if (msg.event === "room_created") {
        send("create", { mode: pendingStart.mode, minionConfigs: pendingStart.minionConfigs });
        setPendingStart(null);
        setMessageHandler(null);
        onNext("game");
      }
      if (msg.event === "room_error") {
        alert("Failed to create room: " + (msg.data?.message || ""));
        setStarting(false);
        setPendingStart(null);
        setMessageHandler(null);
      }
    });
    return () => setMessageHandler(null);
  }, [pendingStart]);

  const statusMsg = () => {
    if (filledSlots < minionCount) return `Select minions not complete (${filledSlots}/${minionCount})`;
    if (filledStrategy < minionCount) return `Not all strategies set (${filledStrategy}/${minionCount})`;
    return null;
  };

  const handleSelectMinion = (minionId) => {
    if (slots.some((s, i) => s === minionId && i !== activeSlot)) return;
    setSlots(prev => { const n = [...prev]; n[activeSlot] = minionId; return n; });
  };

  const handleDefenseChange = (minionId, val) => {
    setDefenseValues(prev => {
      const n = [...prev];
      n[activeSlot] = { ...n[activeSlot], [minionId]: Number(val) };
      return n;
    });
  };

  const handleStrategyChange = (val) => {
    setStrategyValues(prev => { const n = [...prev]; n[activeSlot] = val; return n; });
  };

  const handleStart = () => {
    setStarting(true);
    try {
      const minionConfigs = slots.map((minionId, i) => ({
        minionId,
        defense:  defenseValues[i]?.[minionId] ?? 10,
        strategy: strategyValues[i] || "done",
      }));
      updatePlayer(0, {
        selectedMinions: slots,
        minionDefense:   defenseValues[activeSlot],
        strategy:        strategyValues[0] || "done",
        minionConfigs,
      });
      if (mode === "pvp") {
        send("create", { mode: "PVP", minionConfigs });
        onNext("waitingRoom");
      } else {
        send("create-room");
        setPendingStart({ mode: mode.toUpperCase(), minionConfigs });
      }
    } catch (e) {
      alert("An error occurred: " + e.message);
      setStarting(false);
    }
  };

  if (viewingCustom) return (
    <CustomStrategyPage
      onSave={(code) => { handleStrategyChange(code); setViewingCustom(false); }}
      onBack={() => setViewingCustom(false)}
    />
  );
  if (viewingPreset) return (
    <StrategyDetailPage
      preset={viewingPreset}
      onSelect={(p) => { handleStrategyChange(p.code); setViewingPreset(null); }}
      onBack={() => setViewingPreset(null)}
    />
  );

  return (
    <div style={{
      width: "100vw", height: "100vh", display: "flex", flexDirection: "column",
      alignItems: "center",
      backgroundImage: "url('/public/selectCharacter.jpg')",
      backgroundSize: "cover", backgroundPosition: "center",
      fontFamily: "'Emilys Candy', serif", position: "relative", overflow: "hidden", paddingTop: "2.5vh",
    }}>
      <BackButton onClick={onBack} />

      <h1 style={{
        fontSize: "clamp(18px, 2.8vw, 38px)", color: "#e2d9f3",
        textShadow: "0 0 20px #c4b5fd, 0 2px 8px rgba(0,0,0,1), 0 4px 16px rgba(0,0,0,0.8)",
        marginBottom: "1vh", letterSpacing: "0.1em", fontWeight: 700,
      }}>COLLECTION OF MINION</h1>

      <div style={{ display: "flex", gap: 8, marginBottom: "1.5vh" }}>
        {slots.map((s, i) => (
          <button key={i} onClick={() => setActiveSlot(i)} style={{
            padding: "9px 20px", borderRadius: 20, cursor: "pointer",
            fontFamily: "'Cinzel', serif", fontSize: 12,
            border: activeSlot === i ? "2px solid #c4b5fd" : "1px solid rgba(196,181,253,0.4)",
            background: activeSlot === i ? "rgba(109,40,217,0.55)" : "rgba(10,5,30,0.60)",
            color: s ? "#e9d5ff" : "rgba(200,185,255,0.75)",
            backdropFilter: "blur(8px)",
            boxShadow: activeSlot === i ? "0 0 14px rgba(139,92,246,0.5)" : "0 2px 8px rgba(0,0,0,0.4)",
            textShadow: "0 1px 4px rgba(0,0,0,0.9)",
          }}>
            {s ? `Minion ${i+1} ` : `Minion ${i+1}`}
          </button>
        ))}
      </div>

      <div style={{
        display: "flex", alignItems: "center", justifyContent: "flex-start",
        gap: "2vw", flex: 1, width: "100%", padding: "0 3vw", overflow: "hidden",
justifyContent: "space-between",
      }}>

        <div style={{
          display: "flex", flexDirection: "column", gap: 9,
          flexShrink: 0, alignSelf: "stretch", justifyContent: "center",
          background: "rgba(0, 0, 0, 0.55)",
          backdropFilter: "blur(12px)",
          border: "1px solid rgba(196,181,253,0.18)",
          borderRadius: 20,
          padding: "14px 10px",
          boxShadow: "0 4px 20px rgba(0,0,0,0.4)",
        }}>
          <div style={{ color: "rgba(200,185,255,0.7)", fontSize: 9, letterSpacing: 2, textAlign: "center", marginBottom: 2 }}>SELECT</div>
          {MINIONS.map(m => {
            const isSelected = curMinionId === m.id;
            const usedOther  = slots.some((s, i) => s === m.id && i !== activeSlot);
            return (
              <button key={m.id} onClick={() => handleSelectMinion(m.id)} disabled={usedOther} style={{
                width: 100, height: 100, borderRadius: "50%",
                border: isSelected ? "2px solid #c4b5fd" : usedOther ? "1px solid rgba(255,255,255,0.08)" : "1px solid rgba(196,181,253,0.25)",
                background: isSelected ? "rgba(109,40,217,0.45)" : usedOther ? "rgba(0,0,0,0.2)" : "rgba(20,10,50,0.35)",
                cursor: usedOther ? "not-allowed" : "pointer",
                opacity: usedOther ? 0.3 : 1,
                display: "flex", alignItems: "center", justifyContent: "center", transition: "all 0.2s",
                boxShadow: isSelected ? "0 0 18px rgba(196,181,253,0.65)" : "none",
              }}>
                <MinionCharacter3D minionId={m.id} size={81} spin={false} selected={isSelected} rounded={true} />
              </button>
            );
          })}
        </div>

        <div style={{ textAlign: "center", flexShrink: 0 }}>
          {curMinionId ? (
            <>
              <MinionCharacter3D minionId={curMinionId} size={500} spin={true} selected={true} canDrag={true} rounded={false} />
              <div style={{
                color: "#e2d9f3", fontSize: "clamp(14px,1.8vw,24px)", fontWeight: 700, marginTop: 10,
                textShadow: "0 0 20px #c4b5fd, 0 2px 8px rgba(0,0,0,1), 0 4px 16px rgba(0,0,0,0.8)",
              }}>
                {curMinion?.name}
              </div>
            </>
          ) : (
            <div style={{
              width: 140, height: 140, borderRadius: "50%",
              border: "2px dashed rgba(196,181,253,0.3)",
              display: "flex", alignItems: "center", justifyContent: "center",
              color: "rgba(200,185,255,0.5)", fontSize: 12,
            }}> SELECT MINION</div>
          )}
        </div>

        <div style={{
          background: "rgba(10,5,30,0.75)", backdropFilter: "blur(16px)",
          border: "1px solid rgba(196,181,253,0.25)", borderRadius: 20,
          padding: "38px 40px", minWidth: 220, maxWidth: 260, flexShrink: 0,
          boxShadow: "0 4px 24px rgba(0,0,0,0.4)",
        }}>
          {curMinionId ? (
            <>
              <div style={{ color: "#c4b5fd", fontSize: 35, marginBottom: 10, letterSpacing: 2, textShadow: "0 0 10px #c4b5fd" }}>STATS</div>
              <StatRow label="HP" value={BASE_HP} color="#22c55e" />
              <div style={{ marginBottom: 12 }}>
                <div style={{ color: "rgba(200,185,255,0.7)", fontSize: 11, marginBottom: 4 }}>DEF</div>
                <input
                  type="number" min={0} max={100}
                  value={defenseValues[activeSlot]?.[curMinionId] ?? 10}
                  onChange={e => handleDefenseChange(curMinionId, e.target.value)}
                  style={{
                    width: "100%", padding: "7px 15px", borderRadius: 9,
                    border: "1px solid rgba(196,181,253,0.5)", background: "rgba(109,40,217,0.2)",
                    color: "#fff", fontSize: 20, fontWeight: 700, outline: "none",
                    fontFamily: "monospace", textAlign: "center",
                  }}
                />
              </div>
              <div style={{ borderTop: "1px solid rgba(196,181,253,0.15)", paddingTop: 10 }}>
                <StrategyPanel
                  value={strategyValues[activeSlot]}
                  onViewPreset={setViewingPreset}
                  onViewCustom={() => setViewingCustom(true)}
                />
              </div>
            </>
          ) : (
            <div style={{ color: "rgba(200,185,255,0.4)", fontSize: 12, textAlign: "center", padding: "30px 0" }}>
              ← Select a minion first
            </div>
          )}
        </div>
      </div>

      <div style={{ display: "flex", gap: "clamp(8px,1.5vw,20px)", marginBottom: "3vh", justifyContent: "center" }}>
        {MINIONS.map(m => (
          <div
            key={m.id}
            onClick={() => handleSelectMinion(m.id)}
            style={{
              textAlign: "center", cursor: "pointer",
              color: curMinionId === m.id ? "#e9d5ff" : slots.includes(m.id) ? "#86efac" : "rgba(220,210,255,0.9)",
              fontSize: "clamp(12px,1.2vw,15px)",
              fontWeight: curMinionId === m.id ? 700 : 400,
              background: curMinionId === m.id ? "rgba(109,40,217,0.55)" : "rgba(10,5,30,0.55)",
              backdropFilter: "blur(8px)",
              border: curMinionId === m.id ? "1px solid rgba(196,181,253,0.5)" : "1px solid rgba(196,181,253,0.18)",
              borderRadius: 10,
              padding: "8px 16px",
              boxShadow: curMinionId === m.id ? "0 0 12px rgba(139,92,246,0.4)" : "0 2px 6px rgba(0,0,0,0.4)",
            }}
          >{m.name}</div>
        ))}
      </div>

      <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 6, marginBottom: "1.2vh" }}>
        <div style={{ display: "flex", gap: 6 }}>
          {slots.map((s, i) => (
            <div key={i} style={{
              width: 7, height: 7, borderRadius: "50%",
              background: s && strategyValues[i] ? "#86efac" : s ? "#fbbf24" : "rgba(255,255,255,0.2)",
              boxShadow: s && strategyValues[i] ? "0 0 6px #86efac" : s ? "0 0 6px #fbbf24" : "none",
            }} />
          ))}
        </div>
        {statusMsg() && (
          <div style={{
            color: "#fbbf24", fontSize: 17, letterSpacing: 1,
            background: "rgba(10,5,30,0.65)",
            backdropFilter: "blur(8px)",
            padding: "5px 16px", borderRadius: 20,
            border: "1px solid rgba(251,191,36,0.4)",
            textShadow: "0 1px 4px rgba(0,0,0,0.9)",
            boxShadow: "0 2px 8px rgba(0,0,0,0.4)",
          }}> {statusMsg()}</div>
        )}
      </div>

      {slots[activeSlot] && strategyValues[activeSlot] && activeSlot < minionCount - 1 && (
        <button onClick={() => setActiveSlot(activeSlot + 1)} style={{
          marginBottom: "1vh", padding: "10px 36px", fontSize: 14,
          fontFamily: "'Emilys Candy', serif", fontWeight: 700, letterSpacing: "0.12em",
          background: "rgba(109,40,217,0.45)", backdropFilter: "blur(8px)",
          border: "1.5px solid rgba(196,181,253,0.5)",
          borderRadius: 40, color: "#e9d5ff", cursor: "pointer",
          textShadow: "0 0 8px #c4b5fd",
          boxShadow: "0 0 18px rgba(139,92,246,0.35), 0 3px 10px rgba(0,0,0,0.5)",
        }}>
          NEXT → Minion {activeSlot + 2}
        </button>
      )}

      <button onClick={handleStart} disabled={!allReady || starting} style={{
        marginBottom: "4vh", padding: "11px 44px", fontSize: 20,
        fontFamily: "'Emilys Candy', serif", fontWeight: 700, letterSpacing: "0.15em",
        background: allReady ? "rgba(109,40,217,0.50)" : "rgba(10,5,30,0.50)",
        backdropFilter: "blur(8px)",
        border: allReady ? "1px solid rgba(196,181,253,0.55)" : "1px solid rgba(255,255,255,0.1)",
        borderRadius: 40,
        color: allReady ? "#fff" : "rgba(200,185,255,0.35)",
        cursor: allReady ? "pointer" : "not-allowed",
        textShadow: allReady ? "0 1px 6px rgba(0,0,0,0.9)" : "none",
        boxShadow: allReady ? "0 0 24px rgba(139,92,246,0.4), 0 4px 16px rgba(0,0,0,0.5)" : "0 2px 8px rgba(0,0,0,0.4)",
      }}>
        {starting ? "Creating game..." : allReady
          ? "GAME START →"
          : `Not complete (${filledSlots}/${minionCount} minions · ${filledStrategy}/${minionCount} strategies)`}
      </button>
    </div>
  );
}

function StatRow({ label, value, color = "#fff" }) {
  return (
    <div style={{ marginBottom: 9 }}>
      <div style={{ color: "rgba(200,185,255,0.7)", fontSize: 11, marginBottom: 3 }}>{label}</div>
      <div style={{
        padding: "5px 10px", borderRadius: 8,
        background: "rgba(8,4,24,0.5)", border: "1px solid rgba(196,181,253,0.15)",
        color, fontSize: 15, fontWeight: 700, textAlign: "center",
        textShadow: `0 0 8px ${color}`,
      }}>{value}</div>
    </div>
  );
}