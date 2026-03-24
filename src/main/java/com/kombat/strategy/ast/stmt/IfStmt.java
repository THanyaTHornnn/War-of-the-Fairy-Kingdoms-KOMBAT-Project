package com.kombat.strategy.ast.stmt;

import com.kombat.strategy.ast.Expr;
import com.kombat.strategy.ast.ExprUtils;
import com.kombat.strategy.ast.Stmt;
import com.kombat.strategy.evaluator.EvalContext;

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
