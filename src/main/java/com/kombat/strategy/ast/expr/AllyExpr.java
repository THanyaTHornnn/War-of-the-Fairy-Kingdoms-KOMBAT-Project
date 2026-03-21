package com.kombat.strategy.ast.expr;

import com.kombat.strategy.ast.Expr;
import com.kombat.strategy.evaluator.EvalContext;

public class AllyExpr implements Expr {

    @Override
    public long eval(EvalContext ctx) {
        return ctx.ally();
    }
}
