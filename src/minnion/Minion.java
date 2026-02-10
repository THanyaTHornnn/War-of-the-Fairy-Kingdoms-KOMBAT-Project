package minnion;

import player.Player;
import gameState.Position;
import gameState.GameState;

public abstract class Minion {
    protected int hp;
    protected int attack;
    protected int defense;
    public Position position;
    protected Player owner;
    protected Object strategy;

    protected Minion(int hp, int attack, int defense,
                     Position position, Player owner, Object strategy) {
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.position = position;
        this.owner = owner;
        this.strategy = strategy;
    }

    public int getHp() {
        return hp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public Position getPosition() {
        return position;
    }

    public Player getOwner() {
        return owner;
    }

    public void takeDamage(int damage) {
        hp = Math.max(0, hp - Math.max(0, damage));
    }

    public boolean isDead() {
        return hp <= 0;
    }
    public boolean isAlive() {
        return hp > 0;
    }
}
