package strategy.ast.expr;

import strategy.ast.Expr;
import strategy.evaluator.EvalContext;

public class NearbyExpr implements Expr {

    private final int direction;

    public NearbyExpr(int direction) {
        this.direction = direction;
    }

    @Override
    public long eval(EvalContext ctx) {
        return ctx.nearby(direction);
    }
}