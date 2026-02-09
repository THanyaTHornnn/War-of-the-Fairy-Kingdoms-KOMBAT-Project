package strategy.ast.stmt;

import strategy.ast.Expr;
import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;

public class AssignStmt implements Stmt {

    private final String name;
    private final Expr expr;

    public AssignStmt(String name, Expr expr) {
        this.name = name;
        this.expr = expr;
    }

    @Override
    public void execute(EvalContext ctx) {
        long value = expr.eval(ctx);
        ctx.setVar(name, value);
    }
}
