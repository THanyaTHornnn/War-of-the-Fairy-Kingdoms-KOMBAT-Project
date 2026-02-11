package gameState;
import gameState.minnion.Minion;
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

    public Player getOpponent() {
        return players[1 - currentPlayer];
    }

    public int getTurnCount() {
        return globalTurn;
    }

    public int getMaxTurns() {
        return config.maxTurns;
    }

    public Board getBoard() {
        return board;
    }

    public void switchTurn() {
        if (currentPlayer == 0) {
            currentPlayer = 1;
        } else {
            currentPlayer = 0;
        }
        globalTurn++;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean value) {
        this.gameOver = value;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
        this.gameOver = true;
    }
    public Config getConfig() {
        return config;
    }
    public boolean move(Minion minion, Direction dir) {

        Position current = minion.getPosition();
        Position targetPos = current.move(dir, 1);

        if (!board.isInBoard(targetPos)) return false;

        Hex target = board.getHex(targetPos);
        if (target.isOccupied()) return false;

        board.removeMinion(current);
        minion.setPosition(targetPos);
        board.placeMinion(minion);

        return true;
    }
    public static boolean shoot(Minion shooter, Direction dir, long dmg, Board board) {

        Position current = shooter.getPosition();
        Position targetPos = current.move(dir, 1);

        if (!board.isInBoard(targetPos)) return false;

        Hex hex = board.getHex(targetPos);
        if (!hex.isOccupied()) return false;

        Minion target = hex.getOccupant();
        target.takeDamage((int) dmg);

        if (!target.isAlive()) {
            board.removeMinion(target.getPosition());
            target.getOwner().removeMinion(target);
        }

        return true;
    }


}
