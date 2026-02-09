package strategy.ast.expr;

import strategy.ast.Expr;
import strategy.evaluator.EvalContext;
import strategy.runtime.RuntimeError;

public class InfoExpr implements Expr {

    public enum Kind { ALLY, OPPONENT, NEARBY }

    private final Kind kind;
    private final String direction; // ใช้เฉพาะ NEARBY

    public InfoExpr(Kind kind) {
        this(kind, null);
    }

    public InfoExpr(Kind kind, String direction) {
        this.kind = kind;
        this.direction = direction;
    }

    @Override
    public long eval(EvalContext ctx) {
        return switch (kind) {
            case ALLY -> ctx.getSpecialVar("ally");
            case OPPONENT -> ctx.getSpecialVar("opponent");
            case NEARBY -> {
                if (direction == null) {
                    throw new RuntimeError("nearby requires direction");
                }
                yield ctx.getSpecialVar("nearby_" + direction);
            }
        };
    }
}
