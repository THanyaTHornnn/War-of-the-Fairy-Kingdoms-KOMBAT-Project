package gameState;
public class Board {

    public static final int SIZE = 8;
    private final Hex[][] board;

    public Board() {
        board = new Hex[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col] = new Hex(row, col);
            }
        }
    }

    public boolean isInBoard(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    public Hex getHex(int row, int col) {
        if (!isInBoard(row, col)) {
            throw new IndexOutOfBoundsException(
                    "Out of board: (" + row + "," + col + ")"
            );
        }
        return board[row][col];
    }

    public void placeMinion(Minion minion, int row, int col) {
        getHex(row, col).placeMinion(minion);
    }

    public void removeMinion(int row, int col) {
        getHex(row, col).removeMinion();
    }

    public void printBoardHexLikeSpec() {
        for (int row = 0; row < SIZE; row++) {

            /* ---------- บรรทัดบน : col คู่ ---------- */
            System.out.print("     ");
            for (int col = 1; col < SIZE; col += 2) {
                System.out.print("  ___     ");
            }
            System.out.println();

            System.out.print("     ");
            for (int col = 1; col < SIZE; col += 2) {
                if (board[row][col].isEmpty()) {
                    System.out.print(" /   \\    ");
                } else {
                    System.out.print(" / M \\    ");
                }
            }
            System.out.println();

            System.out.print("     ");
            for (int col = 1; col < SIZE; col += 2) {
                System.out.print(" \\___/    ");
            }
            System.out.println();

            for (int col = 0; col < SIZE; col += 2) {
                System.out.print("  ___     ");
            }
            System.out.println();

            for (int col = 0; col < SIZE; col += 2) {
                if (board[row][col].isEmpty()) {
                    System.out.print(" /   \\    ");
                } else {
                    System.out.print(" / M \\    ");
                }
            }
            System.out.println();

            for (int col = 0; col < SIZE; col += 2) {
                System.out.print(" \\___/    ");
            }
            System.out.println("\n");
        }
    }
}