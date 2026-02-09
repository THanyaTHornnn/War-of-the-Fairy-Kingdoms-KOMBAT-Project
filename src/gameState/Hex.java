package gameState;

public class Hex {
    private final int row, col;
    private Minion occupant;

    public Hex(int row, int col) {
        this.row = row;
        this.col = col;
        this.occupant = null;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isEmpty() {
        return occupant == null;
    }

    public Minion getOccupant() {
        return occupant;
    }

    public void placeMinion(Minion minion) {
        if (minion == null) {
            throw new IllegalArgumentException("GameSate.Minion cannot be null");
        }
        if (!isEmpty()) {
            throw new IllegalStateException("GameSate.Hex is already occupied");
        }
        this.occupant = minion;

    }

    public Minion removeMinion() {
        if (isEmpty()) {
            throw new IllegalStateException("GameSate.Hex is already occupied");
        }
        Minion removed = occupant;
        occupant = null;
        return removed;
    }


}
