package com.kombat.controller.turn;

import com.kombat.core.GameLogic;
import com.kombat.core.Minion;
import com.kombat.strategy.evaluator.EvalContext;
import com.kombat.strategy.evaluator.EvalContextImpl;
import com.kombat.strategy.evaluator.StrategyEvaluatorImpl;

import java.util.ArrayList;
import java.util.List;


public class TurnManager {
    private final GameLogic logic;
    private final StrategyEvaluatorImpl evaluator;

    public TurnManager(GameLogic logic) {
        this.logic     = logic;
        this.evaluator = new StrategyEvaluatorImpl();
    }


    public void applyBudget(String playerId) {
        logic.applyTurnBudget(playerId);
    }

    public boolean purchaseHex(String playerId, int row, int col) {
        return logic.purchaseHex(playerId, row, col);
    }


    public boolean spawnMinion(String playerId, Minion minion) {
        return logic.spawnMinion(playerId, minion);
    }



    public List<MinionLog> executeStrategies(String playerId) {
        List<Minion> minions = new ArrayList<>(logic.getMinionsByOwner(playerId));
        List<MinionLog> log  = new ArrayList<>();


        for (Minion m : minions) {
            if (!logic.getMinions().containsKey(m.getId())) continue;

            if (m.getStrategyAST() == null || m.getStrategyAST().isEmpty()) {
                log.add(new MinionLog(m.getId(), false, "No strategy assigned"));
                continue;
            }

            try {
                EvalContext ctx = new EvalContextImpl(logic, m);
                evaluator.evaluate(m.getStrategyAST(), ctx);
                if (!ctx.isDone()) {
                    ctx.forceDone();
                }
                log.add(new MinionLog(m.getId(), true, null));
            } catch (Exception e) {
                log.add(new MinionLog(m.getId(), false, e.getMessage()));
            }
        }

        return log;
    }

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