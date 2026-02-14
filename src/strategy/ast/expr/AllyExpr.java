package strategy.ast.expr;

import strategy.ast.Expr;
import strategy.evaluator.EvalContext;

public class AllyExpr implements Expr {

    @Override
    public long eval(EvalContext ctx) {
        return ctx.ally();
    }
}
