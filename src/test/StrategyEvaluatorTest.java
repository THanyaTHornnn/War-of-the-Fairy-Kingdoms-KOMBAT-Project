package test;

import org.junit.jupiter.api.Test;
import strategy.ast.Stmt;
import strategy.ast.expr.NumberExpr;
import strategy.ast.stmt.*;
import strategy.evaluator.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StrategyEvaluatorTest {

//    @Test
//    void testEvaluatorStopsOnDone() {
//        FakeEvalContext ctx = new FakeEvalContext();
//
//        List<Stmt> program = List.of(
//                new AssignStmt("x", new NumberExpr(1)),
//                new DoneStmt(),
//                new AssignStmt("x", new NumberExpr(999)) // ต้องไม่โดน
//        );
//
//        StrategyEvaluator eval = new StrategyEvaluatorImpl();
//        eval.evaluate(program, ctx);
//
//        assertEquals(1, ctx.getVar("x"));
//    }
}
