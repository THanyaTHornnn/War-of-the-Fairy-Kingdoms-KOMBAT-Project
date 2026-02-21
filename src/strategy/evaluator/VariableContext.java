package strategy.evaluator;

import gameState.GameState;
import gameState.minnion.Minion;


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
        if (!locals.containsKey(name)) {
            throw new RuntimeException("Undefined variable: " + name);
        }
        return locals.get(name);
    }

    public void setVar(String name, long value) {
        locals.put(name, value);
    }

    public boolean hasVar(String name) {
        return locals.containsKey(name);
    }


    public long getSpecial(String name) {
        return switch (name) {
            case "hp" -> minion.getHp();
            case "row" -> minion.getPosition().getRow();
            case "col" -> minion.getPosition().getCol();
            default -> throw new RuntimeException("Unknown special var: " + name);
        };
    }



}
