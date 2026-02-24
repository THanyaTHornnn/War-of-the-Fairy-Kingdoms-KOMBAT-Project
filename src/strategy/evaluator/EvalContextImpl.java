package strategy.evaluator;

import core.GameLogic;

import core.Minion;
import core.Player;
import core.Position;

public class EvalContextImpl implements EvalContext {

    private final GameLogic gameLogic;
    private final Minion minion;
    private final VariableContext vars;
    private boolean done = false;


    public EvalContextImpl(GameLogic gameLogic, Minion minion) {
        this.gameLogic = gameLogic;
        this.minion = minion;
        this.vars = new VariableContext(gameLogic.getSnapshot(), minion);
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

        // ไม่พอ budget → จบ turn ทันที
        if (!player().canAfford(1)) {
            done = true;
            return false;
        }

        // หัก budget เสมอ ไม่ว่าจะ move สำเร็จหรือไม่
        player().deductBudget(1);
        done = true; // move executed → จบ turn เสมอ

        Position newPos = minion.getPosition().move(dir);
        if (!newPos.isValid() || gameLogic.getMinionAt(newPos) != null) {
            return false; // no-op แต่จบแล้ว
        }
        minion.setPosition(newPos);
        return true;
    }


    @Override
    public boolean shoot(int dir, long dmg) {
        if (done) return false;

        long cost = dmg + 1;

        // ไม่พอ budget → no-op (ไม่จบ turn ด้วย ตาม spec)
        if (!player().canAfford(cost)) {
            return false;
        }

        // จ่าย budget เสมอ แม้ target ว่าง
        player().deductBudget(cost);
        done = true; // shoot executed → จบ turn

        Position targetPos = minion.getPosition().move(dir);
        if (!targetPos.isValid()) return true; // จ่ายแล้ว แต่ไม่มีผล

        Minion target = gameLogic.getMinionAt(targetPos);
        if (target == null) return true; // จ่ายแล้ว แต่ไม่มีผล

        // ยิงได้ทั้ง enemy และ ally (self destruct)
        target.takeDamage(dmg);
        if (target.isDead()) gameLogic.removeMinion(target.getId());
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
    public long random(long max) {
        return (long)(Math.random() * max);
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public void consumeBudget(long cost) {
        player().deductBudget(cost);
    }

    @Override
    public boolean hasBudget(long cost) {
        return player().canAfford(cost);
    }

    @Override
    public long getBudget() {
        return player().getBudgetFloor();
    }


    @Override
    public void done() {
        throw new strategy.runtime.RuntimeTerminate("done");
    }
}
