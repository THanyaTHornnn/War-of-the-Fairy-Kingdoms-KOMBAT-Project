package com.kombat.strategy.ast.expr;

import com.kombat.strategy.ast.Expr;
import com.kombat.strategy.evaluator.EvalContext;

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
