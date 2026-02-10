package gameState.minnion;

import gameState.player.Player;
import gameState.Position;
import gameState.minnion.Minion;

public class MinionC extends Minion {
    public MinionC(Player player, Position position, Player owner, Strategy strategy){
        super(70,45,35,position,owner,strategy);
    }
}
