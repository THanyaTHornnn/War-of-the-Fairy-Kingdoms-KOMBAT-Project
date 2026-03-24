package com.kombat.core;

import java.util.Objects;

public class Position {

    private final int row;
    private final int col;

    // ทิศทาง (ห้ามเปลี่ยนเลข)
    public static final int UP = 1;
    public static final int UPRIGHT = 2;
    public static final int DOWNRIGHT = 3;
    public static final int DOWN = 4;
    public static final int DOWNLEFT = 5;
    public static final int UPLEFT = 6;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    // 🔥 move แบบ HEX (ตรง frontend)
    public Position move(int dir) {
        boolean isOdd = (col % 2 != 0);

        int dr = 0;
        int dc = 0;

        if (isOdd) {
            switch (dir) {
                case UP:        dr = -1; dc = 0;  break;
                case DOWN:      dr = 1;  dc = 0;  break;

                case UPRIGHT:   dr = 0;  dc = 1;  break;
                case DOWNRIGHT: dr = 1;  dc = 1;  break;

                case DOWNLEFT:  dr = 1;  dc = -1; break;
                case UPLEFT:    dr = 0;  dc = -1; break;

                default: throw new IllegalArgumentException("Invalid direction: " + dir);
            }
        } else {
            switch (dir) {
                case UP:        dr = -1; dc = 0;  break;
                case DOWN:      dr = 1;  dc = 0;  break;

                case UPRIGHT:   dr = -1; dc = 1;  break;
                case DOWNRIGHT: dr = 0;  dc = 1;  break;

                case DOWNLEFT:  dr = 0;  dc = -1; break;
                case UPLEFT:    dr = -1; dc = -1; break;

                default: throw new IllegalArgumentException("Invalid direction: " + dir);
            }
        }

        return new Position(row + dr, col + dc);
    }

    // ✅ ใช้เช็คขอบกระดาน
    public boolean isValid() {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    // ❌ ห้ามใช้กับ adjacency (เก็บไว้ใช้เฉยๆ)
    public int distanceTo(Position other) {
        if (other == null) return Integer.MAX_VALUE;
        return Math.max(
                Math.abs(this.row - other.row),
                Math.abs(this.col - other.col)
        );
    }

    // 🔁 string → Position
    public static Position fromString(String s) {
        try {
            String[] parts = s.split(",");
            return new Position(
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim())
            );
        } catch (Exception e) {
            return new Position(0, 0);
        }
    }

    // 🔁 Position → string
    @Override
    public String toString() {
        return row + "," + col;
    }

    // ✅ เทียบตำแหน่ง
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return row == p.row && col == p.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}