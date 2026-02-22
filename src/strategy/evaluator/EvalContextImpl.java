package strategy.evaluator;

import core.*;


public class EvalContextImpl implements EvalContext {

    private final GameLogic gameLogic;
    private final Minion minion;
    private final VariableContext vars;


    public EvalContextImpl(GameLogic gameLogic, Minion minion) {
        this.gameLogic = gameLogic;
        this.minion = minion;
        this.vars = new VariableContext(gameLogic.getSnapshot(), minion);

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
        return gameLogic.moveMioion(minion, dir);
    }

    @Override
    public boolean shoot(Direction dir, long dmg) {
        return GameRules.shoot(minion, dir, (int) dmg, gameState.getBoard());
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
