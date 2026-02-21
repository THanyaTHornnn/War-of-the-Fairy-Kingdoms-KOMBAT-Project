package gameState;
import java.util.Objects;

public class Position {

    public final int row;
    public final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    //update
    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
    public Position move(Direction direction,int distance) {
        return new Position(
                row + direction.dr * distance,
                col + direction.dc * distance
        );
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position p = (Position)o;
        return row == p.row && col == p.col;
    }
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
//