package minnion;

import player.Player;
import gameState.Position;

public class MinionC extends Minion {
    public MinionC(Player player, Position position, Player owner, Object strategy){
        super(70,45,35,position,owner,strategy);
    }
}
