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
    public boolean move(int dir) {
        return gameLogic.moveMinion(minion, dir);
    }

    @Override
    public boolean shoot(int dir, long dmg) {
        return gameLogic.shootMinion(minion, dir, dmg);
    }

    @Override
    public long nearby(int dir) {
        return gameLogic.nearbyMinion(minion, dir) ;
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
        return gameLogic.findAlly(minion);
    }

    @Override
    public int opponent() {
        return gameLogic.findOpponent(minion);
    }


    @Override
    public void done() {
        throw new strategy.runtime.RuntimeTerminate("done");
    }
}
