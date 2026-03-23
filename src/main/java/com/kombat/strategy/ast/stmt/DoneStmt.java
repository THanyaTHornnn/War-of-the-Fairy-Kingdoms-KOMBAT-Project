package com.kombat.strategy.ast.stmt;

import com.kombat.strategy.ast.Stmt;
import com.kombat.strategy.evaluator.EvalContext;


public class DoneStmt implements Stmt {

    @Override
    public void execute(EvalContext ctx) {
        ctx.done(); // หยุด strategy ทันที ภายใน ctx.done()  throw RuntimeTerminate
    }
}
