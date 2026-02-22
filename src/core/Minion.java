package core;

import strategy.ast.Stmt;
import strategy.ast.*;
import strategy.ast.expr.*;
import java.util.List;

public class Minion {
    private final String id;
    private final String kind;       // "A","B","C","D","E"
    private final Player owner;
    private final int defense;
    private long hp;
    private Position position;
    private List<Stmt> strategyAST;
    private int spawnTurn;

    // ── Private constructor ───────────────────────────────────
    private Minion(String id, String kind, Player owner,
                   Position pos, long hp, int defense) {
        this.id = id;
        this.kind = kind;
        this.owner = owner;
        this.position = pos;
        this.hp = hp;
        this.defense = defense;
    }

    // ── Factory method (แทน MinionA-E) ───────────────────────
    public static Minion create(String kind, String id,
                                Player owner, Position pos, long hp) {
        int defense = switch (kind) {
            case "A" -> 3;
            case "B" -> 2;
            case "C" -> 1;
            case "D" -> 5;
            case "E" -> 4;
            default  -> throw new IllegalArgumentException("Unknown kind: " + kind);
        };
        return new Minion(id, kind, owner, pos, hp, defense);
    }

    // ── Getters ───────────────────────────────────────────────
    public String getId()                    { return id; }
    public String getKind()                  { return kind; }
    public String getKindName()              { return "Minion" + kind; }
    public Player getOwner()                 { return owner; }
    public Position getPosition()            { return position; }
    public long getHp()                      { return hp; }
    public int getDefense()                  { return defense; }
    public int getSpawnTurn()                { return spawnTurn; }
    public List<Stmt> getStrategyAST()    { return strategyAST; }

    // ── Setters ───────────────────────────────────────────────
    public void setPosition(Position p)      { this.position = p; }
    public void setStrategyAST(List<Stmt> a) { this.strategyAST = a; }
    public void setSpawnTurn(int t)          { this.spawnTurn = t; }

    // ── State ─────────────────────────────────────────────────
    public boolean isAlive() { return hp > 0; }
    public boolean isDead()  { return hp <= 0; }

    // ── takeDamage: แค่ลด hp ไม่รู้เรื่องเกม ─────────────────
    public long takeDamage(long expenditure) {
        long actual = Math.max(1, expenditure - defense);
        hp = Math.max(0, hp - actual);
        return actual;
    }
}