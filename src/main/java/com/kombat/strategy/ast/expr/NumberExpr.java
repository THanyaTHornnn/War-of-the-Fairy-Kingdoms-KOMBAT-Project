package strategy.ast.expr;

import strategy.ast.Expr;
import strategy.evaluator.EvalContext;

public class NumberExpr implements Expr {

    private final long value;

    public NumberExpr(long value) {
        this.value = value;
    }

    @Override
    public long eval(EvalContext ctx) {
        return value;
    }
}
