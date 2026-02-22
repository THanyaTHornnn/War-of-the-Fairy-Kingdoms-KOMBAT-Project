package strategy.evaluator;

import core.GameState;
import core.Minion;


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
        || name.equals("hp") || name.equals("row") || name.equals("col");
    }


    private long getSpecial(String name) {
        return switch (name) {
            case "hp" -> minion.getHp();
            case "row" -> minion.getPosition().getRow();
            case "col" -> minion.getPosition().getCol();
            default -> throw new RuntimeException("Unknown special var: " + name);
        };
    }
    public GameState getGameState() {
        return gameState;
    }

    public Minion getMinion() {
        return minion;
    }


}
