package com.kombat.strategy.ast.expr;

import com.kombat.strategy.ast.Expr;
import com.kombat.strategy.evaluator.EvalContext;

public class OpponentExpr implements Expr {

    @Override
    public long eval(EvalContext ctx) {
        return ctx.opponent();
    }
}
