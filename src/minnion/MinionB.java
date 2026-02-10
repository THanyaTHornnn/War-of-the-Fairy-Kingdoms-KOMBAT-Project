package minnion;

import player.Player;
import gameState.Position;

public class MinionB extends Minion {
    public MinionB(Player player, Position position, Player owner, Object strategy){
        super(25,30,35,position,owner,strategy);
    }
}
