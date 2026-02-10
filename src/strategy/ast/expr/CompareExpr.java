package strategy.ast.expr;

import strategy.ast.Expr;
import strategy.evaluator.EvalContext;

public class CompareExpr implements Expr {

    public enum Op {
        LT, LE, GT, GE, EQ, NE
    }

    private final Expr left;
    private final Expr right;
    private final Op op;

    public CompareExpr(Expr left, Op op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public long eval(EvalContext ctx) {
        long l = left.eval(ctx);
        long r = right.eval(ctx);

        boolean result = switch (op) {
            case LT -> l < r;
            case LE -> l <= r;
            case GT -> l > r;
            case GE -> l >= r;
            case EQ -> l == r;
            case NE -> l != r;
        };

        return result ? 1 : 0;
    }
}
