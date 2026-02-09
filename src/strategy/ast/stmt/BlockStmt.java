package strategy.ast.stmt;

import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;

import java.util.List;

public class BlockStmt implements Stmt {

    private final List<Stmt> statements;

    public BlockStmt(List<Stmt> statements) {
        this.statements = statements;
    }

    @Override
    public void execute(EvalContext ctx) {
        for (Stmt s : statements) {
            s.execute(ctx);
        }
    }
}
