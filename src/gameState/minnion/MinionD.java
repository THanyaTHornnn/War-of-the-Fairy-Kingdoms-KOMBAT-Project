package gameState.minnion;
import gameState.Position;
import gameState.player.Player;

public class MinionD extends Minion {
    public MinionD(Player player, Position position, Player owner, Strategy strategy){
        super(70,45,35,position,owner,strategy);
    }
}
