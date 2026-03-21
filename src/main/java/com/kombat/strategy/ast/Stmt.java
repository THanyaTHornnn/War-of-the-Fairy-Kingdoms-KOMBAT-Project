package com.kombat.strategy.ast;

import strategy.evaluator.EvalContext;

public interface Stmt {
    void execute(EvalContext ctx);
}

