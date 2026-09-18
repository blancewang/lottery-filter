package com.binwen.ssqfilter.engine

import com.binwen.ssqfilter.model.Ticket

class Evaluator {

    // 基础结构条件
    var minSum = 0
    var maxSum = 200

    var minSpan = 0
    var maxSpan = 33

    var minOdd = 0
    var maxOdd = 6

    var minBig = 0
    var maxBig = 6

    var minConsecutive = 0
    var maxConsecutive = 6

    fun evaluate(ticket: Ticket): Boolean {

        val reds = ticket.reds

        // 1. 和值
        val sum = reds.sum()
        if (sum !in minSum..maxSum) return false

        // 2. 跨度（最大 - 最小）
        val span = reds.last() - reds.first()
        if (span !in minSpan..maxSpan) return false

        // 3. 奇数个数
        val oddCount = reds.count { it % 2 == 1 }
        if (oddCount !in minOdd..maxOdd) return false

        // 4. 大号个数（>16）
        val bigCount = reds.count { it > 16 }
        if (bigCount !in minBig..maxBig) return false

        // 5. 连号个数
        var consecutive = 0
        for (i in 0 until reds.size - 1) {
            if (reds[i + 1] == reds[i] + 1) consecutive++
        }
        if (consecutive !in minConsecutive..maxConsecutive) return false

        return true
    }
}
