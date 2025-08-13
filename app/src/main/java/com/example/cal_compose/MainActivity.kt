package com.example.cal_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cal_compose.ui.theme.Cal_composeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cal_composeTheme {
                // Scaffold पूरे UI का base layout देता है
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    CalculatorUI(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorUI(modifier: Modifier = Modifier) {
    // स्क्रीन पर दिखने वाला current input/result store करने के लिए state
    var input by remember { mutableStateOf("") }

    // Background और layout setup
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF101820)) // Dark background
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display section (input और result दिखाने के लिए)
        Text(
            text = input,
            color = Color.White,
            fontSize = 48.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textAlign = TextAlign.End
        )

        // Calculator buttons layout
        val buttons = listOf(
            listOf("C", "⌫", "%", "/"),
            listOf("7", "8", "9", "x"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("00", "0", ".", "=")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Loop करके हर row बनाना
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { symbol ->
                        CalculatorButton(
                            symbol = symbol,
                            onClick = {
                                input = handleButtonClick(input, symbol)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f) // Square buttons
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(symbol: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // अलग-अलग symbols के लिए रंग तय करना
    val backgroundColor = when (symbol) {
        "C" -> Color(0xFFE84545) // Red
        "⌫" -> Color(0xFFFFA500) // Orange
        "/", "x", "-", "+", "%", "=" -> Color(0xFF1E90FF) // Blue
        else -> Color(0xFF2E2E2E) // Dark grey
    }

    // बटन UI
    Box(
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Button click logic
fun handleButtonClick(currentInput: String, symbol: String): String {
    return when (symbol) {
        "C" -> "" // Clear input
        "⌫" -> currentInput.dropLast(1) // Remove last character
        "=" -> calculateResult(currentInput) // Calculate result
        else -> currentInput + symbol // Append symbol
    }
}

// Expression calculation function
fun calculateResult(expression: String): String {
    return try {
        val replaced = expression.replace("x", "*")
        val result = eval(replaced) // Custom eval method नीचे define है
        if (result % 1 == 0.0) result.toInt().toString() else result.toString()
    } catch (e: Exception) {
        "Error"
    }
}

// Simple evaluator (केवल basic math support)
fun eval(expr: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < expr.length) expr[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < expr.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> x /= parseFactor()
                    eat('%'.code) -> x %= parseFactor()
                    else -> return x
                }
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                x = expr.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            return x
        }
    }.parse()
}
