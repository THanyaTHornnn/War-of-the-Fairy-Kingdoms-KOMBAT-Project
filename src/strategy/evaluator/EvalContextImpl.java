package strategy.evaluator;

import core.GameLogic;

import core.Minion;
import core.Position;

public class EvalContextImpl implements EvalContext {

    private final GameLogic gameLogic;
    private final Minion minion;
    private final VariableContext vars;
    private boolean done = false;
    private long budget;

    public EvalContextImpl(GameLogic gameLogic, Minion minion) {
        this.gameLogic = gameLogic;
        this.minion = minion;
        this.vars = new VariableContext(gameLogic.getSnapshot(), minion);
        this.budget = (long) gameLogic.getConfig().turnBudget;
    }



    @Override
    public long getVar(String name) {
        return vars.getVar(name);
    }
    @Override
    public void setVar(String name, long value) {
        vars.setVar(name, value);
    }

    @Override
    public boolean hasVar(String name) {
        return vars.hasVar(name);
    }

    @Override
    public long getSpecialVar(String name) {
        return vars.getVar(name);
    }

    @Override
    public boolean move(int dir) {
        if (done) return false;

        if (!hasBudget(1)) {
            done = true;
            return false;
        }

        boolean success = gameLogic.moveMinion(minion, dir);
        if (success) {
            consumeBudget(1);
            done = true;
        }

        return success;
    }

    @Override
    public boolean shoot(int dir, long dmg) {
        if (done) return false;

        long cost = dmg + 1;

        if (!hasBudget(cost)) {
            done = true;
            return false;
        }

        boolean success = gameLogic.shootMinion(minion, dir, dmg);
        if (success) {
            consumeBudget(cost);
            done = true;
        }

        return success;
    }

    @Override
    public long nearby(int dir) {
        return gameLogic.nearby(minion, dir);
    }



//    @Override
//    public long countAlly() {
//        return gameState.countAlly(minion);
//    }
//
//    @Override
//    public long countOpponent() {
//        return gameState.countOpponent(minion);
//    }

    @Override
    public long ally() {
        return gameLogic.findAlly(minion);
    }

    @Override
    public long opponent() {
        return gameLogic.findOpponent(minion);
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public void consumeBudget(long cost) {
        if (budget < cost) {
            done = true;
            return;
        }
        budget -= cost;
    }

    @Override
    public boolean hasBudget(long cost) {
        return budget >= cost;
    }

    @Override
    public long getBudget() {
        return budget;
    }


    @Override
    public void done() {
        throw new strategy.runtime.RuntimeTerminate("done");
    }
}
