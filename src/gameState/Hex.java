package gameState;

import gameState.minnion.Minion;
import gameState.player.Player;

public class Hex {
    private Minion occupant;
    private boolean spawnable;
    private Player owner;
    private Position position;

    public boolean isSpawnable() {
        return spawnable;
    }

    public void setSpawnable(boolean spawnable) {
        this.spawnable = spawnable;
    }
    public Position getPosition() {
        return position;
    }

    public Minion getOccupant() {
        return occupant;
    }

    public boolean isOccupied() {
        return occupant != null;
    }

    public void placeMinion(Minion minion) {
        if (minion == null) {
            throw new IllegalArgumentException("Minion cannot be null");
        }
        if (isOccupied()) {
            throw new IllegalStateException("Hex is already occupied");
        }
        occupant = minion;
    }

    public Minion removeMinion() {
        if (!isOccupied()) {
            throw new IllegalStateException("Hex is empty");
        }
        Minion removed = occupant;
        occupant = null;
        return removed;
    }
    public Player getOwner() {
        return owner;
    }

    public void setOwnerHex(Player owner) {
        this.owner = owner;
    }
    public boolean isAdjacentToOwned(Player player, Board board) {

        Position pos = this.getPosition();

        for (Direction d : Direction.values()) {
            Position p = pos.move(d, 1);
            if (!board.isInBoard(p)) continue;

            Hex neighbor = board.getHex(p);
            if (neighbor.getOwner() == player)
                return true;
        }
        return false;
    }
}
