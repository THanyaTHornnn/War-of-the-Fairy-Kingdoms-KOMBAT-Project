package gameState.player;

public class Config {

    public final int spawnCost;
    public final int hexPurchaseCost;
    public final double initBudget;
    public final int initHp;
    public final double turnBudget;
    public final double maxBudget;
    public final double interestPct;
    public final int maxTurns;
    public final int maxSpawns;
    public Config() {
        this.spawnCost = 100;
        this.hexPurchaseCost = 1000;
        this.initBudget = 10000;
        this.initHp = 100;
        this.turnBudget = 90;
        this.maxBudget = 23456;
        this.interestPct = 5;
        this.maxTurns = 69;
        this.maxSpawns = 47;
    }
}//
