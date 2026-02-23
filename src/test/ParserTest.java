package test;

import org.junit.jupiter.api.Test;
import strategy.ast.Stmt;
import strategy.ast.expr.*;
import strategy.ast.stmt.*;
import strategy.parser.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    private Token t(TokenType type, String lexeme) {
        return new Token(type, lexeme);
    }

    // =========================
    // EXPRESSION TEST
    // =========================

    @Test
    void testParseBinaryExpression() {
        List<Token> tokens = List.of(
                new Token(TokenType.IDENT, "x"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.NUMBER, "5"),
                new Token(TokenType.PLUS, "+"),
                new Token(TokenType.NUMBER, "3"),
                new Token(TokenType.EOF, "")
        );

        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parseStrategy();

        assertEquals(1, stmts.size());
        assertTrue(stmts.get(0) instanceof AssignStmt);
    }


    // =========================
    // MOVE
    // =========================

    @Test
    void parse_moveCommand() {

        Parser p = new Parser(List.of(
                t(TokenType.MOVE,"move"),
                t(TokenType.UP,"up"),
                t(TokenType.EOF,"")
        ));

        Stmt stmt = p.parseStrategy().get(0);

        assertTrue(stmt instanceof MoveStmt);
    }

    // =========================
    // SHOOT
    // =========================

    @Test
    void parse_shootCommand() {

        Parser p = new Parser(List.of(
                t(TokenType.SHOOT,"shoot"),
                t(TokenType.DOWN,"down"),
                t(TokenType.NUMBER,"10"),
                t(TokenType.EOF,"")
        ));

        Stmt stmt = p.parseStrategy().get(0);

        assertTrue(stmt instanceof ShootStmt);
    }

    // =========================
    // ASSIGN
    // =========================

    @Test
    void parse_assignCommand() {

        Parser p = new Parser(List.of(
                t(TokenType.IDENT,"x"),
                t(TokenType.ASSIGN,"="),
                t(TokenType.NUMBER,"9"),
                t(TokenType.EOF,"")
        ));

        Stmt stmt = p.parseStrategy().get(0);

        assertTrue(stmt instanceof AssignStmt);
    }

    // =========================
    // IF
    // =========================

    @Test
    void parse_ifStatement() {

        Parser p = new Parser(List.of(
                t(TokenType.IF,"if"),
                t(TokenType.LPAREN,"("),
                t(TokenType.NUMBER,"1"),
                t(TokenType.RPAREN,")"),
                t(TokenType.THEN,"then"),
                t(TokenType.DONE,"done"),
                t(TokenType.ELSE,"else"),
                t(TokenType.DONE,"done"),
                t(TokenType.EOF,"")
        ));

        Stmt stmt = p.parseStrategy().get(0);

        assertTrue(stmt instanceof IfStmt);
    }

    // =========================
    // WHILE
    // =========================

    @Test
    void parse_whileStatement() {

        Parser p = new Parser(List.of(
                t(TokenType.WHILE,"while"),
                t(TokenType.LPAREN,"("),
                t(TokenType.NUMBER,"1"),
                t(TokenType.RPAREN,")"),
                t(TokenType.DONE,"done"),
                t(TokenType.EOF,"")
        ));

        Stmt stmt = p.parseStrategy().get(0);

        assertTrue(stmt instanceof WhileStmt);
    }

    // =========================
    // BLOCK
    // =========================

    @Test
    void parse_blockStatement() {

        Parser p = new Parser(List.of(
                t(TokenType.LBRACE,"{"),
                t(TokenType.DONE,"done"),
                t(TokenType.DONE,"done"),
                t(TokenType.RBRACE,"}"),
                t(TokenType.EOF,"")
        ));

        Stmt stmt = p.parseStrategy().get(0);

        assertTrue(stmt instanceof BlockStmt);
    }

    // =========================
    // ERROR CASES
    // =========================

    @Test
    void error_missingDirection() {

        Parser p = new Parser(List.of(
                t(TokenType.MOVE,"move"),
                t(TokenType.EOF,"")
        ));

        assertThrows(RuntimeException.class, p::parseStrategy);
    }

    @Test
    void error_missingParen() {

        Parser p = new Parser(List.of(
                t(TokenType.IF,"if"),
                t(TokenType.LPAREN,"("),
                t(TokenType.NUMBER,"1"),
                t(TokenType.THEN,"then"),
                t(TokenType.DONE,"done"),
                t(TokenType.ELSE,"else"),
                t(TokenType.DONE,"done"),
                t(TokenType.EOF,"")
        ));

        assertThrows(RuntimeException.class, p::parseStrategy);
    }

    @Test
    void error_unknownVariable() {

        Parser p = new Parser(List.of(
                t(TokenType.IDENT,"x"),
                t(TokenType.ASSIGN,"="),
                t(TokenType.IDENT,"???"),
                t(TokenType.EOF,"")
        ));

        assertThrows(RuntimeException.class, p::parseStrategy);
    }

    @Test
    void parse_precedence() {
        Parser p = new Parser(List.of(
                t(TokenType.IDENT,"x"),
                t(TokenType.ASSIGN,"="),
                t(TokenType.NUMBER,"5"),
                t(TokenType.PLUS,"+"),
                t(TokenType.NUMBER,"3"),
                t(TokenType.STAR,"*"),
                t(TokenType.NUMBER,"2"),
                t(TokenType.EOF,"")
        ));

        assertDoesNotThrow(p::parseStrategy);
    }

    @Test
    void parse_nearby() {
        Parser p = new Parser(List.of(
                t(TokenType.IDENT,"x"),
                t(TokenType.ASSIGN,"="),
                t(TokenType.NEARBY,"nearby"),
                t(TokenType.UP,"up"),
                t(TokenType.EOF,"")
        ));

        assertDoesNotThrow(p::parseStrategy);
    }
}