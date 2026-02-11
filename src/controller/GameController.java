package controller;

import gameState.GameState;

public class GameController {

    private final GameState gameState;
    private final TurnManagement turnManagement;
    private final EndgameChecker endgameChecker;

    public GameController(GameState gameState) {
        this.gameState = gameState;
        this.turnManagement = new TurnManagement(gameState);
        this.endgameChecker = new EndgameChecker(gameState);
    }

    /**
     * เรียกเมื่อผู้เล่นกด End Turn
     */
    public void nextTurn() {

        if (gameState.isGameOver()) {
            return;
        }

        turnManagement.endTurn();

        if (endgameChecker.checkEndgame()) {
            gameState.setGameOver(true);
        }
    }

    public boolean isGameOver() {
        return gameState.isGameOver();
    }

    public GameState getGameState() {
        return gameState;
    }
}
