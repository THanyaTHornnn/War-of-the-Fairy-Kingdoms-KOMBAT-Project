package strategy.ast.expr;

import strategy.ast.Expr;
import strategy.evaluator.EvalContext;

public class VarExpr implements Expr {

    private final String name;

    public VarExpr(String name) {
        this.name = name;
    }

    @Override
    public long eval(EvalContext ctx) {
        return ctx.getVar(name);
    }

}

