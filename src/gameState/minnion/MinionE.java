package gameState.minnion;

import gameState.Position;
import gameState.player.Player;
public class MinionE extends Minion {

    public MinionE(Player owner, Position position){
        super(50,50,50,position,owner,null);
    }

    public MinionE(Player owner, Position position, Strategy strategy){
        super(50,50,50,position,owner,strategy);
    }
}


