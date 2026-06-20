package net.immortaldevs.bindcmd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        Command cmd = new Command("");
        assertEquals(Command.CmdType.NONE, cmd.getType());
        assertEquals("", cmd.getCommand());
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
        assertEquals("Result: -2", cmd.getCommand());
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
        assertEquals("Result: ${}", cmd.getCommand());
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

    @Test
    @DisplayName("Exponentiation is right-associative: 2^3^2 = 2^(3^2) = 512")
    void testEvaluateExpression_PowerIsRightAssociative() {
        Command cmd = new Command("${2 ^ 3 ^ 2}");
        assertEquals("512", cmd.getCommand());
    }

    @Test
    @DisplayName("Subtraction is left-associative: 10 - 3 - 2 = 5")
    void testEvaluateExpression_SubtractionIsLeftAssociative() {
        Command cmd = new Command("${10 - 3 - 2}");
        assertEquals("5", cmd.getCommand());
    }

    @Test
    @DisplayName("Division is left-associative: 20 / 4 / 5 = 1")
    void testEvaluateExpression_DivisionIsLeftAssociative() {
        Command cmd = new Command("${20 / 4 / 5}");
        assertEquals("1", cmd.getCommand());
    }

    @Test
    @DisplayName("Negative operand after an operator: 3 * -2 = -6")
    void testEvaluateExpression_NegativeOperandAfterOperator() {
        Command cmd = new Command("${3 * -2}");
        assertEquals("-6", cmd.getCommand());
    }

    @Test
    @DisplayName("Subtracting a negative: 5 - -3 = 8")
    void testEvaluateExpression_SubtractNegative() {
        Command cmd = new Command("${5 - -3}");
        assertEquals("8", cmd.getCommand());
    }

    @Test
    @DisplayName("Large whole-number product must not overflow int formatting")
    void testEvaluateExpression_LargeProductDoesNotOverflow() {
        Command cmd = new Command("${1000000 * 1000000}");
        assertEquals("1000000000000", cmd.getCommand());
    }

    @Test
    @DisplayName("Large power must not overflow int formatting")
    void testEvaluateExpression_LargePowerDoesNotOverflow() {
        Command cmd = new Command("${2 ^ 40}");
        assertEquals("1099511627776", cmd.getCommand());
    }

    @Test
    @DisplayName("Modulo by zero must not emit raw 'NaN'")
    void testEvaluateExpression_ModuloByZeroNoNaN() {
        Command cmd = new Command("${10 % 0}");
        assertFalse(cmd.getCommand().contains("NaN"), () -> "leaked NaN: " + cmd.getCommand());
    }

    @Test
    @DisplayName("sqrt of a negative must not emit raw 'NaN'")
    void testEvaluateExpression_SqrtNegativeNoNaN() {
        Command cmd = new Command("${sqrt(-1)}");
        assertFalse(cmd.getCommand().contains("NaN"), () -> "leaked NaN: " + cmd.getCommand());
    }

    @Test
    @DisplayName("asin outside [-1,1] must not emit raw 'NaN'")
    void testEvaluateExpression_AsinOutOfDomainNoNaN() {
        Command cmd = new Command("${asin(2)}");
        assertFalse(cmd.getCommand().contains("NaN"), () -> "leaked NaN: " + cmd.getCommand());
    }

    @Test
    @DisplayName("log(base, value) should read as 'log base 2 of 8' = 3")
    void testEvaluateExpression_LogBaseValueOrder() {
        Command cmd = new Command("${log(2, 8)}");
        assertEquals("3", cmd.getCommand());
    }

    @Test
    @DisplayName("log(8, 2) = log base 8 of 2 = 1/3")
    void testEvaluateExpression_LogBaseValueOrderDocumented() {
        Command cmd = new Command("${log(8, 2)}");
        assertTrue(cmd.getCommand().startsWith("0.33333"), () -> "expected ~0.3, got: " + cmd.getCommand());
    }

    @Test
    @DisplayName("log2 and log10 single-arg helpers evaluate correctly")
    void testEvaluateExpression_Log2AndLog10() {
        assertEquals("3", new Command("${log2(8)}").getCommand());
        assertEquals("3", new Command("${log10(1000)}").getCommand());
    }

    @Test
    @DisplayName("Nested functions: sqrt(abs(-16)) = 4")
    void testEvaluateExpression_NestedSqrtAbs() {
        Command cmd = new Command("${sqrt(abs(-16))}");
        assertEquals("4", cmd.getCommand());
    }

    @Test
    @DisplayName("Nested functions: ln(exp(1)) = 1")
    void testEvaluateExpression_NestedLnExp() {
        Command cmd = new Command("${ln(exp(1))}");
        assertEquals("1", cmd.getCommand());
    }

    @Test
    @DisplayName("A lone slash is a COMMAND with an empty body")
    void testGetType_LoneSlash() {
        Command cmd = new Command("/");
        assertEquals(Command.CmdType.COMMAND, cmd.getType());
        assertEquals("", cmd.getCommand());
    }

    @Test
    @DisplayName("@ with a non-numeric offset is handled gracefully (NONE, empty)")
    void testAtPrefix_NonNumericOffset() {
        Command cmd = new Command("@abc");
        assertEquals(Command.CmdType.NONE, cmd.getType());
        assertEquals("", cmd.getCommand());
    }

    @Test
    @DisplayName("@ with no offset is handled gracefully (NONE, empty)")
    void testAtPrefix_BareAt() {
        Command cmd = new Command("@");
        assertEquals(Command.CmdType.NONE, cmd.getType());
        assertEquals("", cmd.getCommand());
    }

    @Test
    @DisplayName("An unknown $var is left untouched when no player is available")
    void testProcessMessage_UnknownVariableUnchanged() {
        Command cmd = new Command("$foo");
        assertEquals(Command.CmdType.MESSAGE, cmd.getType());
        assertEquals("$foo", cmd.getCommand());
    }

    @Test
    @DisplayName("a single-token expression is returned verbatim")
    void testEvaluateExpression_SingleTokenUnchanged() {
        assertEquals("Result: ${5}", new Command("Result: ${5}").getCommand());
        assertEquals("Result: ${-5}", new Command("Result: ${-5}").getCommand());
    }

    @Test
    @DisplayName("an unmatched parenthesis is silently tolerated")
    void testEvaluateExpression_UnmatchedParenTolerated() {
        assertEquals("Result: 8", new Command("Result: ${(5 + 3}").getCommand());
    }

    @Test
    @DisplayName("-Infinity is collapsed to the same ∞ symbol as +Infinity")
    void testEvaluateExpression_NegativeInfinitySymbol() {
        assertEquals("Result: ∞", new Command("Result: ${ln(0)}").getCommand());
    }

    @Test
    @DisplayName("subtraction without surrounding spaces still subtracts")
    void testEvaluateExpression_SubtractionWithoutSpaces() {
        assertEquals("7", new Command("${10-3}").getCommand());
    }

    @Test
    @DisplayName("single-digit subtraction without spaces still subtracts")
    void testEvaluateExpression_SubtractionNoSpacesSingleDigits() {
        assertEquals("0", new Command("${1-1}").getCommand());
    }
}
