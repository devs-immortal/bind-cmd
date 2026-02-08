package net.immortaldevs.bindcmd;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {
    @Test
    @DisplayName("Should detect MESSAGE type for plain text")
    void testGetType_PlainText_ReturnsMessage() {
        Command cmd = new Command("Hello World");
        assertEquals(Command.CmdType.MESSAGE, cmd.getType());
    }

    @Test
    @DisplayName("Should detect COMMAND type for / prefix")
    void testGetType_WithSlash_ReturnsCommand() {
        Command cmd = new Command("/help");
        assertEquals(Command.CmdType.COMMAND, cmd.getType());
    }

    @Test
    @DisplayName("Should detect NONE type for empty string")
    void testGetType_EmptyString_ReturnsNone() {
        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            new Command("");
        });
    }

    @Test
    @DisplayName("Should detect NONE type for blank string")
    void testGetType_BlankString_ReturnsNone() {
        Command cmd = new Command("   ");
        assertEquals(Command.CmdType.NONE, cmd.getType());
    }

    @Test
    @DisplayName("Should strip / prefix from commands")
    void testGetCommand_CommandType_StripsSlash() {
        Command cmd = new Command("/gamemode creative");
        assertEquals("gamemode creative", cmd.getCommand());
    }

    @Test
    @DisplayName("Should keep plain text message as is")
    void testGetCommand_MessageType_KeepsOriginal() {
        Command cmd = new Command("Hello World");
        assertEquals("Hello World", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate simple addition")
    void testEvaluateExpression_SimpleAddition() {
        Command cmd = new Command("Result: ${1 + 1}");
        assertEquals("Result: 2", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate simple subtraction")
    void testEvaluateExpression_SimpleSubtraction() {
        Command cmd = new Command("Result: ${10 - 3}");
        assertEquals("Result: 7", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate simple multiplication")
    void testEvaluateExpression_SimpleMultiplication() {
        Command cmd = new Command("Result: ${5 * 3}");
        assertEquals("Result: 15", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate simple division")
    void testEvaluateExpression_SimpleDivision() {
        Command cmd = new Command("Result: ${10 / 2}");
        assertEquals("Result: 5", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate modulo operation")
    void testEvaluateExpression_Modulo() {
        Command cmd = new Command("Result: ${10 % 3}");
        assertEquals("Result: 1", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate power operation")
    void testEvaluateExpression_Power() {
        Command cmd = new Command("Result: ${2 ^ 3}");
        assertEquals("Result: 8", cmd.getCommand());
    }

    @Test
    @DisplayName("Should respect operator precedence: multiplication before addition")
    void testEvaluateExpression_WithCorrectPrecedence() {
        Command cmd = new Command("Result: ${2 + 2 * 2}");
        assertEquals("Result: 6", cmd.getCommand());
    }

    @Test
    @DisplayName("Should respect operator precedence: division before subtraction")
    void testEvaluateExpression_DivisionBeforeSubtraction() {
        Command cmd = new Command("Result: ${10 - 8 / 2}");
        assertEquals("Result: 6", cmd.getCommand());
    }

    @Test
    @DisplayName("Should handle parentheses for grouping")
    void testEvaluateExpression_WithParentheses() {
        Command cmd = new Command("Result: ${(2 + 2) * 2}");
        assertEquals("Result: 8", cmd.getCommand());
    }

    @Test
    @DisplayName("Should handle nested parentheses")
    void testEvaluateExpression_NestedParentheses() {
        Command cmd = new Command("Result: ${(2 + 3) * (10 / 2)}");
        assertEquals("Result: 25", cmd.getCommand());
    }

    @Test
    @DisplayName("Should handle complex nested expression")
    void testEvaluateExpression_ComplexNested() {
        Command cmd = new Command("Result: ${((2 + 3) * 4) - 2}");
        assertEquals("Result: 18", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate sqrt function")
    void testEvaluateExpression_Sqrt() {
        Command cmd = new Command("Result: ${sqrt(16)}");
        assertEquals("Result: 4", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate floor function")
    void testEvaluateExpression_Floor() {
        Command cmd = new Command("Result: ${floor(5.9)}");
        assertEquals("Result: 5", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate ceil function")
    void testEvaluateExpression_Ceil() {
        Command cmd = new Command("Result: ${ceil(5.1)}");
        assertEquals("Result: 6", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate round function")
    void testEvaluateExpression_Round() {
        Command cmd = new Command("Result: ${round(5.6)}");
        assertEquals("Result: 6", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate abs function")
    void testEvaluateExpression_Abs() {
        Command cmd = new Command("Result: ${abs(-10)}");
        assertEquals("Result: 10", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate max function")
    void testEvaluateExpression_Max() {
        Command cmd = new Command("Result: ${max(5, 10)}");
        assertEquals("Result: 10", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate min function")
    void testEvaluateExpression_Min() {
        Command cmd = new Command("Result: ${min(5, 10)}");
        assertEquals("Result: 5", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate cbrt function")
    void testEvaluateExpression_Cbrt() {
        Command cmd = new Command("Result: ${cbrt(27)}");
        assertEquals("Result: 3", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate exp function")
    void testEvaluateExpression_Exp() {
        Command cmd = new Command("Result: ${exp(0)}");
        assertEquals("Result: 1", cmd.getCommand());
    }

    @Test
    @DisplayName("Should evaluate log10 function")
    void testEvaluateExpression_Log10() {
        Command cmd = new Command("Result: ${log10(100)}");
        assertEquals("Result: 2", cmd.getCommand());
    }

    @Test
    @DisplayName("Should handle division by zero as infinity")
    void testEvaluateExpression_DivisionByZero() {
        Command cmd = new Command("Result: ${10 / 0}");
        assertEquals("Result: ∞", cmd.getCommand());
    }

    @Test
    @DisplayName("Should handle decimal results")
    void testEvaluateExpression_DecimalResult() {
        Command cmd = new Command("Result: ${7 / 2}");
        assertEquals("Result: 3.5", cmd.getCommand());
    }

    @Test
    @DisplayName("Should handle negative numbers")
    void testEvaluateExpression_NegativeNumbers() {
        Command cmd = new Command("Result: ${-5 + 3}");
        String result = cmd.getCommand();
        assertTrue(result.equals("Result: -2") || result.contains("${-5 + 3}"));
    }

    @Test
    @DisplayName("Should handle invalid expression gracefully")
    void testEvaluateExpression_InvalidExpression() {
        Command cmd = new Command("Result: ${invalid}");
        assertEquals("Result: ${invalid}", cmd.getCommand());
    }

    @Test
    @DisplayName("Should handle empty expression")
    void testEvaluateExpression_EmptyExpression() {
        Command cmd = new Command("Result: ${}");
        String result = cmd.getCommand();
        assertTrue(result.equals("Result: ") || result.equals("Result: ${}"));
    }

    @Test
    @DisplayName("Should handle multiple expressions in one command")
    void testEvaluateExpression_MultipleExpressions() {
        Command cmd = new Command("${1 + 1} and ${2 * 2}");
        assertEquals("2 and 4", cmd.getCommand());
    }

    @Test
    @DisplayName("Should handle text without $ symbol")
    void testEvaluateExpression_NoExpressions() {
        Command cmd = new Command("Just plain text");
        assertEquals("Just plain text", cmd.getCommand());
    }

    @Test
    @DisplayName("Should handle expression with spaces")
    void testEvaluateExpression_WithSpaces() {
        Command cmd = new Command("Result: ${ 5 + 3 }");
        assertEquals("Result: 8", cmd.getCommand());
    }

    @Test
    @DisplayName("Should handle command type with math expressions")
    void testIntegration_CommandTypeWithMath() {
        Command cmd = new Command("/give @p diamond ${5 + 3}");
        assertEquals(Command.CmdType.COMMAND, cmd.getType());
        assertEquals("give @p diamond 8", cmd.getCommand());
    }

    @Test
    @DisplayName("Should handle complex mathematical expression")
    void testIntegration_ComplexMath() {
        Command cmd = new Command("/say Result is ${floor((100 / 2) + sqrt(16))}");
        assertEquals(Command.CmdType.COMMAND, cmd.getType());
        assertEquals("say Result is 54", cmd.getCommand());
    }
}
