//package test;
//
//import gameState.Direction;
//import strategy.evaluator.EvalContext;
//
//public class FakeEvalContext implements EvalContext {
//
//    @Override public long getVar(String name){ return 0; }
//    @Override public void setVar(String name,long v){}
//
//    @Override public long getSpecialVar(String name){ return 0; }
//
//    @Override public void move(Direction dir){  }
//    @Override public void shoot(Direction dir, long dmg){  }
//
//    @Override public long ally(){ return 5; }
//    @Override public long opponent(){ return 3; }
//    @Override public long nearby(Direction direction){ return 1; }
//
//    @Override public void done(){}
//}
