package strategy.ast.stmt;

import strategy.ast.Expr;
import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;

public class IfStmt implements Stmt {

    private final Expr condition;
    private final Stmt thenStmt;
    private final Stmt elseStmt;

    public IfStmt(Expr condition, Stmt thenStmt, Stmt elseStmt) {
        this.condition = condition;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }

    @Override
    public void execute(EvalContext ctx) {
        if (condition.eval(ctx) != 0) {
            thenStmt.execute(ctx);
        } else {
            elseStmt.execute(ctx);
        }
    }
}
