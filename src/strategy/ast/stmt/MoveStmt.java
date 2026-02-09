package strategy.ast.stmt;

import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;

public class MoveStmt implements Stmt {

    private final String direction;

    public MoveStmt(String direction) {
        this.direction = direction;
    }

    @Override
    public void execute(EvalContext ctx) {
        ctx.move(direction);
    }
}
