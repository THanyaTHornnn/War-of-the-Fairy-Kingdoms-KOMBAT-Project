package strategy.evaluator;

public interface EvalContext {

    // variables
    long getVar(String name);
    void setVar(String name, long value);
    boolean hasVar(String name);
    // special variables (read-only)
    long getSpecialVar(String name);
    // "budget", "row", "col", "hp"

    // actions
    boolean move(int dir); // 0–5 หรือ enum ที่ตกลงกัน
    boolean shoot(int dir, long dmg);
    void done();

    long nearby(int dir);
    long ally();
    long opponent();


    boolean isDone();

    long getBudget();
    void consumeBudget(long cost);
    boolean hasBudget(long cost);

}
