package com.binwen.ssqfilter.engine

import com.binwen.ssqfilter.model.Ticket

class Evaluator {

    // -------------------------
    // 容错系统（基础版）
    // -------------------------
    var tolerance = 0   // 总容错值

    // -------------------------
    // 红球结构条件
    // -------------------------
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

    // -------------------------
    // 蓝球结构条件
    // -------------------------
    var allowOddBlue = true
    var allowEvenBlue = true

    var minBlue = 1
    var maxBlue = 16

    var allowPrimeBlue = true
    var allowCompositeBlue = true

    fun evaluate(ticket: Ticket): Boolean {

        val reds = ticket.reds
        val blue = ticket.blue

        var fail = 0   // 失败计数器

        // -------------------------
        // 红球结构条件
        // -------------------------

        // 1. 和值
        val sum = reds.sum()
        if (sum !in minSum..maxSum) fail++

        // 2. 跨度
        val span = reds.last() - reds.first()
        if (span !in minSpan..maxSpan) fail++

        // 3. 奇数个数
        val oddCount = reds.count { it % 2 == 1 }
        if (oddCount !in minOdd..maxOdd) fail++

        // 4. 大号个数（>16）
        val bigCount = reds.count { it > 16 }
        if (bigCount !in minBig..maxBig) fail++

        // 5. 连号个数
        var consecutive = 0
        for (i in 0 until reds.size - 1) {
            if (reds[i + 1] == reds[i] + 1) consecutive++
        }
        if (consecutive !in minConsecutive..maxConsecutive) fail++

        // -------------------------
        // 蓝球结构条件
        // -------------------------

        // 6. 蓝球奇偶
        if (blue % 2 == 1 && !allowOddBlue) fail++
        if (blue % 2 == 0 && !allowEvenBlue) fail++

        // 7. 蓝球区间
        if (blue !in minBlue..maxBlue) fail++

        // 8. 蓝球质合
        val isPrime = isPrime(blue)
        if (isPrime && !allowPrimeBlue) fail++
        if (!isPrime && !allowCompositeBlue) fail++

        // -------------------------
        // 容错判断
        // -------------------------
        return fail <= tolerance
    }

    private fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        for (i in 2..Math.sqrt(n.toDouble()).toInt()) {
            if (n % i == 0) return false
        }
        return true
    }
}
