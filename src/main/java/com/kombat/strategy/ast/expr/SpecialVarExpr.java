package com.kombat.strategy.ast.expr;

import com.kombat.strategy.ast.Expr;
import com.kombat.strategy.evaluator.EvalContext;
import com.kombat.strategy.runtime.RuntimeError;

public class SpecialVarExpr implements Expr {

    private final String name;

    public SpecialVarExpr(String name) {
        this.name = name;
    }
//
    @Override
    public long eval(EvalContext ctx) {
        try{
        return ctx.getSpecialVar(name);
        } catch (RuntimeError e) {
            throw e;
        }
    }
}
