
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
        int r = row;
        int c = col;
        boolean odd = (row % 2 == 1);

        switch (direction) {

            case UP:
                r = row - 1;
                break;

            case DOWN:
                r = row + 1;
                break;

            case UPRIGHT:
                r = row - 1;
                c = odd ? col + 1 : col;
                break;

            case UPLEFT:
                r = row - 1;
                c = odd ? col : col - 1;
                break;

            case DOWNRIGHT:
                r = row + 1;
                c = odd ? col + 1 : col;
                break;

            case DOWNLEFT:
                r = row + 1;
                c = odd ? col : col - 1;
                break;
        }

        return new Position(r, c);
    }

    public Position move(int direction, int steps) {
        Position result = this;
        for (int i = 0; i < steps; i++) {
            result = result.move(direction);
            if (!result.isValid()) break;  // หยุดถ้าออกนอกขอบบ
        }
        return result;
    }


    public boolean isValid() {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    // แปลง offset coordinates →cube coordinates แล้วคำนวณ hex distance
    public int distanceTo(Position other) {
        // แปลง this
        int x1 = this.col - (this.row - (this.row & 1)) / 2;
        int z1 = this.row;
        int y1 = -x1 - z1;

        int x2 = other.col - (other.row - (other.row & 1)) / 2;
        int z2 = other.row;
        int y2 = -x2 - z2;

        return (Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2)) / 2;
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