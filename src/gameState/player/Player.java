package gameState.player;
import gameState.minnion.Minion;

import java.util.ArrayList;
import java.util.List;


public class Player {

    private final String name;
    private double budget;
    private final double maxBudget;
    private final double turnBudget;
    private int turnCount;

    private final List<Minion> minions = new ArrayList<>();

    public Player(String name, Config config) {
        this.name = name;
        this.budget = config.initBudget;
        this.maxBudget = config.maxBudget;
        this.turnBudget = config.turnBudget;
        this.turnCount = 1;
    }


    public void addTurnBudget() {
        budget += turnBudget;
        if (budget > maxBudget) {
            budget = maxBudget;
        }
    }


    public double getBudget() {
        return budget;
    }

    public List<Minion> getMinions() {
        return minions;
    }
    public void addMinion(Minion m) {
        if (m == null)
            throw new IllegalArgumentException("Minion cannot be null");

        minions.add(m);
    }
    public int totalHP() {
        int sum = 0;
        for (Minion m : minions) sum += m.getHp();
        return sum;
    }

    public void applyInterest(double baseInterestPct) {
        if (budget >= 1.0) {
            double r =
                    baseInterestPct *
                            Math.log10(budget) *
                            Math.log(turnCount); // ln(t)

            budget += budget * r / 100.0;
        }

        if (budget > maxBudget) budget = maxBudget;
        if (budget < 0) budget = 0;

        turnCount++;
    }
    public void removeMinion(Minion m) {
        minions.remove(m);
    }
    public String getName() {
        return name;
    }
    public boolean canAfford(double cost) {
        return budget >= cost;
    }

    public void useBudget(double cost) {
        if (!canAfford(cost))
            throw new IllegalStateException("Not enough budget");

        budget -= cost;
    }

}