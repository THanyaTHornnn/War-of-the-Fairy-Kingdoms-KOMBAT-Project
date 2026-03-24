package com.kombat.strategy.evaluator;

import com.kombat.strategy.ast.Stmt;
import com.kombat.strategy.runtime.RuntimeTerminate;

import java.util.List;

public class StrategyEvaluatorImpl implements StrategyEvaluator {

    @Override
    public void evaluate(List<Stmt> strategy, EvalContext ctx) {
        try {
            for (Stmt stmt : strategy) {
                if (ctx.isDone()) break;

                stmt.execute(ctx);

                if (ctx.isDone()) break;
            }
        } catch (RuntimeTerminate e) {
            // done → จบ strategy
        }
    }
}
