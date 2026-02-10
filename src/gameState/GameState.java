package gameState;

import gameState.player.Player;
import gameState.player.Config;

public class GameState {

    private final Board board;
    private final Player[] players;
    private final Config config;

    private int currentPlayer;
    private int globalTurn;
    private boolean gameOver;
    private Player winner;

    public GameState(String p1Name, String p2Name) {
        this.config = new Config();
        this.board = new Board();
        this.players = new Player[] {
                new Player(p1Name, config),
                new Player(p2Name, config)
        };
        this.currentPlayer = 0;
        this.globalTurn = 1;
        this.gameOver = false;
        this.winner = null;
    }

    public Player getPlayer1() {
        return players[0];
    }
    public Player getPlayer2() {
        return players[1];
    }
    public Player getCurrentPlayer() {
        return players[currentPlayer];
    }
    public int getTurnCount() {
        return globalTurn;
    }

    public int getMaxTurns() {
        return config.maxTurns;
    }
    public void switchTurn() {
        currentPlayer = 1 - currentPlayer;
        globalTurn++;
    }
    public boolean isGameOver() {
        return gameOver;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
        this.gameOver = true;
    }
}
