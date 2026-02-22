package strategy.evaluator;


public interface EvalContext {

    // variables
    long getVar(String name);
    void setVar(String name, long value);

    // special variables (read-only)
    long getSpecialVar(String name);
    // "budget", "row", "col", "hp"

    // actions
    boolean move(int dir); // 0–5 หรือ enum ที่ตกลงกัน
    boolean shoot(int dir, long dmg);
    void done();

    int ally();
    int opponent();


    long nearby(int dir);
}
