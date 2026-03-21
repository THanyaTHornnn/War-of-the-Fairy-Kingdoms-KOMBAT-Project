package com.kombat.strategy.ast;

import com.kombat.strategy.evaluator.EvalContext;

public interface Expr {
    long eval(EvalContext ctx);
}
