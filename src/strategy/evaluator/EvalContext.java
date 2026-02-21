package strategy.evaluator;

import gameState.Direction;

public interface EvalContext {

    // variables
    long getVar(String name);
    void setVar(String name, long value);
    boolean hasVar(String name);
    // special variables (read-only)
    long getSpecialVar(String name);
    // "budget", "row", "col", "hp"

    // actions
    boolean move(Direction dir); // 0–5 หรือ enum ที่ตกลงกัน
    boolean shoot(Direction dir, long dmg);
    void done();

    long nearby(Direction dir);
    long ally();
    long opponent();


    boolean isDone();
}
