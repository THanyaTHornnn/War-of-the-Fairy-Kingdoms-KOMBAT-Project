package dto;

import java.util.List;
import java.util.Set;

// DTO สำหรับส่งข้อมูลออก REST API
public class GameStateDto {
    public int turn;
    public String phase;
    public String current;
    public String winner;
    public String endReason;
    public PlayerDto p1;
    public PlayerDto p2;
    public List<MinionDto> minions;
    public ConfigDto config;

    public GameStateDto() {}

    // ── Nested DTOs ───────────────────────────────────────────
    public static class PlayerDto {
        public long budget;
        public int turnCount;
        public int spawnsUsed;
        public int spawnsLeft;
        public Set<String> spawnableHexes;
        public double interestRate;
        public int minionCount;
        public long totalHP;
        public PlayerDto() {}
    }

    public static class MinionDto {
        public String id;
        public String owner;
        public int row;
        public int col;
        public long hp;
        public int defense;
        public String kind;
        public int spawnTurn;
        public MinionDto() {}
    }

    public static class ConfigDto {
        public long spawnCost;
        public long hexPurchaseCost;
        public long initBudget;
        public long initHp;
        public long turnBudget;
        public long maxBudget;
        public long interestPct;
        public long maxTurns;
        public long maxSpawns;
        public ConfigDto() {}
    }
}