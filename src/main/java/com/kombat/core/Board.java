package com.kombat.core;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private static final int ROWS = 8;
    private static final int COLS = 8;

    //ตำแหน่งนี้ อยู่ในกระดานและใช้ได้ไหม
    public static boolean isValid(Position pos) {
        if (pos == null) return false;
        return pos.getRow() >= 1 && pos.getRow() <= ROWS
                && pos.getCol() >= 1 && pos.getCol() <= COLS;
    }

    public static List<Position> neighbors(Position pos) {
        List<Position> result = new ArrayList<>();
        // เมื่อ Position.move แก้แล้ว Loop นี้จะหาเพื่อนบ้านรอบตัวได้ถูกต้องตามหน้าจอ
        for (int dir = Position.UP; dir <= Position.UPLEFT; dir++) {
            Position n = pos.move(dir);
            if (isValid(n)) result.add(n);
        }
        return result;
    }

    public static boolean isAdjacent(Position a, Position b) {
        if (a == null || b == null) return false;

        int row = a.getRow();
        int col = a.getCol();

        boolean isOdd = (col % 2 != 0);

        int[][] dirs = isOdd
                ? new int[][]{
                {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {1, -1}, {1, 1}
        }
                : new int[][]{
                {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}
        };

        for (int[] d : dirs) {
            if (row + d[0] == b.getRow() &&
                    col + d[1] == b.getCol()) {
                return true;
            }
        }

        return false;
    }
    //สร้างทุกช่องในกระดาน
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