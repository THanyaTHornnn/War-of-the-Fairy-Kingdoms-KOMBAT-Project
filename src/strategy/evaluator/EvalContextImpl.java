package strategy.evaluator;

import gameState.GameRules;
import gameState.GameState;

import gameState.minnion.Minion;
import gameState.Direction;
import strategy.runtime.RuntimeError;
import strategy.evaluator.EvalContext;

public class EvalContextImpl implements EvalContext {

    private final GameState gameState;
    private final Minion minion;
    private final VariableContext vars;
    private boolean done = false;

    public EvalContextImpl(GameState gameState, Minion minion) {
        this.gameState = gameState;
        this.minion = minion;
        this.vars = new VariableContext(gameState, minion);

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
    public void move(Direction dir) {
        GameRules.move(minion, dir, gameState.getBoard());
    }

    @Override
    public void shoot(Direction dir, long dmg) {
        GameRules.shoot(minion, dir, (int) dmg, gameState.getBoard());
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
    public void done() {
        this.done = true;
    }

    @Override
    public boolean isDone() {
        return done;
    }
}
