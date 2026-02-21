package gameState.minnion;
import gameState.GameState;
import strategy.ast.Stmt;

import java.util.List;

public interface Strategy {
    void execute(Minion me, GameState gameState);

    List<Stmt> getStatements();
}
 //
