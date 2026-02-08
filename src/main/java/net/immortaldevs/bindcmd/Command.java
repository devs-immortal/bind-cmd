package net.immortaldevs.bindcmd;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.ArrayListDeque;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.*;

public final class Command {
    private final CmdType type;
    private final String command;

    private static final List<String> FUNCTIONS;

    static {
        List<String> functions = new ArrayList<>(List.of(
                "sqrt", "cbrt", "min", "max", "floor", "ceil", "round", "abs", "sin", "cos", "tan", "asin",
                "acos", "atan", "atan2", "sinh", "cosh", "tanh", "exp", "ln", "log", "log2", "log10"
        ));
        functions.sort((a, b) -> Integer.compare(b.length(), a.length()));
        FUNCTIONS = Collections.unmodifiableList(functions);
    }

    public Command(String command) {
        if (command.startsWith("@")) {
            var message = getLastMessage(command);
            this.type = getType(message);
            this.command = processMessage(message);
        } else {
            this.type = getType(command);
            var message = this.type == CmdType.MESSAGE ? command : command.substring(1);
            this.command = processMessage(message);
        }
    }

    public CmdType getType() {
        return type;
    }

    public String getCommand() {
        return command;
    }

    private CmdType getType(String command) {
        if (command.isBlank()) {
            return CmdType.NONE;
        } else if (command.startsWith("/")) {
            return CmdType.COMMAND;
        }
        return CmdType.MESSAGE;
    }

    private String getLastMessage(String command) {
        try {
            int offset = Integer.parseInt(command.substring(1));
            var history = getChatHistory();
            if (history.isEmpty() || offset < 0 || offset >= history.size()) {
                return "";
            }
            return history.get(history.size() - 1 - offset);
        } catch (NumberFormatException e) {
            return "";
        }
    }

    private ArrayListDeque<String> getChatHistory() {
        var inGameHud = Minecraft.getInstance().gui;
        var chatHud = inGameHud.getChat();
        return chatHud.getRecentChat();
    }

    private String processMessage(String message) {
        if (!message.contains("$")) return message;

        Pattern pattern = Pattern.compile("\\$\\{(.+?)}|\\$(\\w+)");
        Matcher matcher = pattern.matcher(message);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String expression = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (expression == null) expression = "";
            String evaluated = expression;
            try {
                String afterVarReplace = replaceVariables(evaluated);
                evaluated = evaluateExpression(afterVarReplace != null ? afterVarReplace : matcher.group());
            } catch (Exception ignored) {
                evaluated = matcher.group();
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(evaluated));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    @SuppressWarnings("ConstantConditions")
    private String replaceVariables(String expression) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return null;
        LocalPlayer player = client.player;
        if (player == null) return null;

        Map<String, String> variables = Map.of(
                "username", player.getName().getString(),
                "maxHealth", String.valueOf(player.getMaxHealth()),
                "health", String.valueOf(player.getHealth()),
                "hunger", String.valueOf(player.getFoodData().getFoodLevel()),
                "x", String.valueOf(player.getBlockX()),
                "y", String.valueOf(player.getBlockY()),
                "z", String.valueOf(player.getBlockZ())
        );

        String result = expression;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (expression.contains(key)) {
                result = result.replace(key, value);
            }
        }

        return expression.equals(result) ? null : result;
    }

    private String evaluateExpression(String expression) {
        List<String> output = new ArrayList<>();
        Deque<String> operators = new ArrayDeque<>();

        Map<String, Integer> precedence = Map.of(
                "+", 2,
                "-", 2,
                "*", 3,
                "/", 3,
                "%", 3,
                "^", 4
        );

        String tokenPattern = "(-?\\d+(?:\\.\\d+)?)|\\s*([()+\\-*/%^]|" + String.join("|", FUNCTIONS) + ")\\s*";
        Pattern tokenRegex = Pattern.compile(tokenPattern);
        Matcher matcher = tokenRegex.matcher(expression);
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            String token = matcher.group().trim();
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }

        if (tokens.size() < 2) return expression;

        for (String token : tokens) {
            if (token.equals("(")) {
                operators.addLast(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.getLast().equals("(")) {
                    output.add(operators.removeLast());
                }
                if (!operators.isEmpty() && operators.getLast().equals("(")) {
                    operators.removeLast();
                }
            } else if (isDouble(token)) {
                output.add(token);
            } else if (precedence.containsKey(token) || FUNCTIONS.contains(token)) {
                int tokenPrec = FUNCTIONS.contains(token) ? 5 : precedence.get(token);
                while (!operators.isEmpty() && !operators.getLast().equals("(")) {
                    String last = operators.getLast();
                    int lastPrec = FUNCTIONS.contains(last) ? 5 : precedence.getOrDefault(last, 0);
                    boolean leftAssoc = !token.equals("^");
                    if (tokenPrec < lastPrec || (tokenPrec == lastPrec && leftAssoc)) {
                        output.add(operators.removeLast());
                    } else {
                        break;
                    }
                }
                operators.addLast(token);
            }
        }

        while (!operators.isEmpty()) {
            output.add(operators.removeLast());
        }

        Deque<Double> stack = new ArrayDeque<>();
        for (String token : output) {
            if (isDouble(token)) {
                stack.addLast(Double.parseDouble(token));
            } else if (FUNCTIONS.contains(token)) {
                double operand = stack.removeLast();
                double result;
                switch (token) {
                    case "min" -> result = min(operand, stack.removeLast());
                    case "max" -> result = max(operand, stack.removeLast());
                    case "floor" -> result = floor(operand);
                    case "ceil" -> result = ceil(operand);
                    case "round" -> result = round(operand);
                    case "sqrt" -> result = sqrt(operand);
                    case "cbrt" -> result = cbrt(operand);
                    case "abs" -> result = abs(operand);
                    case "sin" -> result = sin(operand);
                    case "cos" -> result = cos(operand);
                    case "tan" -> result = tan(operand);
                    case "asin" -> result = asin(operand);
                    case "acos" -> result = acos(operand);
                    case "atan" -> result = atan(operand);
                    case "atan2" -> result = atan2(stack.removeLast(), operand);
                    case "sinh" -> result = sinh(operand);
                    case "cosh" -> result = cosh(operand);
                    case "tanh" -> result = tanh(operand);
                    case "exp" -> result = exp(operand);
                    case "ln" -> result = log(operand);
                    case "log" -> {
                        double value = stack.removeLast();
                        result = log(value) / log(operand);
                    }
                    case "log2" -> result = log(operand) / log(2.0);
                    case "log10" -> result = log10(operand);
                    default -> throw new IllegalArgumentException("Unknown operator: " + token);
                }
                stack.addLast(result);
            } else if (precedence.containsKey(token)) {
                double operand2 = stack.removeLast();
                double operand1 = stack.removeLast();
                double result = getResult(token, operand1, operand2);
                stack.addLast(result);
            }
        }

        if (stack.isEmpty()) return expression;
        double result = stack.getFirst();
        if (Double.isInfinite(result)) return "∞";
        if (result % 1.0 == 0.0) return Integer.toString((int) result);
        return Double.toString(result);
    }

    private static double getResult(String token, double operand1, double operand2) {
        return switch (token) {
            case "+" -> operand1 + operand2;
            case "-" -> operand1 - operand2;
            case "*" -> operand1 * operand2;
            case "/" -> operand1 / operand2;
            case "%" -> operand1 % operand2;
            case "^" -> pow(operand1, operand2);
            default -> throw new IllegalArgumentException("Unknown operator: " + token);
        };
    }

    private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public enum CmdType {
        MESSAGE,
        COMMAND,
        NONE
    }
}
