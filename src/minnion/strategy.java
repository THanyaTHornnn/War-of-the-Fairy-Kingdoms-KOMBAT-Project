package minnion;
import gameState.GameState;

public interface strategy {
    void execute(Minion me, GameState gameState);
}