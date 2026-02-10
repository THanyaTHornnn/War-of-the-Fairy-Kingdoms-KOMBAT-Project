package gameState.minnion;
import gameState.player.Player;
import gameState.Position;
public class MinionE extends Minion{
    public MinionE(Player player, Position position, Player owner,Strategy strategy){
        super(50,50,50,position,owner,strategy);
    }
}

