package strategy.ast;

import strategy.evaluator.EvalContext;

public interface Expr {
    long eval(EvalContext ctx);
}
