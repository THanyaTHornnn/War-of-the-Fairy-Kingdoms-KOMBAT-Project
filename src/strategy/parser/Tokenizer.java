package strategy.parser;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private final String src;
    private int pos = 0;

    public Tokenizer(String src) {
        this.src = src;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd()) break;

            char c = peek();

            if (Character.isDigit(c)) {
                tokens.add(number());
            } else if (Character.isLetter(c)) {
                tokens.add(identifier());
            } else {
                tokens.add(symbol());
            }
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private Token number() {
        int start = pos;
        while (!isAtEnd() && Character.isDigit(peek())) advance();
        return new Token(TokenType.NUMBER, src.substring(start, pos));
    }

    private Token identifier() {
        int start = pos;
        while (!isAtEnd() && Character.isLetterOrDigit(peek())) advance();
        String text = src.substring(start, pos);
        return new Token(keywordType(text), text);
    }

    private Token symbol() {
        char c = advance();
        return switch (c) {
            case '+' -> new Token(TokenType.PLUS, "+");
            case '-' -> new Token(TokenType.MINUS, "-");
            case '*' -> new Token(TokenType.STAR, "*");
            case '/' -> new Token(TokenType.DIV, "/");
            case '%' -> new Token(TokenType.MOD, "%");
            case '^' -> new Token(TokenType.CARET, "^");

            case '(' -> new Token(TokenType.LPAREN, "(");
            case ')' -> new Token(TokenType.RPAREN, ")");
            case '{' -> new Token(TokenType.LBRACE, "{");
            case '}' -> new Token(TokenType.RBRACE, "}");

            case '=' -> match('=') ?
                    new Token(TokenType.EQ, "==") :
                    new Token(TokenType.ASSIGN, "=");

            case '>' -> match('=') ?
                    new Token(TokenType.GE, ">=") :
                    new Token(TokenType.GT, ">");

            case '<' -> match('=') ?
                    new Token(TokenType.LE, "<=") :
                    new Token(TokenType.LT, "<");

            case '!' -> {
                if (match('=')) {
                    yield new Token(TokenType.NE, "!=");
                }
                throw error("Unexpected '!'");
            }

            default -> throw error("Unexpected character: " + c);
        };
    }


    private TokenType keywordType(String s) {
        return switch (s) {
            case "if" -> TokenType.IF;
            case "then" -> TokenType.THEN;
            case "else" -> TokenType.ELSE;
            case "while" -> TokenType.WHILE;

            case "move" -> TokenType.MOVE;
            case "shoot" -> TokenType.SHOOT;
            case "done" -> TokenType.DONE;

            case "ally" -> TokenType.ALLY;
            case "opponent" -> TokenType.OPPONENT;
            case "nearby" -> TokenType.NEARBY;

            case "up" -> TokenType.UP;
            case "down" -> TokenType.DOWN;
            case "upleft" -> TokenType.UPLEFT;
            case "upright" -> TokenType.UPRIGHT;
            case "downleft" -> TokenType.DOWNLEFT;
            case "downright" -> TokenType.DOWNRIGHT;

            default -> TokenType.IDENT;
        };
    }

    private boolean match(char expected) {
        if (isAtEnd() || src.charAt(pos) != expected) return false;
        pos++;
        return true;
    }

    private char peek() {
        return src.charAt(pos);
    }

    private char advance() {
        return src.charAt(pos++);
    }

    private boolean isAtEnd() {
        return pos >= src.length();
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) pos++;
    }

    private RuntimeException error(String msg) {
        return new RuntimeException(msg);
    }
}
