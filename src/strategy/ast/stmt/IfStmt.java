package strategy.ast.stmt;

import strategy.ast.Expr;
import strategy.ast.ExprUtils;
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
        if (ExprUtils.isTrue(condition.eval(ctx))) {
            thenStmt.execute(ctx);
        } else {
            if (elseStmt != null) {
                elseStmt.execute(ctx);
            }
        }
    }
}
