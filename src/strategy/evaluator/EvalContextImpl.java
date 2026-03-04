//package strategy.evaluator;
//
//import core.GameLogic;
//
//import core.Minion;
//import core.Player;
//import core.Position;
//
//public class EvalContextImpl implements EvalContext {
//
//    private final GameLogic gameLogic;
//    private final Minion minion;
//    private final VariableContext vars;
//    private boolean done = false;
//
//
//    public EvalContextImpl(GameLogic gameLogic, Minion minion) {
//        this.gameLogic = gameLogic;
//        this.minion = minion;
//        this.vars = new VariableContext(gameLogic.getSnapshot(), minion);
//    }
//
//    private Player player() {
//        return gameLogic.getPlayer(minion.getOwner().getId());
//    }
//
//    @Override
//    public long getVar(String name) {
//        return vars.getVar(name);
//    }
//    @Override
//    public void setVar(String name, long value) {
//        vars.setVar(name, value);
//    }
//
//    @Override
//    public boolean hasVar(String name) {
//        return vars.hasVar(name);
//    }
//
//    @Override
//    public long getSpecialVar(String name) {
//        return vars.getVar(name);
//    }
//
//    @Override
//    public boolean move(int dir) {
//        if (done) return false;
//
//        boolean success = gameLogic.move(minion, dir);
//
//        if (success) {
//            done = true;
//        }
//
//        return success;
//    }
//
//
//    @Override
//    public boolean shoot(int dir, long dmg) {
//        if (done) return false;
//
//        boolean success = gameLogic.shoot(minion, dir, dmg);
//
//        if (success) {
//            done = true;
//        }
//
//        return success;
//    }
//
//    @Override
//    public long nearby(int dir) {
//        return gameLogic.nearby(minion, dir);
//    }
//
//
//
////    @Override
////    public long countAlly() {
////        return gameState.countAlly(minion);
////    }
////
////    @Override
////    public long countOpponent() {
////        return gameState.countOpponent(minion);
////    }
//
//    @Override
//    public long ally() {
//        return gameLogic.findAlly(minion);
//    }
//
//    @Override
//    public long opponent() {
//        return gameLogic.findOpponent(minion);
//    }
//
//    @Override
//    public long random(long max) {
//        return (long)(Math.random() * max);
//    }
//
//    @Override
//    public boolean isDone() {
//        return done;
//    }
//
//    @Override
//    public void consumeBudget(long cost) {
//        player().deductBudget(cost);
//    }
//
//    @Override
//    public boolean hasBudget(long cost) {
//        return player().canAfford(cost);
//    }
//
//    @Override
//    public long getBudget() {
//        return player().getBudgetFloor();
//    }
//
//    @Override
//    public void forceDone() {
//        done = true;
//    }
//
//
//    @Override
//    public void done() {
//        throw new strategy.runtime.RuntimeTerminate("done");
//    }
//}
package strategy.evaluator;

import core.GameLogic;
import core.Minion;
import core.Player;

public class EvalContextImpl implements EvalContext {

    private final GameLogic gameLogic;
    private final Minion minion;
    private final VariableContext vars;
    private boolean done = false;

    public EvalContextImpl(GameLogic gameLogic, Minion minion) {
        this.gameLogic = gameLogic;
        this.minion    = minion;
        this.vars      = new VariableContext(gameLogic.getSnapshot(), minion);
    }

    private Player player() {
        return gameLogic.getPlayer(minion.getOwner().getId());
    }

    @Override
    public long getVar(String name) { return vars.getVar(name); }

    @Override
    public void setVar(String name, long value) { vars.setVar(name, value); }

    @Override
    public boolean hasVar(String name) { return vars.hasVar(name); }

    @Override
    public long getSpecialVar(String name) { return vars.getVar(name); }

    // ── move ─────────────────────────────────────────────────
    // ตามสเปค: move จ่าย 1 budget เสมอ
    // ถ้า budget ไม่พอ → จบ strategy (done)
    // ถ้า target occupied/นอกขอบ → no-op แต่ยังจ่าย budget และ strategy ดำเนินต่อ
    @Override
    public boolean move(int dir) {
        if (done) return false;

        // ตรวจ budget ก่อน — ถ้าไม่พอให้จบ strategy ทันที
        if (!player().canAfford(1)) {
            done = true;
            return false;
        }

        // GameLogic.move() จ่าย budget และพยายามขยับ
        // return true = ขยับสำเร็จ, false = no-op (ยังจ่ายแล้ว)
        boolean moved = gameLogic.move(minion, dir);

        // ไม่ set done ที่นี่ — strategy ดำเนินต่อได้
        return moved;
    }

    // ── shoot ────────────────────────────────────────────────
    // ตามสเปค: cost = expenditure + 1
    // ถ้า budget ไม่พอ → no-op (ไม่จ่าย ไม่จบ strategy)
    // ถ้า target ว่าง → จ่ายแต่ไม่มีผล strategy ดำเนินต่อ
    @Override
    public boolean shoot(int dir, long dmg) {
        if (done) return false;

        long cost = dmg + 1;
        if (!player().canAfford(cost)) {
            // ตามสเปค: ถ้าไม่พอ → no-op (ไม่จบ strategy)
            return false;
        }

        boolean shot = gameLogic.shoot(minion, dir, dmg);
        // ไม่ set done — strategy ดำเนินต่อ
        return shot;
    }

    @Override
    public long nearby(int dir) { return gameLogic.nearby(minion, dir); }

    @Override
    public long ally() { return gameLogic.findAlly(minion); }

    @Override
    public long opponent() { return gameLogic.findOpponent(minion); }

    @Override
    public long random(long max) { return (long)(Math.random() * max); }

    @Override
    public boolean isDone() { return done; }

    @Override
    public void consumeBudget(long cost) { player().deductBudget(cost); }

    @Override
    public boolean hasBudget(long cost) { return player().canAfford(cost); }

    @Override
    public long getBudget() { return player().getBudgetFloor(); }

    @Override
    public void forceDone() { done = true; }

    // done command →던지ง RuntimeTerminate ให้ evaluator จับ
    @Override
    public void done() {
        done = true;
        throw new strategy.runtime.RuntimeTerminate("done");
    }
}