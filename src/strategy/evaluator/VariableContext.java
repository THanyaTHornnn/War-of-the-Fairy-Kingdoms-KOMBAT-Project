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

    public long get(String name) {
        return locals.getOrDefault(name, 0L);
    }

    public void set(String name, long value) {
        locals.put(name, value);
    }

     //special variables
     public long resolve(String name, EvalContext ctx) {

         // local variable
         if (locals.containsKey(name)) {
             return locals.get(name);
         }

         // dynamic game info
         switch (name) {
             case "ally": return ctx.ally();
             case "opponent": return ctx.opponent();
             case "nearby": return ctx.nearby();
         }

         throw new RuntimeException("Unknown variable: " + name);
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
