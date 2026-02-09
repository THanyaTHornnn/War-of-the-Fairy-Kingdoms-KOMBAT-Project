package gameState;

public class Minion {
    private int Hp;
    private int Defense;
    private int row;
    private int col;
    private final Object strategy;

    public Minion(int Hp, int Defense, int row, int col, Object strategy) {
        this.Hp = Hp;
        this.Defense = Defense;
        this.row = row;
        this.col = col;
        this.strategy = strategy;
    }

}
