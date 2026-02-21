package gameState;

import gameState.minnion.Minion;

public class Board {

    public static final int SIZE = 8;
    private final Hex[][] grid;

    public Board() {
        grid = new Hex[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = new Hex();
            }
        }
    }

    public boolean isInBoard(Position p) {
        return p.row >= 0 && p.row < SIZE &&
                p.col >= 0 && p.col < SIZE;
    }

    public Hex getHex(Position p) {
        if (!isInBoard(p)) {
            throw new IndexOutOfBoundsException(
                    "Out of board: (" + p.row + "," + p.col + ")"
            );
        }
        return grid[p.row][p.col];
    }

    public void placeMinion(Minion m) {
        getHex(m.getPosition()).placeMinion(m);
    }

    public void removeMinion(Position p) {
        getHex(p).removeMinion();
    }

    public void printBoardHex() {
        for (int r = 0; r < SIZE; r++) {

            System.out.print("     ");
            for (int c = 1; c < SIZE; c += 2) {
                System.out.print(" ___     ");
            }
            System.out.println();

            System.out.print("     ");
            for (int c = 1; c < SIZE; c += 2) {
                System.out.print(grid[r][c].isOccupied() ? "/ M \\    " : "/   \\    ");
            }
            System.out.println();

            System.out.print("     ");
            for (int c = 1; c < SIZE; c += 2) {
                System.out.print("\\___/    ");
            }
            System.out.println();

            for (int c = 0; c < SIZE; c += 2) {
                System.out.print(" ___     ");
            }
            System.out.println();

            for (int c = 0; c < SIZE; c += 2) {
                System.out.print(grid[r][c].isOccupied() ? "/ M \\    " : "/   \\    ");
            }
            System.out.println();

            for (int c = 0; c < SIZE; c += 2) {
                System.out.print("\\___/    ");
            }
            System.out.println("\n");
        }
    }
}
//