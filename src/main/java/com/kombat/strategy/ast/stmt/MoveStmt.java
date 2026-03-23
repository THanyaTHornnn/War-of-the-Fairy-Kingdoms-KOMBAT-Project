package com.kombat.strategy.ast.stmt;

import com.kombat.strategy.ast.Stmt;
import com.kombat.strategy.evaluator.EvalContext;

public class MoveStmt implements Stmt {

    private final int direction;

    public MoveStmt(int direction) {
        this.direction = direction;
    }

    @Override
    public void execute(EvalContext ctx) {
        ctx.move(direction);
    }
}

