package gameState;

import gameState.minnion.Minion;

public class Hex {
    private Minion occupant;
    private boolean spawnable;

    public boolean isSpawnable() {
        return spawnable;
    }

    public void setSpawnable(boolean spawnable) {
        this.spawnable = spawnable;
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
}
