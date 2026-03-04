package test;

import controller.GameController;
import core.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import strategy.ast.Stmt;
import strategy.parser.Parser;
import strategy.parser.Tokenizer;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    GameController gc;
    Map<String, Integer> kindDefense;
    Map<String, List<Stmt>> kindAst;

    // helper parse เหมือนใน Rungame
    private List<Stmt> parse(String src) {
        return new Parser(new Tokenizer(src).tokenize()).parseStrategy();
    }

    @BeforeEach
    void setup() throws Exception {
        gc = new GameController();
        kindDefense = new LinkedHashMap<>();
        kindAst     = new LinkedHashMap<>();
    }


    // 1. Parser → AST → Evaluator → GameLogic ครบ pipeline

    @Test
    void pipeline_strategyTextToMinionMove() throws Exception {
        gc.createGame(null, GameState.Mode.DUEL);

        List<Stmt> ast = parse("move down");

        Minion m = gc.createMinion("A", "p1", 1, 1, 0);
        assertTrue(gc.setupSpawn("p1", m, ast));
        gc.startGame();

        Position before = m.getPosition();
        gc.executeTurn("p1");

        // minion ต้องเดินลงมา
        assertNotEquals(before, m.getPosition());
        assertEquals(before.getRow() + 1, m.getPosition().getRow());
    }

    @Test
    void pipeline_ifCondition_executesCorrectBranch() throws Exception {
        gc.createGame(null, GameState.Mode.DUEL);

        // if (1) then move down else move up → ต้องเดินลงเสมอ
        List<Stmt> ast = parse("if (1) then move down else move up");

        Minion m = gc.createMinion("A", "p1", 1, 1, 0);
        gc.setupSpawn("p1", m, ast);
        gc.startGame();

        gc.executeTurn("p1");

        assertEquals(2, m.getPosition().getRow()); // เดินลง
    }


    // 2. End turn triggers strategy execution

    @Test
    void endTurn_triggersAllMinionStrategies() throws Exception {
        gc.createGame(null, GameState.Mode.DUEL);

        List<Stmt> ast = parse("move down");

        // spawn 2 minions p1
        Minion m1 = gc.createMinion("A", "p1", 1, 1, 0);
        Minion m2 = gc.createMinion("A", "p1", 1, 2, 0);
        gc.setupSpawn("p1", m1, ast);
        gc.setupSpawn("p1", m2, ast);
        gc.startGame();

        Position b1 = m1.getPosition();
        Position b2 = m2.getPosition();

        GameController.TurnResult result = gc.executeTurn("p1");

        // ทั้งสอง minion ต้องเดิน
        assertNotEquals(b1, m1.getPosition());
        assertNotEquals(b2, m2.getPosition());

        // log ต้องมี 2 entries ทั้งคู่ success
        assertEquals(2, result.log.size());
        assertTrue(result.log.stream().allMatch(l -> l.success));
    }


    // 3. Multiple minions — เรียงตาม spawnTurn (เก่าสุดก่อน)

    @Test
    void multipleMinions_executeInSpawnOrder() throws Exception {
        gc.createGame(null, GameState.Mode.DUEL);

        // m1 spawn ก่อน — เดิน DOWN
        // m2 spawn หลัง — เดิน UP
        Minion m1 = gc.createMinion("A", "p1", 1, 1, 0);
        Minion m2 = gc.createMinion("A", "p1", 1, 2, 0);

        gc.setupSpawn("p1", m1, parse("move down"));
        gc.setupSpawn("p1", m2, parse("move up"));
        gc.startGame();

        gc.executeTurn("p1");

        // m1 เดินลง → row เพิ่ม
        assertTrue(m1.getPosition().getRow() > 1);
        // m2 เดิน up ไม่ได้ (row=1 อยู่ขอบบน) → ไม่เปลี่ยน
        assertEquals(1, m2.getPosition().getRow());
    }


    // 4. Endgame detection

    @Test
    void endgame_p2HasNoMinions_p1Wins() throws Exception {
        gc.createGame(null, GameState.Mode.DUEL);

        Minion m1 = gc.createMinion("A", "p1", 1, 1, 0);
        gc.setupSpawn("p1", m1, parse("done"));
        // p2 ไม่ spawn เลย
        gc.startGame();

        GameController.TurnResult result = gc.executeTurn("p1");

        assertTrue(result.isOver);
        assertEquals("p1", result.winner);
    }

    @Test
    void endgame_allMinionsDead_triggersGameOver() throws Exception {
        gc.createGame(null, GameState.Mode.DUEL);

        // p1 ยิง DOWN ด้วย expenditure สูง
        Minion attacker = gc.createMinion("A", "p1", 1, 1, 0);
        gc.setupSpawn("p1", attacker, parse("shoot down 999"));

        // p2 spawn ที่ (2,1) ติดกับ p1 ทิศ DOWN — HP น้อยมาก
        Minion target = Minion.create(
                "A",
                "weakTarget",
                gc.getGameState().p2,
                new Position(2, 1),
                1,   // HP = 1 → ยิงครั้งเดียวตาย
                0
        );
        target.setStrategyAST(parse("done"));

        // เพิ่ม spawnable hex ให้ p2 แล้ว spawn ปกติ
        gc.getGameState().p2.addSpawnableHex(new Position(2, 1));
        assertTrue(gc.setupSpawn("p2", target, parse("done")));

        gc.startGame();

        GameController.TurnResult result = gc.executeTurn("p1");

        assertTrue(result.isOver);
        assertEquals("p1", result.winner);
    }


    // 5. จำลอง Rungame AUTO flow ครบ loop

    @Test
    void autoMode_gameRunsToCompletion() throws Exception {
        gc.createGame(null, GameState.Mode.AUTO);

        kindDefense.put("A", 2);
        kindAst.put("A", parse("move down"));
        gc.setKinds(kindDefense, kindAst);

        // setup spawn เหมือน Rungame.autoSpawnSetup
        Minion m1 = gc.createMinion("A", "p1", 1, 1, 2);
        gc.setupSpawn("p1", m1, kindAst.get("A"));

        Minion m2 = gc.createMinion("A", "p2", 8, 8, 2);
        gc.getGameState().p2.addSpawnableHex(new Position(8, 8));
        gc.setupSpawn("p2", m2, kindAst.get("A"));

        gc.startGame();

        // วน loop เหมือน Rungame.gameLoop จนจบเกม
        int maxIter = 200; // กันวนไม่สิ้นสุด
        while (!gc.isGameOver() && maxIter-- > 0) {
            String current = gc.getGameState().current;
            gc.executeTurn(current);
        }

        // ต้องจบเกมภายใน maxTurns
        assertTrue(gc.isGameOver());
        assertNotNull(gc.getGameState().winner);
    }


    // 6. Budget flow ครบ turn

    @Test
    void turnBudget_appliedBeforeStrategyExecutes() throws Exception {
        gc.createGame(null, GameState.Mode.DUEL);

        // ใช้ strategy ที่ assign Budget ลงตัวแปร แล้วดูว่าได้ค่าหลัง applyBudget
        List<Stmt> ast = parse("x = Budget");

        Minion m = gc.createMinion("A", "p1", 1, 1, 0);
        gc.setupSpawn("p1", m, ast);
        gc.startGame();

        long budgetBefore = gc.getGameState().p1.getBudgetFloor();
        gc.executeTurn("p1");
        long budgetAfter = gc.getGameState().p1.getBudgetFloor();

        // หลัง executeTurn budget ต้องเพิ่มขึ้น (turnBudget + interest)
        // และหักค่า move(1) ไปด้วย — แต่ยังต้องมากกว่า budgetBefore
        // (initBudget=10000, turnBudget=90 → ต้องเพิ่ม)
        assertTrue(budgetAfter >= budgetBefore);
    }
}