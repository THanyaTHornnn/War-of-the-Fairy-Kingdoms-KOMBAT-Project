//package strategy.evaluator;
//
//import game.GameState;
//import game.Minion;
//import java.util.HashMap;
//import java.util.Map;
//
//public class VariableContext {
//
//    private final Map<String, Long> locals = new HashMap<>();
//    private final GameState gameState;
//    private final Minion minion;
//
//    public VariableContext(GameState gameState, Minion minion) {
//        this.gameState = gameState;
//        this.minion = minion;
//    }
//
//    public long get(String name) {
//        return locals.getOrDefault(name, 0L);
//    }
//
//    public void set(String name, long value) {
//        locals.put(name, value);
//    }
//
//    // special variables
//    public long budget() {
//        return gameState.getBudget(minion.getOwner());
//    }
//}
