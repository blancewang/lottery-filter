package com.binwen.ssqfilter.engine

import com.binwen.ssqfilter.model.Pools
import com.binwen.ssqfilter.model.RedPool
import com.binwen.ssqfilter.model.Ticket

class Enumerator {

    fun enumerate(redPool: RedPool, pools: Pools): Sequence<Ticket> {

        // 1. 验证红球池是否合法
        val error = redPool.validate()
        if (error != null) {
            return emptySequence()   // 返回空序列，避免崩溃
        }

        // 2. 构建红球池
        val reds = redPool.build()
        val blues = pools.blue

        return sequence {
            val n = reds.size
            if (n < 6) return@sequence

            val indices = IntArray(6)

            fun dfs(depth: Int, start: Int) {
                if (depth == 6) {
                    val ticketReds = listOf(
                        reds[indices[0]],
                        reds[indices[1]],
                        reds[indices[2]],
                        reds[indices[3]],
                        reds[indices[4]],
                        reds[indices[5]]
                    )
                    for (b in blues) {
                        yield(Ticket(ticketReds, b))
                    }
                    return
                }
                for (i in start until n) {
                    indices[depth] = i
                    dfs(depth + 1, i + 1)
                }
            }

            dfs(0, 0)
        }
    }
}
