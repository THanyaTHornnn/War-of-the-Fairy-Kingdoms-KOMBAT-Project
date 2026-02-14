package test;
import gameState.GameState;
import gameState.player.Player;
import gameState.minnion.Minion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {
    @Test
    void initialStateCorrect() {
        GameState g = new GameState("A","B");

        assertEquals("A", g.getPlayer1().getName());
        assertEquals("B", g.getPlayer2().getName());
        assertEquals(g.getPlayer1(), g.getCurrentPlayer());
        assertEquals(1, g.getTurnCount());
        assertFalse(g.isGameOver());
        assertNull(g.getWinner());
        assertNotSame(g.getPlayer1(), g.getPlayer2());
    }


    @Test
    void switchTurnChangesPlayer() {
        GameState g = new GameState("A","B");

        Player first = g.getCurrentPlayer();
        g.switchTurn();

        assertNotEquals(first, g.getCurrentPlayer());
    }

    @Test //เช็ค pattern
    void switchTurnIncreasesTurnCount() {
        GameState g = new GameState("A","B");

        g.switchTurn();
        assertEquals(2, g.getTurnCount());

        g.switchTurn();
        assertEquals(3, g.getTurnCount());
    }

    @Test// เช็ค patternารสลับ
    void alternatingTurnsStable() {
        GameState g = new GameState("A","B");

        Player p1 = g.getCurrentPlayer();
        g.switchTurn();
        Player p2 = g.getCurrentPlayer();
        g.switchTurn();
        Player p3 = g.getCurrentPlayer();

        assertEquals(p1, p3);
        assertNotEquals(p1, p2);
    }

    @Test
    void setWinnerWorks() {
        GameState g = new GameState("A","B");

        Player p = g.getPlayer1();
        g.setWinner(p);

        assertTrue(g.isGameOver());
        assertEquals(p, g.getWinner());
    }

    @Test
    void drawCase() {
        GameState g = new GameState("A","B");

        g.setWinner(null);

        assertTrue(g.isGameOver());
        assertNull(g.getWinner());
    }

    @Test
    void currentPlayerStableReference() {
        GameState g = new GameState("A","B");

        Player p1 = g.getCurrentPlayer();
        Player p2 = g.getCurrentPlayer();

        assertSame(p1,p2);
    }

    @Test //ตรวจว่า external reference ไม่ทำให้ state ภายในเปลี่ยน GameState ต้องยังมี player อยู่
    void playerObjectsImmutableOutside() {
        GameState g = new GameState("A","B");

        Player external = g.getPlayer1();
        external = null;

        assertNotNull(g.getPlayer1());
    }

    @Test
    void manySwitchTurnsStable() {
        GameState g = new GameState("A","B");

        for(int i=0;i<1000;i++)
            g.switchTurn();

        assertNotNull(g.getCurrentPlayer());
        assertFalse(g.isGameOver());
    }

    @Test //เรียกซ้ำต้องไม่เปลี่ยน winner
    void repeatedSetWinnerSafe() {
        GameState g = new GameState("A","B");

        g.setWinner(g.getPlayer1());
        g.setWinner(g.getPlayer1());
        g.setWinner(g.getPlayer1());

        assertTrue(g.isGameOver());
        assertEquals(g.getPlayer1(), g.getWinner());
    }

}