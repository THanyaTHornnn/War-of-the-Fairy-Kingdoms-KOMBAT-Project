package strategy.ast.stmt;

import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;


public class MoveStmt implements Stmt {

    private final int dir;

    public MoveStmt(int dir) {
        this.dir = dir;
    }

    @Override
    public void execute(EvalContext ctx) {
        ctx.move(dir);
    }
}
