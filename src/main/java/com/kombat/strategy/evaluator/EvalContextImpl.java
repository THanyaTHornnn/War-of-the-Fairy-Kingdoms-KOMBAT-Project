package com.kombat.strategy.evaluator;

import com.kombat.core.GameLogic;

import com.kombat.core.Minion;
import com.kombat.core.Player;

public class EvalContextImpl implements EvalContext {

    private final GameLogic gameLogic;
    private final Minion minion;
    private final VariableContext vars;
    private boolean done = false;


    public EvalContextImpl(GameLogic gameLogic, Minion minion) {
        this.gameLogic = gameLogic;
        this.minion = minion;
        this.vars = new VariableContext(gameLogic, minion);
    }

    private Player player() {
        return gameLogic.getPlayer(minion.getOwner().getId());
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
    public long getSpecialVar(String name) {
        return vars.getVar(name);
    }

    @Override
    public boolean move(int dir) {
        if (done) return false;
        // budget ไม่พอ → จบ strategy ทันที
        if (!player().canAfford(1)) {
            done = true;
            return false;
        }
        gameLogic.move(minion, dir); // จ่ายเงินใน move() เสมอ
        // ไม่ set done — strategy เดินต่อได้
        return true;
    }

    @Override
    public boolean shoot(int dir, long dmg) {
        if (done) return false;
        // budget ไม่พอ → no-op, strategy เดินต่อ
        if (!player().canAfford(dmg + 1)) return false;
        gameLogic.shoot(minion, dir, dmg);
        // ไม่ set done — strategy เดินต่อได้
        return true;
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
    public long getBudget() {
        return player().getBudgetFloor();
    }

    @Override
    public void forceDone() {
        done = true;
    }


    @Override
    public void done() {
        throw new com.kombat.strategy.runtime.RuntimeTerminate("done");
    }
}
