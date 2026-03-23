package com.kombat.strategy.ast.stmt;

import com.kombat.strategy.ast.Expr;
import com.kombat.strategy.ast.Stmt;
import com.kombat.strategy.evaluator.EvalContext;

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