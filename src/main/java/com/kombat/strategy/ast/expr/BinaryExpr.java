package com.kombat.strategy.ast.expr;

import com.kombat.strategy.ast.Expr;
import com.kombat.strategy.evaluator.EvalContext;


public class BinaryExpr implements Expr {

    public enum Op { PLUS , MINUS, STAR, DIV, MOD, GE, LE, GT, LT, NEQ, EQ, CARET }

    private final Expr left;
    private final Expr right;
    private final Op op;

    public BinaryExpr(Expr left, Op op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public long eval(EvalContext ctx) {
        long l = left.eval(ctx);
        long r = right.eval(ctx);

        if ((op == Op.DIV || op == Op.MOD) && r == 0) {
            ctx.done();
        }

        return switch (op) {
            case PLUS -> l + r;
            case MINUS -> l - r;
            case STAR -> l * r;
            case DIV -> l / r;
            case MOD -> l % r;
            case CARET -> (long) Math.pow(l, r);
            default -> throw new RuntimeException("Unknown operator");
        };
    }

}
