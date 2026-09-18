package com.binwen.ssqfilter.engine

import com.binwen.ssqfilter.model.Pools
import com.binwen.ssqfilter.model.Ticket

class Enumerator {

    fun enumerate(pools: Pools): Sequence<Ticket> {
        val reds = pools.red
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
