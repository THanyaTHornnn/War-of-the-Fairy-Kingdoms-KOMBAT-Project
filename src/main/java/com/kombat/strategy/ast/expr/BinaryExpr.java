package strategy.ast.expr;

import strategy.ast.Expr;
import strategy.evaluator.EvalContext;
import strategy.runtime.RuntimeTerminate;

public class BinaryExpr implements Expr {

    public enum Op { PLUS, MINUS, STAR, DIV, MOD, GE, LE, GT, LT, NEQ, EQ, CARET }

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

        // division/mod by zero → terminate strategy immediately (spec behaviour)
        if ((op == Op.DIV || op == Op.MOD) && r == 0) {
            throw new RuntimeTerminate("division by zero");
        }

        return switch (op) {
            case PLUS  -> l + r;
            case MINUS -> l - r;
            case STAR  -> l * r;
            case DIV   -> l / r;
            case MOD   -> l % r;
            case CARET -> (long) Math.pow(l, r);
            // comparison ops — delegate to CompareExpr conventions
            case EQ  -> l == r ? 1L : 0L;
            case LT  -> l <  r ? 1L : 0L;
            case GT  -> l >  r ? 1L : 0L;
            case LE  -> l <= r ? 1L : 0L;
            case GE  -> l >= r ? 1L : 0L;
            case NEQ -> l != r ? 1L : 0L;
        };
    }
}