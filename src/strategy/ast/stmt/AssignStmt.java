package strategy.ast.stmt;

import strategy.ast.Expr;
import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;

public class AssignStmt implements Stmt {

    private final String varName;
    private final Expr expr;

    public AssignStmt(String varName, Expr expr) {
        this.varName = varName;
        this.expr = expr;
    }

    @Override
    public void execute(EvalContext ctx) {
        long value = expr.eval(ctx);
        ctx.setVar(varName, value);
    }

    public String getName() {
        return varName;
    }
}
