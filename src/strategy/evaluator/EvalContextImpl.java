package strategy.evaluator;

import gameState.GameRules;
import gameState.GameState;

import gameState.minnion.Minion;
import gameState.Direction;
import strategy.runtime.RuntimeError;

public class EvalContextImpl implements EvalContext {

    private final GameState gameState;
    private final Minion minion;
    private final VariableContext vars;
    private boolean done = false;
    private long budget;

    public EvalContextImpl(GameState gameState, Minion minion) {
        this.gameState = gameState;
        this.minion = minion;
        this.vars = new VariableContext(gameState, minion);
        this.budget = (long) gameState.getConfig().turnBudget;
    }



    @Override
    public long getVar(String name) {
        if (!vars.hasVar(name)) {
            throw new RuntimeError("Undefined variable: " + name);
        }
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
        return vars.getSpecial(name);
    }

    @Override
    public boolean move(Direction dir) {
        if (done) return false;

        boolean success = GameRules.move(minion, dir, gameState.getBoard());
        if (success) done = true;

        return success;
    }

    @Override
    public boolean shoot(Direction dir, long dmg) {
        return GameRules.shoot(minion, dir, (int) dmg, gameState.getBoard());
    }

    @Override
    public long nearby(Direction dir) {
        return GameRules.hasNearbyOpponent(minion,dir, gameState.getBoard()) ? 1L : 0L;
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
        return GameRules.findAlly(minion, gameState.getBoard());
    }

    @Override
    public long opponent() {
        return GameRules.findOpponent(minion, gameState.getBoard());
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
        this.done = true;
    }

    @Override
    public boolean isDone() {
        return done;
    }
}
