//package test;
//
//
//import org.junit.jupiter.api.Test;
//import strategy.evaluator.EvalContext;
//
//import strategy.evaluator.VariableContext;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class VariableContextTest {
//
//    @Test
//    void testLocalVariableSetGet() {
//        VariableContext vc = new VariableContext(null, null);
//
//        vc.setVar("x", 42);
//        assertEquals(42, vc.get("x"));
//    }
//
//    @Test
//    void testResolveLocalFirst() {
//        VariableContext vc = new VariableContext(null, null);
//
//        vc.setVar("ally", 99);
//
//        EvalContext fake = new FakeEvalContext();
//        assertEquals(99, vc.resolve("ally", fake));
//    }
//
//    @Test
//    void testResolveDynamic() {
//        VariableContext vc = new VariableContext(null, null);
//
//        EvalContext fake = new FakeEvalContext();
//        assertEquals(5, vc.resolve("ally", fake));
//        assertEquals(3, vc.resolve("opponent", fake));
//        assertEquals(1, vc.resolve("nearby", fake));
//    }
//}
