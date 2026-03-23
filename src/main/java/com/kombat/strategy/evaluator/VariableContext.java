package com.kombat.strategy.evaluator;

import com.kombat.core.GameState;
import com.kombat.core.Minion;
import com.kombat.core.Player;


import java.util.Map;

public class VariableContext {

    // ใช้ map จาก Minion โดยตรง → ค่าคงอยู่ข้าม turn
    private final Map<String, Long> locals;
    private final GameState gameState;
    private final Minion minion;

    public VariableContext(GameState gameState, Minion minion) {
        this.gameState = gameState;
        this.minion = minion;
        this.locals = minion.getLocalVars(); // reference เดิม ไม่ใช่ copy
    }

    public long getVar(String name) {
        // special vars มาก่อนเสมอ
        if (isSpecial(name)) return getSpecial(name);

        // global (ขึ้นต้นด้วยตัวพิมพ์ใหญ่)
        if (Character.isUpperCase(name.charAt(0))) {
            return minion.getOwner().getGlobal(name);
        }

        // local
        return locals.getOrDefault(name, 0L);
    }

    public void setVar(String name, long value) {
        // global (ขึ้นต้นด้วยตัวพิมพ์ใหญ่)
        if (Character.isUpperCase(name.charAt(0))) {
            minion.getOwner().setGlobal(name, value);
        } else {
            locals.put(name, value);
        }
    }

    public boolean hasVar(String name) {
        return isSpecial(name)
                || locals.containsKey(name)
                || (Character.isUpperCase(name.charAt(0)) && minion.getOwner().hasGlobal(name));
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
            // ตัวแปรที่ยังไม่ถูก assign → return 0 ตาม spec
            default -> 0L;
        };
    }
    public GameState getGameState() {
        return gameState;
    }

    public Minion getMinion() {
        return minion;
    }


}