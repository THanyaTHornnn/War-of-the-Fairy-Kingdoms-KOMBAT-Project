package com.kombat.strategy.ast;

import com.kombat.strategy.evaluator.EvalContext;

public interface Stmt {
    void execute(EvalContext ctx);
}

