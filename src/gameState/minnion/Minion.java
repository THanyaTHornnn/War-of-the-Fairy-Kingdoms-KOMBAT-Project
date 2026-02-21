package gameState.minnion;
import gameState.GameState;
import gameState.Position;
import gameState.player.Player;

public class Minion {

    private final MinionType type;
    private int hp;
    private final Player owner;
    private Position position;

    public Minion(MinionType type, int hp, Player owner, Position position) {
        this.type = type;
        this.hp = hp;
        this.owner = owner;
        this.position = position;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void executeStrategy(GameState state) {
        if (!isAlive()) return;
        type.getStrategy().execute(this, state);
    }


    public int getDefense() {
        return type.getDefense();
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position p) {
        this.position = p;
    }
    public Player getOwner() {
        return owner;
    }
    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }
    public int getHp() {
        return hp;
    }

}