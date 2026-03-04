package strategy.ast.stmt;

import strategy.ast.Expr;
import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;

public class ShootStmt implements Stmt {

    private final int direction;
    private final Expr damage;

    public ShootStmt(int direction, Expr damage) {
        this.direction = direction;
        this.damage = damage;
    }

    @Override
    public void execute(EvalContext ctx) {
        long dmg = damage.eval(ctx);
        ctx.shoot(direction, dmg);
    }
}