package com.binwen.ssqfilter

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.binwen.ssqfilter.engine.Enumerator
import com.binwen.ssqfilter.engine.Evaluator
import com.binwen.ssqfilter.model.Pools
import com.binwen.ssqfilter.model.RedPool

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputKill = findViewById<EditText>(R.id.inputKill)
        val inputDan = findViewById<EditText>(R.id.inputDan)
        val inputCandidates = findViewById<EditText>(R.id.inputCandidates)
        val inputBlue = findViewById<EditText>(R.id.inputBlue)
        val inputTolerance = findViewById<EditText>(R.id.inputTolerance)
        val btnRun = findViewById<Button>(R.id.btnRun)
        val output = findViewById<TextView>(R.id.output)

        btnRun.setOnClickListener {

            val kill = parseList(inputKill.text.toString())
            val dan = parseList(inputDan.text.toString())
            val candidates = parseList(inputCandidates.text.toString())
            val blues = parseList(inputBlue.text.toString())

            val tolerance = inputTolerance.text.toString().toIntOrNull() ?: 0

            val redPool = RedPool(kill, dan, candidates)
            val pools = Pools(candidates, blues)

            val enumerator = Enumerator()
            val evaluator = Evaluator()
            evaluator.tolerance = tolerance

            val result = StringBuilder()

            for (ticket in enumerator.enumerate(redPool, pools)) {
                if (evaluator.evaluate(ticket)) {
                    result.append(ticket.reds.joinToString(","))
                    result.append(" + ")
                    result.append(ticket.blue)
                    result.append("\n")
                }
            }

            output.text = result.toString()
        }
    }

    private fun parseList(text: String): List<Int> {
        if (text.contains("-")) {
            val parts = text.split("-")
            val start = parts[0].toInt()
            val end = parts[1].toInt()
            return (start..end).toList()
        }
        return text.split(",").mapNotNull { it.toIntOrNull() }
    }
}
