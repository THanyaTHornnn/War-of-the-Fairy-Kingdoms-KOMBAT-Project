package strategy.ast.stmt;

import core.Position;
import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;


public class MoveStmt implements Stmt {

    private final int direction;


    public MoveStmt(int direction) {
        this.direction = direction;
    }

    @Override
    public void execute(EvalContext ctx) {
        ctx.move(direction);
    }
}
