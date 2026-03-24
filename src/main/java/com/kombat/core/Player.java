package com.kombat.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Player {
    private final String id;
    private final boolean isBot;
    private double budget;
    private int turnCount;
    private int spawnsUsed;
    private final Set<String> spawnableHexes = new HashSet<>();
    private final Map<String, Minion> minions = new HashMap<>();
    private boolean auto;
    private final Map<String, Long> globals = new HashMap<>();
    private int lastPurchaseTurn = -1;   // เทิร์นล่าสุดที่ซื้อ hex
    private int lastSpawnTurn = -1;
    private Position lastPurchasedHex;
    public Player(String id, boolean isBot) {
        this.id = id;
        this.isBot = isBot;
        this.auto =  isBot;
    }

    // ── Getters ──────────────────────────────────────────────
    public String getId()               { return id; }
    //public boolean isBot()              { return isBot; }
    public long getBudget()           { return (long) budget; }
    public long getBudgetFloor()        { return (long) Math.floor(budget); }
    public int getTurnCount()           { return turnCount; }
    public int getSpawnsUsed()          { return spawnsUsed; }
    public Set<String> getSpawnableHexes() { return spawnableHexes; }
    //public Map<String, Minion> getMinions() { return minions; }

    // ── Budget ───────────────────────────────────────────────
    public void setBudget(double v)     { this.budget = v; }
    public void addBudget(double v)     { this.budget += v; }
    public void deductBudget(long cost) {
        if (getBudgetFloor() < cost)
            throw new IllegalStateException("Insufficient budget");
        this.budget -= cost;
    }
    public boolean canAfford(long cost) { return getBudgetFloor() >= cost; }

    // ── Turn / Spawn ─────────────────────────────────────────
    public void incrementTurnCount()    { turnCount++; }
    public void incrementSpawnsUsed()   { spawnsUsed++; }

    public boolean hasPurchasedThisTurn(int currentTurn) {
        return lastPurchaseTurn == currentTurn;
    }

    public void setPurchasedThisTurn(int currentTurn) {
        this.lastPurchaseTurn = currentTurn;
    }



    // ── Spawnable Hexes ──────────────────────────────────────
    public void addSpawnableHex(Position pos) { spawnableHexes.add(pos.toString()); }
    public boolean isSpawnable(Position pos)  { return spawnableHexes.contains(pos.toString()); }

    // ── Minions ──────────────────────────────────────────────
    public void addMinion(Minion m)           { minions.put(m.getId(), m); }
    public void removeMinion(String id)       { minions.remove(id); }

    public int getMinionCount() {
        return (int) minions.values().stream().filter(Minion::isAlive).count();
    }
    public long getTotalHP() {
        return minions.values().stream()
                .filter(Minion::isAlive).mapToLong(Minion::getHp).sum();
    }


    // ── Interest rate: b * log10(budget) * ln(turnCount) ────
    public double interestRate(long basePct) {
        if (budget < 1 || turnCount == 0) return 0;
        return basePct * Math.log10(budget) * Math.log(turnCount);
    }
    public boolean isAuto() {
        return auto;
    }

    // ── Global variables ─────────────────────────────────────
    public long getGlobal(String name) {
        return globals.getOrDefault(name, 0L);
    }

    public void setGlobal(String name, long value) {
        globals.put(name, value);
    }

    public boolean hasGlobal(String name) {
        return globals.containsKey(name);
    }
    public void reset() {
        this.budget = 0;
        this.turnCount = 0;
        this.spawnsUsed = 0;
        this.minions.clear();
        this.spawnableHexes.clear();
        this.lastPurchaseTurn = -1;
        this.globals.clear();
    }
    public void resetSpawnedThisTurn() {
        this.lastSpawnTurn = -1;
    }

    public void resetPurchasedThisTurn() {
        this.lastPurchaseTurn = -1;
    }
    // ✅ setter
    public void setLastPurchasedHex(Position p) {
        this.lastPurchasedHex = p;
    }

}