package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Config {
    public long spawnCost       = 100;
    public long hexPurchaseCost = 1000;
    public long initBudget      = 10000;
    public long initHp          = 100;
    public long turnBudget      = 90;
    public long maxBudget       = 23456;
    public long interestPct     = 5;
    public long maxTurns        = 69;
    public long maxSpawns       = 47;

    public static Config defaultConfig() { return new Config(); }

    public static Config parse(String filePath) throws IOException {
        Config c = new Config();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    long val = Long.parseLong(parts[1].trim());
                    switch (parts[0].trim()) {
                        case "spawn_cost"        -> c.spawnCost = val;
                        case "hex_purchase_cost" -> c.hexPurchaseCost = val;
                        case "init_budget"       -> c.initBudget = val;
                        case "init_hp"           -> c.initHp = val;
                        case "turn_budget"       -> c.turnBudget = val;
                        case "max_budget"        -> c.maxBudget = val;
                        case "interest_pct"      -> c.interestPct = val;
                        case "max_turns"         -> c.maxTurns = val;
                        case "max_spawns"        -> c.maxSpawns = val;
                    }
                }
            }
        }
        return c;
    }
}