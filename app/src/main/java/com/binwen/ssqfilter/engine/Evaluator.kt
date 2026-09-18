package com.binwen.ssqfilter.engine

import com.binwen.ssqfilter.model.Ticket

class Evaluator {

    // 红球结构条件
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

    // 蓝球结构条件
    var allowOddBlue = true
    var allowEvenBlue = true

    var minBlue = 1
    var maxBlue = 16

    var allowPrimeBlue = true
    var allowCompositeBlue = true

    fun evaluate(ticket: Ticket): Boolean {

        val reds = ticket.reds
        val blue = ticket.blue

        // -------------------------
        // 红球结构条件
        // -------------------------

        // 1. 和值
        val sum = reds.sum()
        if (sum !in minSum..maxSum) return false

        // 2. 跨度
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

        // -------------------------
        // 蓝球结构条件
        -------------------------

        // 6. 蓝球奇偶
        if (blue % 2 == 1 && !allowOddBlue) return false
        if (blue % 2 == 0 && !allowEvenBlue) return false

        // 7. 蓝球区间
        if (blue !in minBlue..maxBlue) return false

        // 8. 蓝球质合
        val isPrime = isPrime(blue)
        if (isPrime && !allowPrimeBlue) return false
        if (!isPrime && !allowCompositeBlue) return false

        return true
    }

    private fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        for (i in 2..Math.sqrt(n.toDouble()).toInt()) {
            if (n % i == 0) return false
        }
        return true
    }
}
