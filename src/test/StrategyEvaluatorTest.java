package test;

import core.*;
import org.junit.jupiter.api.Test;
import strategy.ast.Stmt;
import strategy.ast.expr.BinaryExpr;
import strategy.ast.expr.CompareExpr;
import strategy.ast.expr.NumberExpr;
import strategy.ast.expr.VarExpr;
import strategy.ast.stmt.*;
import strategy.evaluator.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StrategyEvaluatorTest {
    @Test
    void eval_assign_shouldStoreVariable() {

        Config config = new Config();
        GameLogic game = new GameLogic(config, GameState.Mode.AUTO);
        game.initBudgets();

        Minion m = Minion.create("A", "m1", game.getP1(), new Position(4,4), 100);
        game.spawnMinion("p1", m);

        EvalContext ctx = new EvalContextImpl(game, m);

        List<Stmt> strategy = List.of(
                new AssignStmt("x", new NumberExpr(10))
        );

        new StrategyEvaluatorImpl().evaluate(strategy, ctx);

        assertEquals(10, ctx.getVar("x"));
    }

    @Test
    void eval_move_shouldMoveMinion() {

        Config config = new Config();
        GameLogic game = new GameLogic(config, GameState.Mode.AUTO);
        game.initBudgets();

        Minion m = Minion.create("A", "m1", game.getP1(), new Position(4,4), 100);
        game.spawnMinion("p1", m);

        EvalContext ctx = new EvalContextImpl(game, m);

        List<Stmt> strategy = List.of(
                new MoveStmt(Position.UP)
        );

        new StrategyEvaluatorImpl().evaluate(strategy, ctx);

        assertEquals(new Position(3,4), m.getPosition());
    }


    @Test
    void eval_shoot_shouldDamageEnemy() {

        Config config = new Config();
        GameLogic game = new GameLogic(config, GameState.Mode.AUTO);
        game.initBudgets();

        Position attackerPos = new Position(4,4);
        Position targetPos   = attackerPos.move(Position.DOWN);

        Minion attacker = Minion.create("A", "m1", game.getP1(), attackerPos, 100);
        Minion target   = Minion.create("A", "m2", game.getP2(), targetPos, 100);

        game.spawnMinion("p1", attacker);
        game.getP2().addSpawnableHex(targetPos);
        game.spawnMinion("p2", target);

        EvalContext ctx = new EvalContextImpl(game, attacker);

        List<Stmt> strategy = List.of(
                new ShootStmt(Position.DOWN, new NumberExpr(20))
        );

        new StrategyEvaluatorImpl().evaluate(strategy, ctx);

        assertTrue(target.getHp() < 100);
    }

    @Test
    void eval_if_shouldExecuteThenBranch() {

        Config config = new Config();
        GameLogic game = new GameLogic(config, GameState.Mode.AUTO);
        game.initBudgets();

        Minion m = Minion.create("A", "m1", game.getP1(), new Position(4,4), 100);
        game.spawnMinion("p1", m);

        EvalContext ctx = new EvalContextImpl(game, m);

        Stmt stmt = new IfStmt(
                new NumberExpr(1),
                new AssignStmt("x", new NumberExpr(5)),
                new AssignStmt("x", new NumberExpr(9))
        );

        new StrategyEvaluatorImpl().evaluate(List.of(stmt), ctx);

        assertEquals(5, ctx.getVar("x"));
    }

    @Test
    void eval_move_shouldChangePosition() {

        Config config = new Config();
        GameLogic game = new GameLogic(config, GameState.Mode.AUTO);
        game.initBudgets();

        Minion m = Minion.create("A", "m1", game.getP1(), new Position(4,4), 100);
        game.spawnMinion("p1", m);

        EvalContext ctx = new EvalContextImpl(game, m);

        List<Stmt> strategy = List.of(
                new MoveStmt(Position.DOWN)
        );

        new StrategyEvaluatorImpl().evaluate(strategy, ctx);

        assertEquals(new Position(5,4), m.getPosition());
    }

    // done หยุด strategy ทันที — statement หลัง done ต้องไม่ execute
    @Test
    void eval_done_shouldStopExecution() {
        GameLogic game = new GameLogic(new Config(), GameState.Mode.AUTO);
        game.initBudgets();
        Minion m = Minion.create("A","m1",game.getP1(),new Position(4,4),100);
        game.spawnMinion("p1", m);

        EvalContext ctx = new EvalContextImpl(game, m);
        new StrategyEvaluatorImpl().evaluate(List.of(
                new DoneStmt(),
                new AssignStmt("x", new NumberExpr(42)) // ไม่ควร execute
        ), ctx);

        assertEquals(0L, ctx.getVar("x")); // ยังเป็น 0
        assertTrue(ctx.isDone());
    }

    // while loop หยุดเมื่อ condition เป็น false
    @Test
    void eval_while_shouldLoopUntilFalse() {
        GameLogic game = new GameLogic(new Config(), GameState.Mode.AUTO);
        game.initBudgets();
        Minion m = Minion.create("A","m1",game.getP1(),new Position(4,4),100);
        game.spawnMinion("p1", m);

        EvalContext ctx = new EvalContextImpl(game, m);
        // x = 0; while (x < 3) x = x + 1
        new StrategyEvaluatorImpl().evaluate(List.of(
                new AssignStmt("x", new NumberExpr(0)),
                new WhileStmt(
                        new CompareExpr(new VarExpr("x"), CompareExpr.Op.LT, new NumberExpr(3)),
                        new AssignStmt("x", new BinaryExpr(
                                new VarExpr("x"), BinaryExpr.Op.PLUS, new NumberExpr(1)))
                )
        ), ctx);

        assertEquals(3L, ctx.getVar("x"));

    }

    // division by zero → strategy terminate (ไม่ crash)
    @Test
    void eval_divisionByZero_shouldTerminate() {
        GameLogic game = new GameLogic(new Config(), GameState.Mode.AUTO);
        game.initBudgets();
        Minion m = Minion.create("A","m1",game.getP1(),new Position(4,4),100);
        game.spawnMinion("p1", m);

        EvalContext ctx = new EvalContextImpl(game, m);
        assertDoesNotThrow(() ->
                new StrategyEvaluatorImpl().evaluate(List.of(
                        new AssignStmt("x", new BinaryExpr(
                                new NumberExpr(10), BinaryExpr.Op.DIV, new NumberExpr(0)))
                ), ctx)
        );
    }




}
