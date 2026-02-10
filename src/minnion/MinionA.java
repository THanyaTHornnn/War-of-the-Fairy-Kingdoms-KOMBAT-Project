package minnion;
import player.Player;
import gameState.Position;
public class MinionA extends Minion {
    public MinionA(Player player, Position position,Player owner,Object strategy){
        super(50, 60,40,position,owner,strategy);
    }
}
