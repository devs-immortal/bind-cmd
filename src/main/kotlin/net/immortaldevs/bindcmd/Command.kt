package net.immortaldevs.bindcmd

import net.minecraft.client.MinecraftClient
import kotlin.math.*

enum class CmdType {
    MESSAGE,
    COMMAND,
    NONE
}

class Command(command: String) {
    var type: CmdType private set
    var command: String private set

    init {
        if (command.isEmpty()) {
            this.type = CmdType.NONE
            this.command = ""
        } else if (command[0] == '/') {
            this.type = CmdType.COMMAND
            this.command = processMessage(command.substring(1))
        } else if (command[0] == '@') {
            val (type, cmd) = getLastMessage(command) ?: Pair(CmdType.NONE, "")
            this.type = type
            this.command = processMessage(cmd)
        } else {
            this.type = CmdType.MESSAGE
            this.command = processMessage(command)
        }
    }

    private fun getLastMessage(cmd: String): Pair<CmdType, String>? {
        val history = MinecraftClient.getInstance().inGameHud?.chatHud?.messageHistory ?: return null
        val offset = if (cmd.length > 1) cmd.substring(1).toIntOrNull() ?: 1 else 1
        if (history.size - offset < 0) return null
        val message = history[history.size - offset]
        if (message[0] == '/') {
            return Pair(CmdType.COMMAND, message.substring(1))
        }
        return Pair(CmdType.MESSAGE, message)
    }

    private fun processMessage(message: String): String {
        if (!message.contains("$")) return message
        val regex = Regex("""\$\{(.+?)}|\$(\w+)""")
        return message.replace(regex) { matchResult ->
            var expression = matchResult.groups[1]?.value ?: matchResult.groups[2]?.value ?: ""
            try {
                expression = replaceVariables(expression)
                expression = evaluateExpression(expression)
            } catch (_: Exception) {
                // Ignore
            }
            expression
        }
    }

    private fun replaceVariables(expression: String): String {
        val player = MinecraftClient.getInstance().player ?: return expression
        val variables = mapOf(
            "username" to player.name.string,
            "maxHealth" to player.maxHealth.toString(),
            "health" to player.health.toString(),
            "hunger" to player.hungerManager.foodLevel.toString(),
            "x" to player.blockX.toString(),
            "y" to player.blockY.toString(),
            "z" to player.blockZ.toString(),
        )
        variables.forEach { (key, value) ->
            if (expression.contains(key)) return expression.replace(key, value)
        }
        return expression
    }

    private fun evaluateExpression(expression: String): String {
        val output = mutableListOf<String>()
        val operators = mutableListOf<String>()

        val methods = listOf(
            "sqrt", "cbrt", "min", "max", "floor", "ceil", "round", "abs", "sin", "cos", "tan", "asin",
            "acos", "atan", "atan2", "sinh", "cosh", "tanh", "exp", "ln", "log", "log2", "log10"
        )
        val precedence = mapOf("+" to 2, "-" to 2, "*" to 3, "/" to 3, "%" to 3, "^" to 4) + methods.map { it to 5 }

        val pattern = "([\\-0-9.]+)|\\s*([()+\\-*/%^]|${methods.joinToString("|")})\\s*"
        val tokens = pattern.toRegex().findAll(expression.replace(" ", "")).map { it.value }.toList()

        if (tokens.size < 2) return expression

        for (token in tokens) {
            when {
                token == "(" -> operators.add(token)
                token == ")" -> {
                    while (operators.last() != "(")
                        output.add(operators.removeLast())
                    operators.removeLast()
                }

                token.isDouble() -> output.add(token)
                token in precedence.keys -> {
                    while (operators.isNotEmpty() && operators.last() != "(" &&
                        (precedence[token]!! < precedence[operators.last()]!! ||
                                (precedence[token] == precedence[operators.last()] && token !in listOf("*", "/", "%")))
                    ) {
                        output.add(operators.removeLast())
                    }
                    operators.add(token)
                }
            }
        }

        while (operators.isNotEmpty()) {
            output.add(operators.removeLast())
        }

        val stack = mutableListOf<Double>()

        for (token in output) {
            when {
                token.isDouble() -> stack.add(token.toDouble())
                token in methods -> {
                    val operand = stack.removeLast()
                    val result = when (token) {
                        "min" -> min(operand, stack.removeLast())
                        "max" -> max(operand, stack.removeLast())
                        "floor" -> floor(operand)
                        "ceil" -> ceil(operand)
                        "round" -> round(operand)
                        "sqrt" -> sqrt(operand)
                        "cbrt" -> cbrt(operand)
                        "abs" -> abs(operand)
                        "sin" -> sin(operand)
                        "cos" -> cos(operand)
                        "tan" -> tan(operand)
                        "asin" -> asin(operand)
                        "acos" -> acos(operand)
                        "atan" -> atan(operand)
                        "atan2" -> atan2(operand, stack.removeLast())
                        "sinh" -> sinh(operand)
                        "cosh" -> cosh(operand)
                        "tanh" -> tanh(operand)
                        "exp" -> exp(operand)
                        "ln" -> ln(operand)
                        "log" -> log(stack.removeLast(), operand)
                        "log2" -> log2(operand)
                        "log10" -> log10(operand)
                        else -> throw IllegalArgumentException("Unknown operator: $token")
                    }
                    stack.add(result)
                }

                token in precedence.keys -> {
                    val operand2 = stack.removeLast()
                    val operand1 = stack.removeLast()
                    val result = when (token) {
                        "+" -> operand1 + operand2
                        "-" -> operand1 - operand2
                        "*" -> operand1 * operand2
                        "/" -> operand1 / operand2
                        "%" -> operand1 % operand2
                        "^" -> operand1.pow(operand2)
                        else -> throw IllegalArgumentException("Unknown operator: $token")
                    }
                    stack.add(result)
                }
            }
        }

        val result = stack.first()
        if (result.isInfinite())
            return "∞"
        if (result % 1 == 0.0)
            return result.toInt().toString()
        return result.toString()
    }

    private fun String.isDouble() = this.toDoubleOrNull() != null
}