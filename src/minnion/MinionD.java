package minnion;
import gameState.Position;
import player.Player;

public class MinionD extends Minion {
    public MinionD(Player player, Position position, Player owner, Object strategy){
        super(70,45,35,position,owner,strategy);
    }
}
