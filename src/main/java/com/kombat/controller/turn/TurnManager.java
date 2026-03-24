package com.kombat.controller.turn;

import com.kombat.core.GameLogic;
import com.kombat.core.Minion;
import com.kombat.strategy.evaluator.EvalContext;
import com.kombat.strategy.evaluator.EvalContextImpl;
import com.kombat.strategy.evaluator.StrategyEvaluatorImpl;

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
    // รัน strategy เฉพาะ minion ของ playerId นี้เท่านั้น ตาม spec
//    public List<MinionLog> executeStrategies(String playerId) {
//        // เรียงจากเก่าสุด → ใหม่สุด ตาม spec
//        List<Minion> minions = new ArrayList<>(logic.getMinionsByOwner(playerId));
//        List<MinionLog> log  = new ArrayList<>();
//
//
//        for (Minion m : minions) {
//            if (!logic.getMinions().containsKey(m.getId())) continue;
//
//            if (m.getStrategyAST() == null || m.getStrategyAST().isEmpty()) {
//                log.add(new MinionLog(m.getId(), false, "No strategy assigned"));
//                continue;
//            }
//
//            try {
//                EvalContext ctx = new EvalContextImpl(logic, m);
//                evaluator.evaluate(m.getStrategyAST(), ctx);
//                if (!ctx.isDone()) {
//                    ctx.forceDone();   // ใช้ turn แม้ไม่ทำอะไร
//                }
//                log.add(new MinionLog(m.getId(), true, null));
//            } catch (Exception e) {
//                log.add(new MinionLog(m.getId(), false, e.getMessage()));
//            }
//        }
//
//        return log;
//    }
    public List<MinionLog> executeStrategies(String playerId) {
        List<Minion> minions = new ArrayList<>(logic.getMinionsByOwner(playerId));
        List<MinionLog> log = new ArrayList<>();

        System.out.println("=== executeStrategies for " + playerId + " ===");
        System.out.println("Minions count: " + minions.size());

        for (Minion m : minions) {
            System.out.println("Minion " + m.getId() + " at " + m.getPosition());
            System.out.println("Strategy AST: " + m.getStrategyAST());

            if (!logic.getMinions().containsKey(m.getId())) {
                System.out.println("Minion not in game map");
                continue;
            }

            if (m.getStrategyAST() == null || m.getStrategyAST().isEmpty()) {
                System.out.println("❌ No strategy for " + m.getId());
                log.add(new MinionLog(m.getId(), false, "No strategy assigned"));
                continue;
            }

            try {
                System.out.println("Evaluating strategy for " + m.getId());
                EvalContext ctx = new EvalContextImpl(logic, m);
                evaluator.evaluate(m.getStrategyAST(), ctx);
                System.out.println("Evaluation done, isDone=" + ctx.isDone());

                if (!ctx.isDone()) {
                    ctx.forceDone();
                }
                log.add(new MinionLog(m.getId(), true, null));
            } catch (Exception e) {
                System.out.println("❌ Error evaluating " + m.getId() + ": " + e.getMessage());
                e.printStackTrace();
                log.add(new MinionLog(m.getId(), false, e.getMessage()));
            }
        }

        return log;
    }

    // ── Log ───────────────────────────────────────────────────-
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