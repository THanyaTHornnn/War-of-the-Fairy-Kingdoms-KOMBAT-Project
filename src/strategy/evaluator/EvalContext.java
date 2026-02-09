package strategy.evaluator;

public interface EvalContext {

    // variables
    long getVar(String name);
    void setVar(String name, long value);

    // special variables (read-only)
    long getSpecialVar(String name);
    // "budget", "row", "col", "hp"

    // actions
    void move(int dir);   // 0–5 หรือ enum ที่ตกลงกัน
    void shoot(int dir);
    void done();
}
