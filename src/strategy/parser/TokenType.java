package strategy.parser;

public enum TokenType {
    // keywords
    IF, THEN, ELSE, WHILE,
    MOVE, SHOOT, DONE,
    ALLY, OPPONENT, NEARBY,

    // directions
    UP, DOWN, UPLEFT, UPRIGHT, DOWNLEFT, DOWNRIGHT,

    // identifiers & literals
    IDENT, NUMBER,

    // operators
    PLUS, MINUS, STAR, DIV, MOD,
    LT, LE, GT, GE, EQ, NE,
    ASSIGN,        // =
    CARET,         // ^

    // punctuation
    LPAREN, RPAREN,
    LBRACE, RBRACE,

    EOF //End Of File
}
