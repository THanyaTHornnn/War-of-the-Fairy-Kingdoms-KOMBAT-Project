package com.kombat.strategy.evaluator;

import core.GameState;
import core.Minion;
import core.Player;

import java.util.Map;

public class VariableContext {

    private final Map<String, Long> locals;
    private final GameState gameState;
    private final Minion minion;

    public VariableContext(GameState gameState, Minion minion) {
        this.gameState = gameState;
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
            case "Budget"     -> owner.getBudgetFloor();
            case "MaxBudget"  -> gameState.config.maxBudget;
            case "SpawnsLeft" -> gameState.config.maxSpawns - owner.getSpawnsUsed();
            case "random"     -> (long)(Math.random() * 1000);
            case "Int"        -> (long) owner.interestRate(gameState.config.interestPct);
            default           -> 0L;
        };
    }
}