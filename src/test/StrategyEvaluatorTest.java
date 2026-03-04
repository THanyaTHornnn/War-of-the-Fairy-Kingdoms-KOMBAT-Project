package test;

import core.*;
import org.junit.jupiter.api.Test;
import strategy.ast.Stmt;
import strategy.ast.expr.BinaryExpr;
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


}
