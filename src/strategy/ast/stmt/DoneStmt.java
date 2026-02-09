package strategy.ast.stmt;

import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;
import strategy.runtime.RuntimeTerminate;

public class DoneStmt implements Stmt {

    @Override
    public void execute(EvalContext ctx) {
        ctx.done(); // ภายใน ctx.done() → throw RuntimeTerminate
    }
}
