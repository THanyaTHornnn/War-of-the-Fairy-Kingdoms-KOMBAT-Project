package strategy.evaluator;

import gameState.GameRules;
import gameState.GameState;

import gameState.minnion.Minion;
import gameState.Direction;


public class EvalContextImpl implements EvalContext {

    private final GameState gameState;
    private final Minion minion;
    private final VariableContext vars;


    public EvalContextImpl(GameState gameState, Minion minion) {
        this.gameState = gameState;
        this.minion = minion;
        this.vars = new VariableContext(gameState, minion);

    }

    @Override
    public long getVar(String name) {
        return vars.get(name);
    }

    @Override
    public void setVar(String name, long value) {
        vars.set(name, value);
    }

    @Override
    public long getSpecialVar(String name) {
        return vars.getSpecial(name);
    }

    @Override
    public boolean move(Direction dir) {
        return GameRules.move(minion, dir, gameState.getBoard());
    }

    @Override
    public boolean shoot(Direction dir, long dmg) {
        return GameRules.shoot(minion, dir, dmg, gameState.getBoard());
    }

    @Override
    public int nearby() {
        return GameRules.hasNearbyOpponent(minion, gameState.getBoard()) ? 1 : 0;
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
    public int ally() {
        return GameRules.findAlly(minion, gameState.getBoard());
    }

    @Override
    public int opponent() {
        return GameRules.findOpponent(minion, gameState.getBoard());
    }


    @Override
    public void done() {
        throw new strategy.runtime.RuntimeTerminate("done");
    }
}
