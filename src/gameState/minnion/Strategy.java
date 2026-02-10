package gameState.minnion;

import gameState.GameState;

public interface Strategy {
    void execute(Minion me, GameState gameState);
}
