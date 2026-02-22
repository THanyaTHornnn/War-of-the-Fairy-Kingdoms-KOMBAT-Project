package strategy.ast.stmt;

import strategy.ast.Expr;
import strategy.ast.ExprUtils;
import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;



public class WhileStmt implements Stmt {

    private final Expr condition;
    private final Stmt body;

    public WhileStmt(Expr condition, Stmt body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void execute(EvalContext ctx) {
        while (ExprUtils.isTrue(condition.eval(ctx))) {
            if (ctx.isDone()) break;

            //loop iteration ต้องเสีย budget เพื่อให้ strategy execution เป็น finite และป้องกัน infinite loop ที่ไม่มี action ซึ่ง spec อนุญาตให้เขียนได้
            ctx.consumeBudget(1);
            if (ctx.isDone()) break;
            body.execute(ctx);

            if (ctx.isDone()) break;
        }
    }
}
