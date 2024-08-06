import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MathEvaluator {
    private List<Token> tokens;
    private int pos;
    private Token currentToken;
    private Map<String, Object> variables;
    private Map<String, List<Integer>> tt;

    public MathEvaluator(List<Token> tokens, Map<String, Object> variables, Map<String, List<Integer>> tt) {
        this.tokens = tokens;
        this.pos = 0;
        if (!tokens.isEmpty()) {
            this.currentToken = tokens.get(pos);
        }
        this.variables = variables;
        this.tt = tt;
    }

    private void advance() {
        pos++;
        if (pos < tokens.size()) {
            currentToken = tokens.get(pos);
        } else {
            currentToken = null; // EOF
        }
    }

    private void consumeToken(TokenType tokenType) {
        if (currentToken != null && currentToken.getType() == tokenType) {
            advance();
        } else {
            throw new RuntimeException("Unexpected token: " + currentToken + ", expected: " + tokenType);
        }
    }

    private int factor() {
        int value;
        if (currentToken.getType() == TokenType.INTEGER) {
            value = (int) currentToken.getValue();
            consumeToken(TokenType.INTEGER);
        } else if (currentToken.getType() == TokenType.IDENTIFIER) {
            String var = (String) currentToken.getValue();
            if (variables.containsKey(var)) {
                Object varValue = variables.get(var);
                if (varValue instanceof Integer) {
                    value = (Integer) varValue;
                } else if (varValue instanceof List) {
                    value = sumList((List<Integer>) varValue);
                } else {
                    throw new RuntimeException("Variable " + var + " is not an integer or a list");
                }
            } else {
                throw new RuntimeException("Unexpected variable: " + var);
            }
            consumeToken(TokenType.IDENTIFIER);
        } else if (currentToken.getType() == TokenType.SUM) {
            value = sumExpression();
        } else {
            throw new RuntimeException("Unexpected token: " + currentToken);
        }
        return value;
    }

    private int term() {
        int result = factor();

        while (currentToken != null && (currentToken.getType() == TokenType.PLUS || currentToken.getType() == TokenType.MINUS)) {
            if (currentToken.getType() == TokenType.PLUS) {
                consumeToken(TokenType.PLUS);
                result += factor();
            } else if (currentToken.getType() == TokenType.MINUS) {
                consumeToken(TokenType.MINUS);
                result -= factor();
            }
        }

        return result;
    }

    public int expr() {
        int result = term();

        while (currentToken != null) {
            if (currentToken.getType() == TokenType.PLUS) {
                consumeToken(TokenType.PLUS);
                result += term();
            } else if (currentToken.getType() == TokenType.MINUS) {
                consumeToken(TokenType.MINUS);
                result -= term();
            }
        }

        return result;
    }

    private int sumExpression() {
        consumeToken(TokenType.SUM);
        consumeToken(TokenType.LPAREN);

        int totalSum = 0;

        if (currentToken.getType() == TokenType.IDENTIFIER) {
            String var = (String) currentToken.getValue();
            consumeToken(TokenType.IDENTIFIER);

            if (var.equals("tt")) {
                consumeToken(TokenType.LBRACKET); // consume '['
                String key = (String) currentToken.getValue();
                consumeToken(TokenType.STRING);
                consumeToken(TokenType.RBRACKET); // consume ']'

                if (tt.containsKey(key)) {
                    List<Integer> values = tt.get(key);
                    totalSum = processListSum(values);
                } else {
                    throw new RuntimeException("Key " + key + " not found in map tt");
                }
            } else {
                if (variables.containsKey(var) && variables.get(var) instanceof List) {
                    List<Integer> values = (List<Integer>) variables.get(var);
                    totalSum = processListSum(values);
                } else {
                    throw new RuntimeException("sum function expects a map or list reference");
                }
            }
        } else {
            throw new RuntimeException("sum function expects an identifier");
        }

        consumeToken(TokenType.RPAREN);
        return totalSum;
    }

    private int processListSum(List<Integer> values) {
        int totalSum = 0;
        if (currentToken != null && (currentToken.getType() == TokenType.PLUS || currentToken.getType() == TokenType.MINUS)) {
            char operator = (char) currentToken.getValue();
            consumeToken(currentToken.getType());

            int addValue = factor(); // The value to add to each element in the list

            for (int value : values) {
                if (operator == '+') {
                    totalSum += value + addValue;
                } else if (operator == '-') {
                    totalSum += value - addValue;
                }
            }
        } else {
            for (int value : values) {
                totalSum += value;
            }
        }
        return totalSum;
    }

    private int sumList(List<Integer> list) {
        int sum = 0;
        for (int num : list) {
            sum += num;
        }
        return sum;
    }

    public List<String> getVariables() {
        List<String> variablesList = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.getType() == TokenType.IDENTIFIER) {
                String var = (String) token.getValue();
                if (var.equals("tt")) {
                    i++; // advance to LBRACKET
                    if (i < tokens.size() && tokens.get(i).getType() == TokenType.LBRACKET) {
                        i++; // advance to STRING
                        if (i < tokens.size() && tokens.get(i).getType() == TokenType.STRING) {
                            String key = (String) tokens.get(i).getValue();
                            i++; // advance to RBRACKET
                            if (i < tokens.size() && tokens.get(i).getType() == TokenType.RBRACKET) {
                                variablesList.add("tt['" + key + "']");
                            }
                        }
                    }
                } else {
                    variablesList.add(var);
                }
            }
        }
        return variablesList;
    }

    public static void main(String[] args) {
        MathLexer lexer = new MathLexer("sum(tt['0101']+1) - y + sum(z-2)");
        List<Token> tokens = lexer.tokenize();

        Map<String, Object> variables = Map.of(
            "y", 10, // Example integer variable
            "z", List.of(4, 5, 6) // Example list variable
        );

        Map<String, List<Integer>> tt = Map.of(
            "0101", List.of(1, 2, 3) // Example map variable
        );

        MathEvaluator evaluator = new MathEvaluator(tokens, variables, tt);
        int result = evaluator.expr();
        System.out.println("Result: " + result); // Expected output: 8

        List<String> vars = evaluator.getVariables();
        System.out.println("Variables: " + vars); // Expected output: [tt['0101'], y, z]
    }
}
