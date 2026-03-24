package com.kombat.strategy.ast.stmt;

import com.kombat.strategy.ast.Stmt;
import com.kombat.strategy.evaluator.EvalContext;

import java.util.List;

public class BlockStmt implements Stmt {

    private final List<Stmt> statements;

    public BlockStmt(List<Stmt> statements) {
        this.statements = statements;
    }

    @Override
    public void execute(EvalContext ctx) {
        for (Stmt stmt : statements) {
            stmt.execute(ctx);
        }
    }

    public List<Stmt> getStatements() {
        return statements;
    }
}
