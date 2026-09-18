package com.binwen.ssqfilter

import org.json.JSONArray
import org.json.JSONObject

/**
 * 原生组合枚举 + 条件过滤，供 WebView 通过 JavascriptInterface 调用。
 * 比 JS 更快，尤其在候选红球较多时。
 */
object FilterEngine {

    private val PRIMES = booleanArrayOf(
        false, false, true, true, false, true, false, true, false, false,
        false, true, false, true, false, false, false, true, false, true,
        false, false, false, true, false, false, false, false, false, true,
        false, true, false, true
    )
    private val EDGE = BooleanArray(34).also { a ->
        for (i in 1..11) a[i] = true
        for (i in 22..33) a[i] = true
    }

    /**
     * @param configJson 过滤配置 JSON 字符串
     * @return 结果 JSON：{ok,input,count,pass:[[..],...]}
     */
    fun run(configJson: String): String {
        return try {
            val cfg = JSONObject(configJson)
            val maxKeep = cfg.optInt("max", 25000).coerceIn(1, 50000)
            val kills = toIntSet(cfg.optJSONArray("kills"))
            val dans = toIntList(cfg.optJSONArray("dans"))
            val danMin = cfg.optInt("danMin", 0)
            val vals = cfg.optJSONObject("vals") ?: JSONObject()
            val en = cfg.optJSONObject("en") ?: JSONObject()
            val groups = parseGroups(cfg.optJSONArray("groups"))

            val source: List<IntArray>
            val inputN: Int
            if (cfg.has("combos") && cfg.getJSONArray("combos").length() > 0) {
                val arr = cfg.getJSONArray("combos")
                source = ArrayList(arr.length())
                for (i in 0 until arr.length()) {
                    source.add(toIntArray6(arr.getJSONArray(i)))
                }
                inputN = source.size
            } else {
                val pool = toIntList(cfg.optJSONArray("pool")).sorted()
                if (pool.size < 6) {
                    return JSONObject()
                        .put("ok", false)
                        .put("error", "pool_lt_6")
                        .put("input", 0)
                        .put("count", 0)
                        .put("pass", JSONArray())
                        .toString()
                }
                source = comb6(pool)
                inputN = source.size
            }

            val pass = ArrayList<IntArray>(minOf(maxKeep, 4096))
            for (c in source) {
                if (evalOne(c, kills, dans, danMin, vals, en, groups)) {
                    pass.add(c)
                    if (pass.size >= maxKeep) break
                }
            }

            val out = JSONArray()
            for (c in pass) {
                val row = JSONArray()
                for (x in c) row.put(x)
                out.put(row)
            }
            JSONObject()
                .put("ok", true)
                .put("input", inputN)
                .put("count", pass.size)
                .put("pass", out)
                .toString()
        } catch (e: Exception) {
            JSONObject()
                .put("ok", false)
                .put("error", e.message ?: "error")
                .put("input", 0)
                .put("count", 0)
                .put("pass", JSONArray())
                .toString()
        }
    }

    private fun toIntSet(arr: JSONArray?): Set<Int> {
        if (arr == null) return emptySet()
        val s = HashSet<Int>(arr.length())
        for (i in 0 until arr.length()) s.add(arr.getInt(i))
        return s
    }

    private fun toIntList(arr: JSONArray?): List<Int> {
        if (arr == null) return emptyList()
        val list = ArrayList<Int>(arr.length())
        for (i in 0 until arr.length()) list.add(arr.getInt(i))
        return list
    }

    private fun toIntArray6(arr: JSONArray): IntArray {
        val a = IntArray(6)
        for (i in 0 until 6) a[i] = arr.getInt(i)
        return a
    }

    private data class Group(val on: Boolean, val nums: IntArray, val a: Int, val b: Int)

    private fun parseGroups(arr: JSONArray?): List<Group> {
        if (arr == null) return emptyList()
        val list = ArrayList<Group>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val numsArr = o.optJSONArray("nums")
            val nums = if (numsArr == null) IntArray(0) else {
                IntArray(numsArr.length()) { numsArr.getInt(it) }
            }
            list.add(Group(o.optBoolean("on", true), nums, o.optInt("a", 0), o.optInt("b", 6)))
        }
        return list
    }

    /** C(n,6) 迭代生成，避免递归栈与大量临时对象 */
    private fun comb6(pool: List<Int>): List<IntArray> {
        val n = pool.size
        val out = ArrayList<IntArray>(binomialApprox(n, 6))
        if (n < 6) return out
        val idx = intArrayOf(0, 1, 2, 3, 4, 5)
        while (true) {
            val c = IntArray(6) { pool[idx[it]] }
            out.add(c)
            var i = 5
            while (i >= 0 && idx[i] == n - 6 + i) i--
            if (i < 0) break
            idx[i]++
            for (j in i + 1..5) idx[j] = idx[j - 1] + 1
        }
        return out
    }

    private fun binomialApprox(n: Int, k: Int): Int {
        if (k < 0 || k > n) return 0
        var r = 1L
        val kk = minOf(k, n - k)
        for (i in 1..kk) {
            r = r * (n - kk + i) / i
            if (r > 2_000_000) return 2_000_000
        }
        return r.toInt()
    }

    private fun v(vals: JSONObject, key: String, def: Int = 0): Int =
        if (vals.has(key)) vals.getInt(key) else def

    private fun on(en: JSONObject, key: String): Boolean =
        en.optBoolean(key, false) || en.optInt(key, 0) == 1

    private fun inRange(x: Int, a: Int, b: Int): Boolean {
        val lo = minOf(a, b)
        val hi = maxOf(a, b)
        return x in lo..hi
    }

    private fun acValue(c: IntArray): Int {
        val seen = BooleanArray(33)
        var n = 0
        for (i in 0 until 6) for (j in i + 1 until 6) {
            val d = c[j] - c[i]
            if (d in 1..32 && !seen[d]) {
                seen[d] = true
                n++
            }
        }
        return n - 5
    }

    private fun conGroups(c: IntArray): Int {
        var g = 0
        for (i in 1 until 6) {
            if (c[i] == c[i - 1] + 1) {
                if (i == 1 || c[i - 1] != c[i - 2] + 1) g++
            }
        }
        return g
    }

    private fun evalOne(
        c: IntArray,
        kills: Set<Int>,
        dans: List<Int>,
        danMin: Int,
        vals: JSONObject,
        en: JSONObject,
        groups: List<Group>
    ): Boolean {
        // hard: kill
        for (x in c) if (x in kills) return false
        // hard: all dans must appear; optional danMin
        if (dans.isNotEmpty()) {
            var hit = 0
            for (d in dans) if (c.contains(d)) hit++
            if (hit < dans.size) return false
            if (danMin > 0 && hit < danMin) return false
        } else if (danMin > 0) {
            return false
        }

        var sum = 0
        var mn = 99
        var mx = 0
        var odd = 0
        var big = 0
        var pri = 0
        var edge = 0
        val z3 = IntArray(3)
        val z4 = IntArray(4)
        val rd = IntArray(3)
        val wx = IntArray(5)

        for (x in c) {
            sum += x
            if (x < mn) mn = x
            if (x > mx) mx = x
            if (x and 1 == 1) odd++
            if (x >= 17) big++
            if (x in 2..33 && PRIMES[x]) pri++
            if (EDGE[x]) edge++
            when {
                x <= 11 -> z3[0]++
                x <= 22 -> z3[1]++
                else -> z3[2]++
            }
            when {
                x <= 8 -> z4[0]++
                x <= 16 -> z4[1]++
                x <= 24 -> z4[2]++
                else -> z4[3]++
            }
            rd[x % 3]++
            wx[x % 5]++
        }
        val span = mx - mn

        var failSt = 0
        var failZn = 0
        var failRd = 0
        var failWx = 0
        var failEd = 0
        var failCu = 0

        if (on(en, "enSum") && !inRange(sum, v(vals, "sumMin", 70), v(vals, "sumMax", 130))) failSt++
        if (on(en, "enSpan") && !inRange(span, v(vals, "spanMin", 15), v(vals, "spanMax", 30))) failSt++
        if (on(en, "enOdd") && !inRange(odd, v(vals, "oddMin", 2), v(vals, "oddMax", 4))) failSt++
        if (on(en, "enBig") && !inRange(big, v(vals, "bigMin", 2), v(vals, "bigMax", 4))) failSt++
        if (on(en, "enPrime") && !inRange(pri, v(vals, "priMin", 1), v(vals, "priMax", 4))) failSt++
        if (on(en, "enAc") && !inRange(acValue(c), v(vals, "acMin", 6), v(vals, "acMax", 10))) failSt++
        if (on(en, "enCon") && !inRange(conGroups(c), v(vals, "conMin", 0), v(vals, "conMax", 2))) failSt++

        if (on(en, "enZ3")) {
            for (i in 0 until 3) {
                if (!inRange(z3[i], v(vals, "z3a$i", 1), v(vals, "z3b$i", 3))) failZn++
            }
        }
        if (on(en, "enZ4")) {
            for (i in 0 until 4) {
                if (!inRange(z4[i], v(vals, "z4a$i", 0), v(vals, "z4b$i", 3))) failZn++
            }
        }
        if (on(en, "enRoad")) {
            if (!inRange(rd[0], v(vals, "r0a", 1), v(vals, "r0b", 3))) failRd++
            if (!inRange(rd[1], v(vals, "r1a", 1), v(vals, "r1b", 3))) failRd++
            if (!inRange(rd[2], v(vals, "r2a", 1), v(vals, "r2b", 3))) failRd++
        }
        if (on(en, "enWx")) {
            for (i in 0 until 5) {
                if (!inRange(wx[i], v(vals, "wx${i}a", 0), v(vals, "wx${i}b", 3))) failWx++
            }
        }
        if (on(en, "enEdge") && !inRange(edge, v(vals, "edgeMin", 1), v(vals, "edgeMax", 4))) failEd++

        for (g in groups) {
            if (!g.on || g.nums.isEmpty()) continue
            var h = 0
            for (n in g.nums) if (c.contains(n)) h++
            if (!inRange(h, g.a, g.b)) failCu++
        }

        if (!inRange(failSt, v(vals, "tolStLo", 0), v(vals, "tolStHi", 2))) return false
        if (!inRange(failZn, v(vals, "tolZnLo", 0), v(vals, "tolZnHi", 1))) return false
        if (!inRange(failRd, v(vals, "tolRdLo", 0), v(vals, "tolRdHi", 1))) return false
        if (!inRange(failWx, v(vals, "tolWxLo", 0), v(vals, "tolWxHi", 1))) return false
        if (!inRange(failEd, v(vals, "tolEdLo", 0), v(vals, "tolEdHi", 1))) return false
        if (!inRange(failCu, v(vals, "tolCuLo", 0), v(vals, "tolCuHi", 1))) return false
        val total = failSt + failZn + failRd + failWx + failEd + failCu
        if (!inRange(total, v(vals, "tolAllLo", 0), v(vals, "tolAllHi", 2))) return false
        return true
    }
}
