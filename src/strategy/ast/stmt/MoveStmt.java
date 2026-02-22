package strategy.ast.stmt;

import gameState.Direction;
import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;


public class MoveStmt implements Stmt {

    private final Direction direction;

    public MoveStmt(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void execute(EvalContext ctx) {
        ctx.move(direction);
    }

}
