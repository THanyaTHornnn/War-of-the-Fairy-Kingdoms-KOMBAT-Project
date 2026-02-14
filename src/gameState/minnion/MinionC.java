package gameState.minnion;

import gameState.Position;
import gameState.player.Player;

public class MinionC extends Minion {

    public MinionC(Player owner, Position position) {
        super(70,45,35,position,owner,null);
    }

    public MinionC(Player owner, Position position, Strategy strategy) {
        super(70,45,35,position,owner,strategy);
    }
}
