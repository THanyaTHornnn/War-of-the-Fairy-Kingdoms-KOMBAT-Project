package com.kombat.strategy.ast.stmt;

import com.kombat.strategy.ast.Expr;
import com.kombat.strategy.ast.Stmt;
import com.kombat.strategy.evaluator.EvalContext;

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
        // spec: "An attempt to assign any of these variables results in a no-op"
        if (isReadOnly(varName)) return;
        ctx.setVar(varName, value);
    }

    private boolean isReadOnly(String name) {
        return switch (name) {
            case "hp", "row", "col",
                 "Budget", "Int", "MaxBudget",
                 "SpawnsLeft", "random" -> true;
            default -> false;
        };
    }

    public String getName() {
        return varName;
    }

}