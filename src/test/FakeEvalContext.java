package test;

import gameState.Direction;
import strategy.evaluator.EvalContext;

public class FakeEvalContext implements EvalContext {

    @Override public long getVar(String name){ return 0; }
    @Override public void setVar(String name,long v){}

    @Override public long getSpecialVar(String name){ return 0; }

    @Override public boolean move(Direction dir){ return false; }
    @Override public boolean shoot(Direction dir,long dmg){ return false; }

    @Override public int ally(){ return 5; }
    @Override public int opponent(){ return 3; }
    @Override public int nearby(){ return 1; }

    @Override public void done(){}
}
