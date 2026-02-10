package strategy.ast.expr;

import strategy.ast.Expr;
import strategy.evaluator.EvalContext;
import strategy.runtime.RuntimeError;

public class SpecialVarExpr implements Expr {

    private final String name;

    public SpecialVarExpr(String name) {
        this.name = name;
    }

    @Override
    public long eval(EvalContext ctx) {
        return ctx.getSpecialVar(name);
    }
}
