package com.binwen.ssqfilter.model

data class RedPool(
    val kill: List<Int>,
    val dan: List<Int>,
    val candidates: List<Int>,
    val danRequired: Int = 0
) {

    fun build(): List<Int> {
        // 1. 候选池去掉杀号
        val base = candidates.filter { it !in kill }.toMutableList()

        // 2. 胆码必须加入池中
        for (d in dan) {
            if (d !in base) base.add(d)
        }

        // 3. 排序
        base.sort()

        return base
    }

    fun validate(): String? {
        // 胆码不能在杀号里
        if (dan.any { it in kill }) {
            return "胆码与杀号冲突"
        }

        // 胆码必须至少出现 danRequired 个
        if (dan.size < danRequired) {
            return "胆码数量不足（至少需要 $danRequired 个）"
        }

        // 红球池必须至少 6 个
        if (build().size < 6) {
            return "红球池数量不足（至少需要 6 个）"
        }

        return null
    }
}
