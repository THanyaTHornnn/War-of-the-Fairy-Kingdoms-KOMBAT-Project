package core;

import java.util.ArrayList;
import java.util.List;

// Hex grid 8x8 — เก็บข้อมูล grid และช่วย query ตำแหน่ง
public class Board {
    private static final int ROWS = 8;
    private static final int COLS = 8;

    // ── Valid position check ──────────────────────────────────
    public static boolean isValid(Position pos) {
        return pos.getRow() >= 1 && pos.getRow() <= ROWS
                && pos.getCol() >= 1 && pos.getCol() <= COLS;
    }

    // ── Get all valid neighbors ───────────────────────────────
    public static List<Position> neighbors(Position pos) {
        List<Position> result = new ArrayList<>();
        for (int dir = Position.UP; dir <= Position.UPLEFT; dir++) {
            Position n = pos.move(dir);
            if (isValid(n)) result.add(n);
        }
        return result;
    }


    // ── Check adjacency ───────────────────────────────────────
    public static boolean isAdjacent(Position a, Position b) {
        for (int dir = Position.UP; dir <= Position.UPLEFT; dir++) {
            if (a.move(dir).equals(b)) return true;
        }
        return false;
    }

    // ── Get all positions on board ────────────────────────────
    public static List<Position> allPositions() {
        List<Position> all = new ArrayList<>();
        for (int r = 1; r <= ROWS; r++)
            for (int c = 1; c <= COLS; c++)
                all.add(new Position(r, c));
        return all;
    }

    public static int getRows() { return ROWS; }
    public static int getCols() { return COLS; }
}