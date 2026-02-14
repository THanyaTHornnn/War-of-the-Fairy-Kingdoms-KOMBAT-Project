package gameState.minnion;
import gameState.Position;
import gameState.player.Player;

public class MinionD extends Minion {

    public MinionD(Player owner, Position position){
        super(70,45,35,position,owner,null);
    }

    public MinionD(Player owner, Position position, Strategy strategy){
        super(70,45,35,position,owner,strategy);
    }
}

