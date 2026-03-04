package test;

import core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class CombatTest {
    @Test
    void testShootWhenAdjacent() {

        Position attacker = new Position(4,4);
        Position target = attacker.move(Position.UP);

        boolean canAttack = Board.isAdjacent(attacker, target);

        assertTrue(canAttack);
    }

    @Test
    void testCannotShootWhenNotAdjacent() {

        Position attacker = new Position(4,4);
        Position target = new Position(7,7);

        boolean canAttack = Board.isAdjacent(attacker, target);

        assertFalse(canAttack);
    }
//ยิงศัตรูที่ติดกัน → ต้องโดน
@Test
void shoot_shouldHitAdjacentEnemy() {

    Config config = new Config();
    GameLogic game = new GameLogic(config, GameState.Mode.AUTO);
    game.initBudgets();

    Player p1 = game.getP1();
    Player p2 = game.getP2();

    Minion m1 = Minion.create("A", "m1", p1, new Position(4,4), 100);
    Minion m2 = Minion.create("A", "m2", p2, new Position(5,4), 100);

    game.getP1().addSpawnableHex(new Position(4,4));
    game.getP2().addSpawnableHex(new Position(5,4));

    assertTrue(game.spawnMinion("p1", m1));
    assertTrue(game.spawnMinion("p2", m2));

    boolean result = game.shoot(m1, Position.DOWN, 10);

    assertTrue(result);
    assertTrue(m2.getHp() < 100);
}
//ยิงศัตรูที่ไม่ติดกัน → ต้องพลาด
    @Test
    void shoot_shouldFailIfEnemyNotAdjacent() {

        Config config = new Config();
        GameLogic game = new GameLogic(config, GameState.Mode.AUTO);

        Player p1 = game.getP1();
        Player p2 = game.getP2();

        Minion m1 = Minion.create("A", "m1", p1, new Position(4,4), 100);
        Minion m2 = Minion.create("A", "m2", p2, new Position(6,6), 100);

        game.spawnMinion("p1", m1);
        game.spawnMinion("p2", m2);

        boolean result = game.shoot(m1,Position.DOWN, 10);

        assertFalse(result);
        assertEquals(100, m2.getHp());
    }
//ยิงเพื่อน → ต้องยิงไม่ได้
    @Test
    void shoot_shouldFailIfTargetIsAlly() {

        Config config = new Config();
        GameLogic game = new GameLogic(config, GameState.Mode.AUTO);

        Player p1 = game.getP1();

        Minion m1 = Minion.create("A", "m1", p1, new Position(4,4), 100);
        Minion m2 = Minion.create("A", "m2", p1, new Position(5,4), 100);

        game.spawnMinion("p1", m1);
        game.spawnMinion("p1", m2);

        boolean result = game.shoot(m1, Position.DOWN, 10);

        assertFalse(result);
        assertEquals(100, m2.getHp());
    }
//ยิงช่องว่าง → ต้องยิงไม่ได้
    @Test
    void shoot_shouldFailIfNoTarget() {

        Config config = new Config();
        GameLogic game = new GameLogic(config, GameState.Mode.AUTO);

        Player p1 = game.getP1();

        Minion m1 = Minion.create("A", "m1", p1, new Position(4,4), 100);
        game.spawnMinion("p1", m1);

        boolean result = game.shoot(m1, Position.DOWN, 10);

        assertFalse(result);
    }
}
