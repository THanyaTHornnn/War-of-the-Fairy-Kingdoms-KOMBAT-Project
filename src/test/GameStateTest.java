package test;
import gameState.Direction;
import gameState.GameState;
import gameState.player.Config;
import gameState.player.Player;
import gameState.GameRules;
import gameState.minnion.Minion;
import gameState.minnion.MinionA;
import gameState.Position;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    void testMoveValid() {
        GameState game = new GameState("A","B");
        Player p = game.getCurrentPlayer();

        Minion m = new MinionA(p, new Position(2,2));
        game.getBoard().placeMinion(m);

        boolean result = GameRules.move(m, Direction.UP, game.getBoard());

        assertTrue(result);
        assertEquals(1, m.getPosition().getRow());
        assertEquals(2, m.getPosition().getCol());
    }

    @Test
    void testMoveOutOfBoard() {
        GameState game = new GameState("A","B");
        Player p = game.getCurrentPlayer();

        Minion m = new MinionA(p, new Position(0,0));
        game.getBoard().placeMinion(m);

        boolean result = GameRules.move(m, Direction.UP, game.getBoard());

        assertFalse(result);
        assertEquals(0, m.getPosition().getRow());
        assertEquals(0, m.getPosition().getCol());
    }

    @Test
    void testShootEnemy() {
        GameState game = new GameState("A","B");

        Player p1 = game.getPlayer1();
        Player p2 = game.getPlayer2();

        Minion shooter = new MinionA(p1,new Position(2,2));
        Minion target = new MinionA(p2,new Position(1,2));

        game.getBoard().placeMinion(shooter);
        game.getBoard().placeMinion(target);

        int before = target.getHp();

        boolean result =
                GameRules.shoot(shooter,Direction.UP,5,game.getBoard());

        assertTrue(result);
        assertTrue(target.getHp() < before);
    }

    @Test
    void testBudgetOverflow() {
        GameState game = new GameState("A","B");
        Player p = game.getPlayer1();

        double prev = p.getBudget();

        for(int i=0;i<200;i++)
            p.addTurnBudget();

        double after = p.getBudget();

        assertTrue(after >= prev);

        double stable = after;
        p.addTurnBudget();

        assertEquals(stable, p.getBudget());
    }

    @Test
    void testSpawnAddsMinion() {
        GameState game = new GameState("A","B");
        Player p = game.getPlayer1();

        int before = p.getMinions().size();

        p.addMinion(new MinionA(p,new Position(1,1)));

        assertEquals(before+1, p.getMinions().size());
    }
    @Test
    void testSpawnLimit() {
        GameState game = new GameState("A","B");
        Player p = game.getPlayer1();

        int limit = new Config().maxSpawns; // หรือกำหนดเลขตรง เช่น 5

        for(int i=0;i<limit;i++){
            p.addMinion(new MinionA(p,new Position(i,i)));
        }

        int before = p.getMinions().size();

        p.addMinion(new MinionA(p,new Position(9,9)));

        int after = p.getMinions().size();

        assertEquals(before, after);
    }

}
