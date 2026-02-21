package strategy.ast.stmt;

import strategy.ast.Expr;
import strategy.ast.ExprUtils;
import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;
import strategy.runtime.RuntimeError;

public class WhileStmt implements Stmt {

    private final Expr condition;
    private final Stmt body;

    public WhileStmt(Expr condition, Stmt body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void execute(EvalContext ctx) {
        while (ExprUtils.isTrue(condition.eval(ctx))) {
            body.execute(ctx);

            if (ctx.isDone()) break;
        }
    }
}
