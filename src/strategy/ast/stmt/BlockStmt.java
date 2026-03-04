package strategy.ast.stmt;

import strategy.ast.Stmt;
import strategy.evaluator.EvalContext;

import java.util.Collection;
import java.util.List;

public class BlockStmt implements Stmt {

    private final List<Stmt> statements;

    public BlockStmt(List<Stmt> statements) {
        this.statements = statements;
    }

    @Override
    public void execute(EvalContext ctx) {
        for (Stmt stmt : statements) {
            stmt.execute(ctx);
        }
    }

    public List<Stmt> getStatements() {
        return statements;
    }
}
