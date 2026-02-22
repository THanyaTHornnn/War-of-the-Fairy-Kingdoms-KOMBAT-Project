package core;

public class Position {
    private final int row;
    private final int col;

    public static final int UP        = 1;
    public static final int UPRIGHT   = 2;
    public static final int DOWNRIGHT = 3;
    public static final int DOWN      = 4;
    public static final int DOWNLEFT  = 5;
    public static final int UPLEFT    = 6;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public Position move(int direction) {
        boolean odd = (row % 2 == 1);
        int r = row, c = col;
        switch (direction) {
            case UP:        r = row - 1; break;
            case DOWN:      r = row + 1; break;
            case UPRIGHT:   r = row - 1; c = odd ? col + 1 : col; break;
            case DOWNRIGHT: r = row + 1; c = odd ? col + 1 : col; break;
            case UPLEFT:    r = row - 1; c = odd ? col : col - 1; break;
            case DOWNLEFT:  r = row + 1; c = odd ? col : col - 1; break;
        }
        return new Position(r, c);
    }

    public boolean isValid() {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    public int distanceTo(Position other) {
        return Math.abs(row - other.row) + Math.abs(col - other.col);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position p)) return false;
        return row == p.row && col == p.col;
    }

    @Override
    public int hashCode() { return 31 * row + col; }

    @Override
    public String toString() { return row + "," + col; }

    public static Position fromString(String s) {
        String[] p = s.split(",");
        return new Position(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
    }
}