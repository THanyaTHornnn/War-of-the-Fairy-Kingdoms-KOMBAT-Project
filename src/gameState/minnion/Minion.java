package gameState.minnion;

import gameState.GameState;
import gameState.Position;
import gameState.player.Player;

public abstract class Minion {

    protected int hp;
    protected int attack;
    protected int defense;
    protected Position position;
    protected Player owner;
    protected Strategy strategy;

    protected Minion(int hp, int attack, int defense,
                     Position position, Player owner, Strategy strategy) {
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.position = position;
        this.owner = owner;
        this.strategy = strategy;
    }

    /* ================= behavior ================= */

    public void executeStrategy(GameState gameState) {
        if (strategy != null && isAlive()) {
            strategy.execute(this, gameState);
        }
    }

    public boolean isAlive() {
        return hp > 0;
    }

    /* ================= getters ================= */

    public int getHp() {
        return hp;
    }

    public Player getOwner() {
        return owner;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
