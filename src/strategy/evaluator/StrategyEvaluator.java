package strategy.evaluator;

import strategy.ast.Stmt;
import java.util.List;

public interface StrategyEvaluator {
    void evaluate(List<Stmt> program, EvalContext ctx);
}
