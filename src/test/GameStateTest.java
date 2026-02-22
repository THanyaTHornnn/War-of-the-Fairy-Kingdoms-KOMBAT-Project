//package test;
//
//import core.GameState;
//import gameState.*;
//import core.Minion;
//import core.Player;
//import gameState.player.Config;
//import gameState.minnion.*;
//import org.junit.jupiter.api.Test;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class GameStateTest {
//
//
//    static class DummyStrategy implements Strategy {
//        public void execute(Minion me, GameState state) {}
//        public List<strategy.ast.Stmt> getStatements() { return List.of(); }
//    }
//
//    private Minion createMinion(Player p,int r,int c){
//        MinionType type =
//                new MinionType("Test",1,new DummyStrategy());
//
//        Minion m =
//                new Minion(type,10,p,new Position(r,c));
//        return m;
//    }
//
//
//    @Test
//    void testMoveValid() {
//        GameState game = new GameState("A","B");
//        Player p = game.getCurrentPlayer();
//
//        Minion m = createMinion(p,2,2);
//        game.getBoard().placeMinion(m);
//
//        boolean result =
//                GameRules.move(m,Direction.UP,game.getBoard());
//
//        assertTrue(result);
//        assertEquals(1,m.getPosition().getRow());
//        assertEquals(2,m.getPosition().getCol());
//    }
//
//    @Test
//    void testMoveOutOfBoard() {
//        GameState game = new GameState("A","B");
//        Player p = game.getCurrentPlayer();
//
//        Minion m = createMinion(p,0,0);
//        game.getBoard().placeMinion(m);
//
//        boolean result =
//                GameRules.move(m,Direction.UP,game.getBoard());
//
//        assertFalse(result);
//        assertEquals(0,m.getPosition().getRow());
//        assertEquals(0,m.getPosition().getCol());
//    }
//
//
//    @Test
//    void testShootEnemyDamage() {
//        GameState game = new GameState("A","B");
//
//        Player p1 = game.getPlayer1();
//        Player p2 = game.getPlayer2();
//
//        Minion shooter = createMinion(p1,2,2);
//        Minion target  = createMinion(p2,1,2);
//
//        game.getBoard().placeMinion(shooter);
//        game.getBoard().placeMinion(target);
//
//        int before = target.getHp();
//
//        boolean result =
//                GameRules.shoot(shooter,Direction.UP,5,game.getBoard());
//
//        assertTrue(result);
//        assertTrue(target.getHp() < before);
//    }
//
//
//    @Test
//    void testFindOpponentDistance() {
//        GameState game = new GameState("A","B");
//
//        Player p1 = game.getPlayer1();
//        Player p2 = game.getPlayer2();
//
//        Minion me = createMinion(p1,4,4);
//        Minion enemy = createMinion(p2,2,4);
//
//        game.getBoard().placeMinion(me);
//        game.getBoard().placeMinion(enemy);
//
//        int result = GameRules.findOpponent(me,game.getBoard());
//
//        assertTrue(result > 0);
//    }
//
//
//    @Test
//    void testNearbyEnemyPositive() {
//        GameState game = new GameState("A","B");
//
//        Player p1 = game.getPlayer1();
//        Player p2 = game.getPlayer2();
//
//        Minion me = createMinion(p1,3,3);
//        Minion enemy = createMinion(p2,2,3);
//
//        game.getBoard().placeMinion(me);
//        game.getBoard().placeMinion(enemy);
//
//        int val =
//                GameRules.nearby(me,Direction.UP,game.getBoard());
//
//        assertTrue(val > 0);
//    }
//
//    @Test
//    void testNearbyAllyNegative() {
//        GameState game = new GameState("A","B");
//        Player p = game.getPlayer1();
//
//        Minion me = createMinion(p,3,3);
//        Minion ally = createMinion(p,2,3);
//
//        game.getBoard().placeMinion(me);
//        game.getBoard().placeMinion(ally);
//
//        int val =
//                GameRules.nearby(me,Direction.UP,game.getBoard());
//
//        assertTrue(val < 0);
//    }
//
//    @Test
//    void testBudgetOverflow() {
//        Player p = new Player("A",new Config());
//
//        double before = p.getBudget();
//
//        for(int i=0;i<500;i++)
//            p.addTurnBudget();
//
//        double after = p.getBudget();
//
//        assertTrue(after >= before);
//
//        double stable = after;
//        p.addTurnBudget();
//
//        assertEquals(stable,p.getBudget());
//    }
//
//    @Test
//    void testSpawnAddsMinion() {
//        Player p = new Player("A",new Config());
//
//        int before = p.getMinions().size();
//
//        p.addMinion(createMinion(p,1,1));
//
//        assertEquals(before+1,p.getMinions().size());
//    }
//
//    @Test
//    void testSpawnLimit() {
//        Player p = new Player("A",new Config());
//
//        int limit = new Config().maxSpawns;
//
//        for(int i=0;i<limit;i++)
//            p.addMinion(createMinion(p,i,i));
//
//        int before = p.getMinions().size();
//
//        p.addMinion(createMinion(p,9,9));
//
//        assertEquals(before,p.getMinions().size());
//    }
//}
////