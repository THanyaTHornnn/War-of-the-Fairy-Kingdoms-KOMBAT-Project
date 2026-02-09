package strategy.ast.stmt;

import strategy.ast.Expr;
import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;
import strategy.runtime.RuntimeError;

public class WhileStmt implements Stmt {

    private static final int MAX_LOOP = 10000;

    private final Expr condition;
    private final Stmt body;

    public WhileStmt(Expr condition, Stmt body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void execute(EvalContext ctx) {
        int count = 0;
        while (condition.eval(ctx) != 0) {
            if (++count > MAX_LOOP) {
                throw new RuntimeError("while loop limit exceeded");
            }
            body.execute(ctx);
        }
    }
}
