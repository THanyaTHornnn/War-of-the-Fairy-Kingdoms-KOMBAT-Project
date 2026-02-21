package gameState.minnion;
public class MinionType {

    private final String name;
    private final int defense;
    private final Strategy strategy;

    public MinionType(String name, int defense, Strategy strategy) {
        this.name = name;
        this.defense = defense;
        this.strategy = strategy;
    }

    public String getName() {
        return name;
    }

    public int getDefense() {
        return defense;
    }

    public Strategy getStrategy() {
        return strategy;
    }
}
//