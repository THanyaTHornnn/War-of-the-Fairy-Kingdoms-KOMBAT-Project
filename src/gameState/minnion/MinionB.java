package gameState.minnion;

import gameState.Position;
import gameState.player.Player;

public class MinionB extends Minion {

    public MinionB(Player owner, Position position) {
        super(25, 30, 35, position, owner, null);
    }

    public MinionB(Player owner, Position position, Strategy strategy) {
        super(25, 30, 35, position, owner, strategy);
    }
}