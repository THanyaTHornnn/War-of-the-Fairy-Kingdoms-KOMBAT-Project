//package strategy.evaluator;
//
//import gameState.GameState;
//import gameState.Minion;
//import gameState.Direction;
//
//public class EvalContextImpl implements EvalContext {
//
//    private final GameState gameState;
//    private final Minion minion;
//    private final VariableContext vars;
//
//    public EvalContextImpl(GameState gameState, Minion minion) {
//        this.gameState = gameState;
//        this.minion = minion;
//        this.vars = new VariableContext(gameState, minion);
//    }
//
//    @Override
//    public long getVar(String name) {
//        return vars.get(name);
//    }
//
//    @Override
//    public void setVar(String name, long value) {
//        vars.set(name, value);
//    }
//
//    @Override
//    public long getSpecialVar(String name) {
//        return vars.getSpecial(name);
//    }
//
//    @Override
//    public void move(int dir) {
//        gameState.move(minion, Direction.fromInt(dir));
//    }
//
//    @Override
//    public void shoot(int dir) {
//        gameState.shoot(minion, Direction.fromInt(dir));
//    }
//
//    @Override
//    public void done() {
//        throw new strategy.runtime.RuntimeTerminate();
//    }
//}
