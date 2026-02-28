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
    public  void applyInterest(String playerId) {
        Player p = logic.getPlayer(playerId);
        logic.applyInterest(p);
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

            if (!logic.getMinions().containsKey(m.getId())) {
                System.out.println("❌ not found in logic map");
                continue;
            }

            if (m.getStrategyAST() == null || m.getStrategyAST().isEmpty()) {
                System.out.println("❌ ไม่มี strategy");
                log.add(new MinionLog(m.getId(), false, "No strategy assigned"));
                continue;
            }

            System.out.println("✓ มี strategy (" + m.getStrategyAST().size() + " statements)");

            try {
                EvalContext ctx = new EvalContextImpl(logic, m);

                System.out.println("▶ executing strategy...");
                evaluator.evaluate(m.getStrategyAST(), ctx);

                System.out.println("✔ strategy executed");
                log.add(new MinionLog(m.getId(), true, null));

            } catch (Exception e) {
                System.out.println("💥 ERROR: " + e.getMessage());
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