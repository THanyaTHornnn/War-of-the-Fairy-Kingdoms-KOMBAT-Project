package strategy.ast.stmt;

import strategy.ast.Expr;
import strategy.ast.ExprUtils;
import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;

public class WhileStmt implements Stmt {

    private final Expr condition;
    private final Stmt body;

    public WhileStmt(Expr condition, Stmt body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void execute(EvalContext ctx) {
        // spec: "a while loop that has run for 10000 iterations is terminated"
        // for (int counter = 0; counter < 10000 && e > 0; counter++) s
        for (int i = 0; i < 10000; i++) {
            if (!ExprUtils.isTrue(condition.eval(ctx))) break;
            if (ctx.isDone()) break;
            body.execute(ctx);
            if (ctx.isDone()) break;
        }
    }
}