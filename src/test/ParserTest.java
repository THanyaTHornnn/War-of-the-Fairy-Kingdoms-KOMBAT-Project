package test;

import org.junit.jupiter.api.Test;

import strategy.parser.Parser;
import strategy.parser.Tokenizer;
import strategy.ast.Stmt;
import strategy.ast.stmt.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    // helper: parse source → AST
    private List<Stmt> parse(String src) {
        Tokenizer tokenizer = new Tokenizer(src);
        Parser parser = new Parser(tokenizer.tokenize());
        return parser.parseStrategy();
    }

    @Test
    void testDoneStatement() {
        List<Stmt> stmts = parse("done");

        assertEquals(1, stmts.size());
        assertTrue(stmts.get(0) instanceof DoneStmt);
    }

    @Test
    void testMoveStatement() {
        List<Stmt> stmts = parse("move up");

        assertEquals(1, stmts.size());
        assertTrue(stmts.get(0) instanceof MoveStmt);
    }

    @Test
    void testAssignmentStatement() {
        List<Stmt> stmts = parse("x = 5");

        assertEquals(1, stmts.size());
        assertTrue(stmts.get(0) instanceof AssignStmt);

        AssignStmt a = (AssignStmt) stmts.get(0);
        assertEquals("x", a.getName()); // ต้องมี getter
    }

    @Test
    void testIfStatement() {
        List<Stmt> stmts = parse(
                "if (1) then done else move up"
        );

        assertEquals(1, stmts.size());
        assertTrue(stmts.get(0) instanceof IfStmt);
    }

    @Test
    void testWhileStatement() {
        List<Stmt> stmts = parse(
                "while (1) move up"
        );

        assertEquals(1, stmts.size());
        assertTrue(stmts.get(0) instanceof WhileStmt);
    }

    @Test
    void testBlockStatement() {
        List<Stmt> stmts = parse(
                "{ move up done }"
        );

        assertEquals(1, stmts.size());
        assertTrue(stmts.get(0) instanceof BlockStmt);

        BlockStmt block = (BlockStmt) stmts.get(0);
        assertEquals(2, block.getStatements().size()); // ต้องมี getter
    }

    @Test
    void testMultipleStatements() {
        List<Stmt> stmts = parse(
                "move up done"
        );

        assertEquals(2, stmts.size());
        assertTrue(stmts.get(0) instanceof MoveStmt);
        assertTrue(stmts.get(1) instanceof DoneStmt);
    }

    @Test
    void testSyntaxError() {
        assertThrows(RuntimeException.class, () -> {
            parse("if (1) done"); // missing then / else
        });
    }


}
