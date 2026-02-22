package strategy.parser;

import strategy.ast.Expr;
import strategy.ast.Stmt;
import strategy.ast.expr.*;
import strategy.ast.stmt.*;
import core.Position;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Strategy → Statement+
    public List<Stmt> parseStrategy() {
        List<Stmt> stmts = new ArrayList<>();

        while (!check(TokenType.EOF)) {

            // กันเคส EOF ซ้อน
            if (peek().type == TokenType.EOF) break;

            stmts.add(parseStatement());
        }

        return stmts;
    }

    // Statement → Command | Block | If | While
    private Stmt parseStatement() {
        if (match(TokenType.IF)) return parseIf();
        if (match(TokenType.WHILE)) return parseWhile();
        if (match(TokenType.LBRACE)) return parseBlock();
        return parseCommand();
    }

    //  helpers ใช้แทน if
    private boolean match(TokenType... t) {
        for (TokenType T : t) {
        if (check(T)) {
            pos++;
            return true;
            }
        }
        return false;
    }

    private boolean check(TokenType t) {
        return peek().type == t;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }

    private Token consume(TokenType t, String msg) {
        if (check(t)) return tokens.get(pos++);
        throw error(msg);
    }

    private RuntimeException error(String msg) {
        return new RuntimeException(msg + " at " + peek());
    }

    //Expression parser
    //Parser ต้องเป็นตัวแปลง TokenType → BinaryExpr.Op
    private BinaryExpr.Op toBinaryOp(TokenType type) {
        return switch (type) {
            case PLUS -> BinaryExpr.Op.PLUS;
            case MINUS -> BinaryExpr.Op.MINUS;
            case STAR -> BinaryExpr.Op.STAR;
            case DIV -> BinaryExpr.Op.DIV;
            case MOD -> BinaryExpr.Op.MOD;
            case CARET -> BinaryExpr.Op.CARET;

            default -> throw error("Invalid binary operator");
        };
    }

    //+-
    private Expr parseExpression() {
        Expr expr = parseTerm();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();
            Expr right = parseTerm();
            expr = new BinaryExpr(expr, toBinaryOp(op.type), right);
        }

        return expr;
    }
    //*/%
    private Expr parseTerm() {
        Expr expr = parseFactor();

        while (match(TokenType.STAR, TokenType.DIV, TokenType.MOD)) {
            Token op = previous();
            Expr right = parseFactor();
            expr = new BinaryExpr(expr,toBinaryOp(op.type), right);
        }

        return expr;
    }

    //^
    private Expr parseFactor() {
        Expr left = parsePower();

        if (match(TokenType.CARET)) {
            Token op = previous();
            Expr right = parseFactor();   // recursive!
            return new BinaryExpr(left, toBinaryOp(op.type), right);
        }

        return left;
    }
    private Expr parsePower() {

        if (match(TokenType.ALLY)) {
            return new AllyExpr();
        }

        if (match(TokenType.OPPONENT)) {
            return new OpponentExpr();
        }

        if (match(TokenType.NEARBY)) {
            Token dir = consumeDirection();
            return new NearbyExpr(tokenToDirection(dir.type));
        }

        if (match(TokenType.NUMBER)) {
            return new NumberExpr(Long.parseLong(previous().lexeme));
        }

        if (match(TokenType.IDENT)) {
            String name = previous().lexeme;

            if (!isAllowedVariable(name)) {
                throw error("Unknown variable: " + name);
            }

            return new VarExpr(name);
        }

        if (match(TokenType.LPAREN)) {
            Expr e = parseExpression();
            consume(TokenType.RPAREN, "Expected ')'");
            return e;
        }

        throw error("Expected expression");
    }

    private boolean isAllowedVariable(String name) {
        // ต้องรองรับตัวแปรตาม Specs หน้า 5
        return switch (name) {
            case "hp", "row", "col", "Budget", "Int", "MaxBudget", "SpawnsLeft", "random" -> true;
            default -> Character.isLowerCase(name.charAt(0)) || Character.isUpperCase(name.charAt(0));
        };
    }

    private Token consumeDirection() {
        Token t = peek();
        if (t.type == TokenType.UP || t.type == TokenType.DOWN ||
                t.type == TokenType.UPLEFT || t.type == TokenType.UPRIGHT ||
                t.type == TokenType.DOWNLEFT || t.type == TokenType.DOWNRIGHT) {
            pos++;
            return t;
        }
        throw error("Expected direction");
    }


    //if ( Expression ) then Statement else Statement
    private Stmt parseIf() {
        consume(TokenType.LPAREN, "Expected '(' after if");
        var cond = parseExpression();   // ยังเป็น stub
        consume(TokenType.RPAREN, "Expected ')'");
        consume(TokenType.THEN, "Expected 'then'");
        Stmt thenStmt = parseStatement();
        consume(TokenType.ELSE, "Expected 'else'");
        Stmt elseStmt = parseStatement();
        return new IfStmt(cond, thenStmt, elseStmt);
    }

    //while ( Expression ) Statement
    private Stmt parseWhile() {
        consume(TokenType.LPAREN, "Expected '(' after while");
        var cond = parseExpression();
        consume(TokenType.RPAREN, "Expected ')'");
        Stmt body = parseStatement();
        return new WhileStmt(cond, body);
    }

    private Stmt parseBlock() {
        List<Stmt> stmts = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)) {
            stmts.add(parseStatement());
        }
        consume(TokenType.RBRACE, "Expected '}'");
        return new BlockStmt(stmts);
    }

    //Command → AssignmentStatement | ActionCommand
    private Stmt parseCommand() {
        if (match(TokenType.DONE)) return new DoneStmt();

        if (match(TokenType.MOVE)) {
            Token dir = consumeDirection();
            return new MoveStmt(tokenToDirection(dir.type));
        }

        if (match(TokenType.SHOOT)) {
            Token dir = consumeDirection();
            var expr = parseExpression();
            return new ShootStmt(tokenToDirection(dir.type), expr);
        }

        // แก้ไข: ถ้าเจอ } หรือ EOF ตรงนี้ แสดงว่าโครงสร้าง Block ผิดพลาด
        if (check(TokenType.RBRACE) || check(TokenType.EOF)) {
            throw error("Unexpected token or missing command");
        }

        // ถ้าไม่ใช่คำสั่ง Action ให้ถือว่าเป็น Assignment
        Token name = consume(TokenType.IDENT, "Expected identifier");
        consume(TokenType.ASSIGN, "Expected '='");
        var expr = parseExpression();
        return new AssignStmt(name.lexeme, expr);
    }

    private int tokenToDirection(TokenType t) {
        return switch (t) {
            case UP -> Position.UP;
            case UPRIGHT -> Position.UPRIGHT;
            case DOWNRIGHT -> Position.DOWNRIGHT;
            case DOWN -> Position.DOWN;
            case DOWNLEFT -> Position.DOWNLEFT;
            case UPLEFT -> Position.UPLEFT;
            default -> throw error("Invalid direction");
        };
    }



}
