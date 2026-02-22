package strategy.ast.stmt;

import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;


public class DoneStmt implements Stmt {

    @Override
    public void execute(EvalContext ctx) {
        ctx.done(); // หยุด strategy ทันที ภายใน ctx.done()  throw RuntimeTerminate
    }
}
