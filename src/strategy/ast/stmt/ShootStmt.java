package strategy.ast.stmt;

import gameState.Direction;
import strategy.ast.Expr;
import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;

public class ShootStmt implements Stmt {

    private final Direction direction;
    private final Expr damage;

    public ShootStmt(Direction direction, Expr damage) {
        this.direction = direction;
        this.damage = damage;
    }

    @Override
    public void execute(EvalContext ctx) {
        long dmg = damage.eval(ctx);
        ctx.shoot( direction , dmg); //*

    }
}
