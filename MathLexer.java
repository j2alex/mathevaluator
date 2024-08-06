import java.util.ArrayList;
import java.util.List;

public class MathLexer {
    private String input;
    private int pos;
    private char currentChar;

    public MathLexer(String input) {
        this.input = input;
        this.pos = 0;
        this.currentChar = input.charAt(pos);
    }

    private void advance() {
        pos++;
        if (pos >= input.length()) {
            currentChar = '\0'; // EOF
        } else {
            currentChar = input.charAt(pos);
        }
    }

    private void skipWhitespace() {
        while (currentChar != '\0' && Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    private int integer() {
        StringBuilder result = new StringBuilder();
        while (currentChar != '\0' && Character.isDigit(currentChar)) {
            result.append(currentChar);
            advance();
        }
        return Integer.parseInt(result.toString());
    }

    private String identifier() {
        StringBuilder result = new StringBuilder();
        while (currentChar != '\0' && (Character.isLetter(currentChar) || currentChar == '_')) {
            result.append(currentChar);
            advance();
        }
        return result.toString();
    }

    private String string() {
        StringBuilder result = new StringBuilder();
        advance(); // skip the opening quote
        while (currentChar != '\0' && currentChar != '\'') {
            result.append(currentChar);
            advance();
        }
        advance(); // skip the closing quote
        return result.toString();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (currentChar != '\0') {
            if (Character.isWhitespace(currentChar)) {
                skipWhitespace();
                continue;
            }

            if (Character.isDigit(currentChar)) {
                tokens.add(new Token(TokenType.INTEGER, integer()));
                continue;
            }

            if (Character.isLetter(currentChar)) {
                String id = identifier();
                if (id.equals("sum")) {
                    tokens.add(new Token(TokenType.SUM, id));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, id));
                }
                continue;
            }

            if (currentChar == '+') {
                tokens.add(new Token(TokenType.PLUS, '+'));
                advance();
                continue;
            }

            if (currentChar == '-') {
                tokens.add(new Token(TokenType.MINUS, '-'));
                advance();
                continue;
            }

            if (currentChar == '(') {
                tokens.add(new Token(TokenType.LPAREN, '('));
                advance();
                continue;
            }

            if (currentChar == ')') {
                tokens.add(new Token(TokenType.RPAREN, ')'));
                advance();
                continue;
            }

            if (currentChar == '[') {
                tokens.add(new Token(TokenType.LBRACKET, '['));
                advance();
                continue;
            }

            if (currentChar == ']') {
                tokens.add(new Token(TokenType.RBRACKET, ']'));
                advance();
                continue;
            }

            if (currentChar == '\'') {
                tokens.add(new Token(TokenType.STRING, string()));
                continue;
            }

            throw new RuntimeException("Unexpected character: " + currentChar);
        }

        return tokens;
    }

    public static void main(String[] args) {
        MathLexer lexer = new MathLexer("sum(tt['0101']+1) - y + sum(z-2)");
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}

enum TokenType {
    INTEGER,
    PLUS,
    MINUS,
    IDENTIFIER,
    STRING,
    LPAREN,
    RPAREN,
    LBRACKET,
    RBRACKET,
    SUM
}

class Token {
    private TokenType type;
    private Object value;

    public Token(TokenType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }
}
