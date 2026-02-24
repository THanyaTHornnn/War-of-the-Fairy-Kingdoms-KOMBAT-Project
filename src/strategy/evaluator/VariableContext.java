package strategy.evaluator;

import core.GameState;
import core.Minion;
import core.Player;

import java.util.HashMap;
import java.util.Map;

public class VariableContext {

    private final Map<String, Long> locals = new HashMap<>();
    private final GameState gameState;
    private final Minion minion;

    public VariableContext(GameState gameState, Minion minion) {
        this.gameState = gameState;
        this.minion = minion;
    }

    public long getVar(String name) {
        if (locals.containsKey(name))
            return locals.get(name);
        return getSpecial(name);
    }

    public void setVar(String name, long value) {
        locals.put(name, value);
    }

    public boolean hasVar(String name) {
        return locals.containsKey(name)
                || name.equals("hp") || name.equals("row") || name.equals("col")
                || name.equals("Budget") || name.equals("MaxBudget")
                || name.equals("SpawnsLeft") || name.equals("random")
                || name.equals("Int");
    }

    private long getSpecial(String name) {
        Player owner = minion.getOwner();  // ← ต้องอยู่นอก switch
        return switch (name) {
            case "hp"         -> minion.getHp();
            case "row"        -> minion.getPosition().getRow();
            case "col"        -> minion.getPosition().getCol();
            case "Budget"     -> owner.getBudgetFloor();
            case "MaxBudget"  -> gameState.config.maxBudget;
            case "SpawnsLeft" -> gameState.config.maxSpawns - owner.getSpawnsUsed();
            case "random"     -> (long)(Math.random() * 1000);
            case "Int"        -> (long) owner.interestRate(gameState.config.interestPct);
            default           -> 0L;
        };
    }

    public GameState getGameState() { return gameState; }
    public Minion getMinion()       { return minion; }
}