package controller.turn;

import core.*;
import strategy.evaluator.*;
import strategy.runtime.*;
import java.util.ArrayList;
import java.util.List;

// ประสาน Evaluator + GameLogic ไม่แก้ state เอง
public class TurnManager {
    private final GameLogic logic;
    private final StrategyEvaluatorImpl evaluator;

    public TurnManager(GameLogic logic) {
        this.logic     = logic;
        this.evaluator = new StrategyEvaluatorImpl();
    }

    // ── Step 1: Apply budget ──────────────────────────────────
    public void applyBudget(String playerId) {
        logic.applyTurnBudget(playerId);
    }

    // ── Step 2: Purchase hex (optional) ──────────────────────
    public boolean purchaseHex(String playerId, int row, int col) {
        return logic.purchaseHex(playerId, row, col);
    }

    // ── Step 3: Spawn minion (optional) ──────────────────────
    public boolean spawnMinion(String playerId, Minion minion) {
        return logic.spawnMinion(playerId, minion);
    }

    // ── Step 4: Execute strategies ────────────────────────────
    public List<MinionLog> executeStrategies(String playerId) {
        List<Minion> minions = logic.getMinionsByOwner(playerId);
        List<MinionLog> log  = new ArrayList<>();

        for (Minion m : minions) {
            if (!logic.getMinions().containsKey(m.getId())) continue;
            try {
                // สร้าง context จาก GameState snapshot ของเรา
                GameState snap = logic.getSnapshot();
                EvalContext ctx = new EvalContextImpl(logic, m);
                evaluator.evaluate(m.getStrategyAST(), ctx);
                log.add(new MinionLog(m.getId(), true, null));
            } catch (Exception e) {
                log.add(new MinionLog(m.getId(), false, e.getMessage()));
            }
        }

        return log;
    }

    // ── Log ───────────────────────────────────────────────────
    public static class MinionLog {
        public final String minionId;
        public final boolean success;
        public final String error;

        public MinionLog(String minionId, boolean success, String error) {
            this.minionId = minionId;
            this.success  = success;
            this.error    = error;
        }
    }
}