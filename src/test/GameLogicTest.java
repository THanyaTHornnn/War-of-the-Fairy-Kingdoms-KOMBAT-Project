package test;

import core.*;
import controller.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameLogicTest {

    GameLogic game;
    Player p1;
    Player p2;

    @BeforeEach
    void init(){
        Config c = new Config();
        c.initBudget=100;
        c.turnBudget=10;
        c.maxBudget=200;
        c.spawnCost=5;
        c.hexPurchaseCost=5;
        c.maxSpawns=2;
        c.maxTurns=100;
        c.interestPct=10;

        game = new GameLogic(c, GameState.Mode.AUTO);
        game.initBudgets();
        game.startGame();

        p1 = game.getP1();
        p2 = game.getP2();
        p2.addSpawnableHex(new Position(5,2));
        p1.addSpawnableHex(new Position(7,7));
        p1.addSpawnableHex(new Position(3,3));
        p2.addSpawnableHex(new Position(4,3));

    }

    private Minion spawn(Player p,int r,int c){
        Minion m = Minion.create(
                "A",
                game.generateMinionId(),
                p,
                new Position(r,c),
                10,
                1
        );
        assertTrue(game.spawnMinion(p.getId(),m));
        return m;
    }

    // =================================================
    // SPAWN
    // =================================================

    @Test
    void spawnSuccess(){
        Minion m = spawn(p1,2,2);
        assertNotNull(game.getMinionAt(new Position(2,2)));
    }

    @Test
    void spawnFailWhenOccupied(){
        spawn(p1,2,2);

        Minion m = Minion.create("A",game.generateMinionId(),p1,new Position(2,2),10,1);

        assertFalse(game.spawnMinion(p1.getId(),m));
    }

    @Test
    void spawnLimit(){
        spawn(p1,1,1);
        spawn(p1,1,2);

        Minion extra = Minion.create("A",game.generateMinionId(),p1,new Position(1,3),10,1);
        assertFalse(game.spawnMinion(p1.getId(),extra));
    }

    // =================================================
    // MOVE
    // =================================================

    @Test
    void Movevalid(){
        Minion m = spawn(p1,2,2);
        assertTrue(game.move(m,Position.DOWN));
        assertEquals(3,m.getPosition().getRow());
    }


    @Test
    void moveOutOfBoard(){
        Minion m = spawn(p1,1,1);
        assertFalse(game.move(m,Position.UP));
    }

    // =================================================
    // SHOOT
    // =================================================


    @Test
    void shootKillsEnemy() {
        p1.setBudget(100);

        Minion atk = spawn(p1, 3, 3);  // spawnable อยู่แล้ว
        // spawn p2 ติดกับ atk ทิศ DOWN
        Position tgtPos = atk.getPosition().move(Position.DOWN);
        Minion tgt = Minion.create("A", game.generateMinionId(), p2,
                tgtPos, 1, 0); // HP=1 ให้ตายแน่นอน
        p2.addSpawnableHex(tgtPos);
        assertTrue(game.spawnMinion(p2.getId(), tgt));

        assertTrue(game.shoot(atk, Position.DOWN, 10));
        assertFalse(tgt.isAlive());
        assertNull(game.getMinionAt(tgtPos)); // ต้องถูกลบออกจาก board
    }

    @Test
    void shootNoTarget(){
        Minion atk = spawn(p1,1,1);
        game.shoot(atk,Position.DOWN,5);

        // nothing should change
        assertEquals(1, atk.getPosition().getRow());
    }

    // =================================================
    // FIND OPPONENT
    // =================================================
    @Test
    void findOpponentDistance(){
        Minion m = spawn(p1,2,2);
        spawn(p2,5,2); // ลงตรง

        assertEquals(3, game.findOpponent(m)/10);
    }

    @Test
    void findOpponentNone(){
        Minion m = spawn(p1,1,1);
        assertEquals(0,game.findOpponent(m));
    }

    // =================================================
    // NEARBY
    // =================================================

    @Test
    void nearbyEnemyPositive(){
        Minion m = spawn(p1,7,7);

        Position pos = m.getPosition().move(Position.DOWN);
        spawn(p2,pos.getRow(),pos.getCol());

        long v = game.nearby(m,Position.DOWN);
        assertTrue(v>0);
    }
    @Test
    void nearbyAllyNegative(){
        Minion m = spawn(p1,1,1);

        Position pos = m.getPosition().move(Position.DOWN);
        spawn(p1,pos.getRow(),pos.getCol());

        long v = game.nearby(m,Position.DOWN);

        assertTrue(v < 0);
    }

    @Test
    void nearbyNone(){
        Minion m = spawn(p1,1,1);
        long v = game.nearby(m,Position.DOWN);

        assertEquals(0,v);
    }

    // =================================================
    // BUDGET
    // =================================================
    @Test
    void testBudgetOverflow(){
        p1.setBudget(5000);      // เกิน maxBudget
        game.beginTurn("p1");    // ต้องมี turnCount > 0 ไม่งั้นไม่มี interest
        game.applyTurnBudget("p1");

        assertTrue(p1.getBudget() <= 200);
    }
    @Test
    void shootSameTeam() {
        p1.setBudget(100);

        Minion atk = spawn(p1, 1, 2);
        Position pos = atk.getPosition().move(Position.DOWN);
        p1.addSpawnableHex(pos);
        Minion ally = spawn(p1, pos.getRow(), pos.getCol());

        boolean result = game.shoot(atk, Position.DOWN, 10);

        assertFalse(result);           // ยิงเพื่อนไม่ได้
        assertEquals(10, ally.getHp()); // HP ไม่ลด
    }
    @Test
    void shootWithoutBudget(){
        p1.setBudget(100); // ให้ spawn ผ่านก่อน

        Minion atk = spawn(p1,3,3);
        spawn(p2,4,3);

        p1.setBudget(0); // ค่อยทำให้ยิงไม่ได้

        boolean result = game.shoot(atk, Position.DOWN, 10);

        assertFalse(result);
    }

}