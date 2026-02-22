package strategy.ast.expr;

import gameState.Direction;
import strategy.ast.Expr;
import strategy.evaluator.EvalContext;

public class NearbyExpr implements Expr {
//
    private final Direction direction;

    public NearbyExpr(Direction direction) {
        this.direction = direction;
    }

    @Override
    public long eval(EvalContext ctx) {
        return ctx.nearby(direction);
    }
}
