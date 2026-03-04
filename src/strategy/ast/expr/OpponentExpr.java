package strategy.ast.expr;

import strategy.ast.Expr;
import strategy.evaluator.EvalContext;

public class OpponentExpr implements Expr {

    @Override
    public long eval(EvalContext ctx) {
        return ctx.opponent();
    }
}
