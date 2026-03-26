package com.kombat.core;

import com.kombat.strategy.ast.Stmt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Minion {
    private final String id;
    private final String kind;
    private final Player owner;
    private final int defense;
    private long hp;
    private Position position;
    private List<Stmt> strategyAST;
    private int spawnTurn;
    private final Map<String, Long> localVars = new HashMap<>();

    private Minion(String id, String kind, Player owner,
                   Position pos, long hp, int defense) {
        this.id = id;
        this.kind = kind;
        this.owner = owner;
        this.position = pos;
        this.hp = hp;
        this.defense = defense;
    }


    public static Minion create(String kind, String id,
                                Player owner, Position pos, long hp, int defense) {
        return new Minion(id, kind, owner, pos, hp, defense);
    }

    public String getId()                    { return id; }
    public String getKindName()              { return "Minion" + kind; }
    public Player getOwner()                 { return owner; }
    public Position getPosition()            { return position; }
    public long getHp()                      { return hp; }
    public int getDefense()                  { return defense; }
    public int getSpawnTurn()                { return spawnTurn; }
    public List<Stmt> getStrategyAST()    { return strategyAST; }
    public void setPosition(Position p)      { this.position = p; }
    public void setStrategyAST(List<Stmt> a) { this.strategyAST = a; }
    public void setSpawnTurn(int t)          { this.spawnTurn = t; }
    public boolean isAlive() { return hp > 0; }
    public boolean isDead()  { return hp <= 0; }

    public long takeDamage(long expenditure) {
        long actual = Math.max(1, expenditure - defense);
        System.out.println("[DAMAGE] " + id + " (" + kind + ") DEF=" + defense);
        System.out.println("[DAMAGE]   Expenditure: " + expenditure);
        System.out.println("[DAMAGE]   Actual damage: " + actual);
        System.out.println("[DAMAGE]   HP before: " + hp);
        hp = Math.max(0, hp - actual);
        return actual;
    }

    public Map<String, Long> getLocalVars()  { return localVars; }

}