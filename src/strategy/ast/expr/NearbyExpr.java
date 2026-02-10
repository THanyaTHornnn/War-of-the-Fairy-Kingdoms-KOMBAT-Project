package strategy.ast.expr;

import strategy.ast.Expr;
import strategy.evaluator.EvalContext;

public class NearbyExpr implements Expr {

    private final int dir;

    public NearbyExpr(int dir) {
        this.dir = dir;
    }

    @Override
    public long eval(EvalContext ctx) {
        return ctx.nearby(dir);
    }
}
