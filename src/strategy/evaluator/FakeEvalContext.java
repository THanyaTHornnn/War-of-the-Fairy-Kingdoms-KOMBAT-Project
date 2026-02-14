//package strategy.evaluator;
//
//import gameState.Direction;
//import strategy.runtime.RuntimeTerminate;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class FakeEvalContext implements EvalContext {
//
//    private final Map<String, Long> vars = new HashMap<>();
//
//    @Override
//    public long getVar(String name) {
//        return vars.getOrDefault(name, 0L);
//    }
//
//    @Override
//    public void setVar(String name, long value) {
//        vars.put(name, value);
//    }
//
//    @Override
//    public long getSpecialVar(String name) {
//        return switch (name) {
//            case "ally" -> 2;
//            case "opponent" -> 3;
//            default -> 0;
//        };
//    }
//
//    @Override
//    public void move(int dir) { }
//
//    @Override
//    public void shoot(Direction direction, long dmg) {
//
//    }
//
//    @Override
//    public void done() {
//        throw new RuntimeTerminate();
//    }
//
//    @Override
//    public long countAlly() { return 2; }
//
//    @Override
//    public long countOpponent() { return 3; }
//
//    @Override
//    public long nearby(int dir) { return 1; }
//
//
//}
