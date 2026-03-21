package com.kombat.strategy.evaluator;

import com.kombat.core.GameLogic;
import com.kombat.core.Minion;
import com.kombat.core.Player;

import java.util.Map;

public class VariableContext {

    private final Map<String, Long> locals;
    private final Minion minion;
    private final GameLogic gameLogic;

    public VariableContext(GameLogic gameLogic, Minion minion) {
        this.gameLogic = gameLogic;
        this.minion    = minion;
        this.locals    = minion.getLocalVars();

    }

    public long getVar(String name) {
        if (isSpecial(name)) return getSpecial(name);

        if (Character.isUpperCase(name.charAt(0))) {
            return minion.getOwner().getGlobal(name);
        }

        return locals.getOrDefault(name, 0L);
    }

    public void setVar(String name, long value) {
        if (Character.isUpperCase(name.charAt(0))) {
            minion.getOwner().setGlobal(name, value);
        } else {
            locals.put(name, value);
        }
    }

    public boolean hasVar(String name) {
        return isSpecial(name)
                || locals.containsKey(name)
                || (Character.isUpperCase(name.charAt(0))
                && minion.getOwner().hasGlobal(name));
    }

    private boolean isSpecial(String name) {
        return switch (name) {
            case "hp", "row", "col",
                 "Budget", "MaxBudget",
                 "SpawnsLeft", "random", "Int" -> true;
            default -> false;
        };
    }

    private long getSpecial(String name) {
        Player owner = minion.getOwner();
        return switch (name) {
            case "hp"         -> minion.getHp();
            case "row"        -> minion.getPosition().getRow();
            case "col"        -> minion.getPosition().getCol();
            case "Budget"     -> minion.getOwner().getBudgetFloor();
            case "SpawnsLeft" -> gameLogic.getConfig().maxSpawns - minion.getOwner().getSpawnsUsed();
            case "MaxBudget"  -> gameLogic.getConfig().maxBudget;
            case "random"     -> (long)(Math.random() * 1000);
            case "Int"        -> (long) owner.interestRate(gameLogic.getConfig().interestPct);
            default           -> 0L;
        };
    }
}