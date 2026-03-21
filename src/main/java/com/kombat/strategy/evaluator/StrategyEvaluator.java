package com.kombat.strategy.evaluator;

import com.kombat.strategy.ast.Stmt;
import java.util.List;

public interface StrategyEvaluator {
    void evaluate(List<Stmt> strategy, EvalContext ctx);
}
