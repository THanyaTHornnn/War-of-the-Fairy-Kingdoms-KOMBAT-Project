package gameState.minnion;

import gameState.player.Player;
import gameState.Position;

public class MinionB extends Minion {

    public MinionB(Player owner, Position position, Strategy strategy) {
        super(25, 30, 35, position, owner, strategy);
    }
}
