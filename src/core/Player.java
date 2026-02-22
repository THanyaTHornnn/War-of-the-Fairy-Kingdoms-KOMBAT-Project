package core;

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

    public Player(String id, boolean isBot) {
        this.id = id;
        this.isBot = isBot;
    }

    // ── Getters ──────────────────────────────────────────────
    public String getId()               { return id; }
    public boolean isBot()              { return isBot; }
    public double getBudget()           { return budget; }
    public long getBudgetFloor()        { return (long) Math.floor(budget); }
    public int getTurnCount()           { return turnCount; }
    public int getSpawnsUsed()          { return spawnsUsed; }
    public Set<String> getSpawnableHexes() { return spawnableHexes; }
    public Map<String, Minion> getMinions() { return minions; }

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
}