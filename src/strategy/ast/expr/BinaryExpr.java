package strategy.ast.expr;

import strategy.ast.Expr;
import strategy.evaluator.EvalContext;
import strategy.runtime.RuntimeError;

public class BinaryExpr implements Expr {

    public enum Op { ADD, SUB, MUL, DIV, MOD }

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
            throw new RuntimeError("division by zero");
        }

        return switch (op) {
            case ADD -> l + r;
            case SUB -> l - r;
            case MUL -> l * r;
            case DIV -> l / r;
            case MOD -> l % r;
        };
    }
}
