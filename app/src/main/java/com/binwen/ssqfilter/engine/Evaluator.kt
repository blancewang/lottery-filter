package com.binwen.ssqfilter.engine

import com.binwen.ssqfilter.model.Ticket
import kotlin.math.abs
import kotlin.math.sqrt

class Evaluator {

    // -------------------------
    // 总容错系统
    // -------------------------
    var tolerance = 0   // 全局容错

    // -------------------------
    // 红球基础结构
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

    // AC 值
    var minAC = 0
    var maxAC = 10

    // -------------------------
    // 分区结构（三区 / 四区 / 五区）
    // -------------------------

    // 三区：1–11 / 12–22 / 23–33
    var minQ1 = 0
    var maxQ1 = 6

    var minQ2 = 0
    var maxQ2 = 6

    var minQ3 = 0
    var maxQ3 = 6

    // 四区：1–8 / 9–16 / 17–24 / 25–33
    var minZ1 = 0
    var maxZ1 = 6

    var minZ2 = 0
    var maxZ2 = 6

    var minZ3 = 0
    var maxZ3 = 6

    var minZ4 = 0
    var maxZ4 = 6

    // 五区（自定义）
    var zone5 = listOf(1..7, 8..14, 15..21, 22..28, 29..33)
    var minW = IntArray(5) { 0 }
    var maxW = IntArray(5) { 6 }
    // -------------------------
    // 路数结构（0 路 / 1 路 / 2 路）
    // -------------------------
    var minL0 = 0
    var maxL0 = 6

    var minL1 = 0
    var maxL1 = 6

    var minL2 = 0
    var maxL2 = 6

    // -------------------------
    // 行列结构（矩阵结构）
    // -------------------------
    // 行：1~33 映射为 6 行
    var minRow1 = 0
    var maxRow1 = 6

    var minRow2 = 0
    var maxRow2 = 6

    var minRow3 = 0
    var maxRow3 = 6

    var minRow4 = 0
    var maxRow4 = 6

    var minRow5 = 0
    var maxRow5 = 6

    var minRow6 = 0
    var maxRow6 = 6

    // 列：1~33 映射为 6 列
    var minCol1 = 0
    var maxCol1 = 6

    var minCol2 = 0
    var maxCol2 = 6

    var minCol3 = 0
    var maxCol3 = 6

    var minCol4 = 0
    var maxCol4 = 6

    var minCol5 = 0
    var maxCol5 = 6

    var minCol6 = 0
    var maxCol6 = 6

    // -------------------------
    // 江恩结构（角度 / 方位）
    // -------------------------
    var allowGannEast = true
    var allowGannWest = true
    var allowGannSouth = true
    var allowGannNorth = true

    var allowGann45 = true
    var allowGann90 = true
    var allowGann135 = true
    var allowGann180 = true
    // -------------------------
    // 自定义组结构（任意组）
    // -------------------------
    var customGroups: List<List<Int>> = listOf()
    var minCustom: IntArray = IntArray(0)
    var maxCustom: IntArray = IntArray(0)

    // -------------------------
    // 蓝球结构（高级）
    // -------------------------
    var allowOddBlue = true
    var allowEvenBlue = true

    var minBlue = 1
    var maxBlue = 16

    var allowPrimeBlue = true
    var allowCompositeBlue = true

    // 蓝球分区（可扩展）
    var minBlueZone1 = 0
    var maxBlueZone1 = 1

    var minBlueZone2 = 0
    var maxBlueZone2 = 1

    // -------------------------
    // 主过滤函数
    // -------------------------
    fun evaluate(ticket: Ticket): Boolean {

        val reds = ticket.reds
        val blue = ticket.blue

        var fail = 0   // 失败计数器

        // -------------------------
        // 红球基础结构
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

        // 6. AC 值
        val ac = calcAC(reds)
        if (ac !in minAC..maxAC) fail++
        // -------------------------
        // 分区过滤（三区）
        // -------------------------
        val q1 = reds.count { it in 1..11 }
        val q2 = reds.count { it in 12..22 }
        val q3 = reds.count { it in 23..33 }

        if (q1 !in minQ1..maxQ1) fail++
        if (q2 !in minQ2..maxQ2) fail++
        if (q3 !in minQ3..maxQ3) fail++

        // -------------------------
        // 四区过滤
        // -------------------------
        val z1 = reds.count { it in 1..8 }
        val z2 = reds.count { it in 9..16 }
        val z3 = reds.count { it in 17..24 }
        val z4 = reds.count { it in 25..33 }

        if (z1 !in minZ1..maxZ1) fail++
        if (z2 !in minZ2..maxZ2) fail++
        if (z3 !in minZ3..maxZ3) fail++
        if (z4 !in minZ4..maxZ4) fail++

        // -------------------------
        // 五区过滤（自定义）
        // -------------------------
        for (i in 0 until 5) {
            val count = reds.count { it in zone5[i] }
            if (count !in minW[i]..maxW[i]) fail++
        }

        // -------------------------
        // 路数过滤
        // -------------------------
        val l0 = reds.count { it % 3 == 0 }
        val l1 = reds.count { it % 3 == 1 }
        val l2 = reds.count { it % 3 == 2 }

        if (l0 !in minL0..maxL0) fail++
        if (l1 !in minL1..maxL1) fail++
        // -------------------------
        // 江恩结构（角度 / 方位）
        // -------------------------
        for (r in reds) {
            val angle = (r * 360 / 33)

            // 方位判断
            val isEast = angle in 0..89
            val isSouth = angle in 90..179
            val isWest = angle in 180..269
            val isNorth = angle in 270..359

            if (isEast && !allowGannEast) fail++
            if (isSouth && !allowGannSouth) fail++
            if (isWest && !allowGannWest) fail++
            if (isNorth && !allowGannNorth) fail++

            // 角度判断
            val a45 = angle % 45 == 0
            val a90 = angle % 90 == 0
            val a135 = angle % 135 == 0
            val a180 = angle % 180 == 0

            if (a45 && !allowGann45) fail++
            if (a90 && !allowGann90) fail++
            if (a135 && !allowGann135) fail++
            if (a180 && !allowGann180) fail++
        }
// -------------------------
    // AC 值计算函数
    // -------------------------
    private fun calcAC(reds: List<Int>): Int {
        val diffs = mutableSetOf<Int>()
        for (i in reds.indices) {
            for (j in i + 1 until reds.size) {
                diffs.add(abs(reds[i] - reds[j]))
            }
        }
        return diffs.size - (reds.size - 1)
    }

    // -------------------------
    // 判断质数
    // -------------------------
    private fun isPrime(n: Int): Boolean {
        if (n < 2) return false
        for (i in 2..sqrt(n.toDouble()).toInt()) {
            if (n % i == 0) return false
        }
        return true
    }
}
        // -------------------------
        // 自定义组过滤
        // -------------------------
        for (i in customGroups.indices) {
            val group = customGroups[i]
            val count = reds.count { it in group }
            if (count !in minCustom[i]..maxCustom[i]) fail++
        }

        // -------------------------
        // 蓝球结构
        // -------------------------
        if (blue % 2 == 1 && !allowOddBlue) fail++
        if (blue % 2 == 0 && !allowEvenBlue) fail++

        if (blue !in minBlue..maxBlue) fail++

        val isPrimeBlue = isPrime(blue)
        if (isPrimeBlue && !allowPrimeBlue) fail++
        if (!isPrimeBlue && !allowCompositeBlue) fail++

        // 蓝球分区
        val blueZone1 = blue in 1..8
        val blueZone2 = blue in 9..16

        if (blueZone1 && !(minBlueZone1 <= 1 && 1 <= maxBlueZone1)) fail++
        if (blueZone2 && !(minBlueZone2 <= 1 && 1 <= maxBlueZone2)) fail++

        // -------------------------
        // 总容错判断
        // -------------------------
        return fail <= tolerance
    }
    
