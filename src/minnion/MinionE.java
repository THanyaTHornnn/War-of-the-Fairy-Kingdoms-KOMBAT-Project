package minnion;
import player.Player;
import gameState.Position;
public class MinionE extends Minion{
    public MinionE(Player player, Position position, Player owner, Object strategy){
        super(50,50,50,position,owner,strategy);
    }
}

