package gameState;

public enum Direction {
    UP(1, -1, 0),
    UPRIGHT(2, -1, 1),
    DOWNRIGHT(3, 0, 1),
    DOWN(4, 1, 0),
    DOWNLEFT(5, 1, -1),
    UPLEFT(6, 0, -1);
    public final int code;
    public final int dc;
    public final int dr;
    Direction(int code, int dr, int dc) {
        this.code = code;
        this.dc = dc;
        this.dr = dr;
    }


}
