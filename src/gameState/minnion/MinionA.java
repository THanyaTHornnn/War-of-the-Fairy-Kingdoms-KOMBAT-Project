package gameState.minnion; import gameState.Position; import gameState.player.Player;
public class MinionA extends Minion {

    public MinionA(Player owner, Position position) {
        super(50,60,40,position,owner,null);
    }

    public MinionA(Player owner, Position position, Strategy strategy) {
        super(50,60,40,position,owner,strategy);
    }
}
