package dev.pulsereport.core.healthconnect

import java.time.LocalDate

/**
 * This cheeky little function resolves, per day, which data origin's records win when a
 * metric has more than one writer.
 * The effective ranking is [priority] followed by any other origins present in
 * [itemsByDayThenOrigin] (alphabetical, for determinism); the first origin in that
 * ranking with a non-empty list for a given day supplies that day's data  so we never get a blend
 * of two origins' records for the same day, even when [priority] is empty (which can happen if the
 * user hasn't opened the Sources screen yet). That keeps a multi-source day from silently
 * double-counting and causing a fair bit of confusion, also lets a day with no data from the top-priority origin
 * (e.g. the user's main device wasn't worn that day) fall through to the next one instead of
 * going empty.
 */
fun <T> selectByPriority(
    itemsByDayThenOrigin: Map<LocalDate, Map<String, List<T>>>,
    priority: List<String>,
): Map<LocalDate, List<T>> {
    val priorityRank = priority.withIndex().associate { (index, packageName) -> packageName to index }
    return itemsByDayThenOrigin.mapValues { (_, byOrigin) ->
        val unlisted = (byOrigin.keys - priorityRank.keys).sorted()
        val effectiveOrder = priority + unlisted
        effectiveOrder.firstNotNullOfOrNull { origin -> byOrigin[origin]?.takeIf { it.isNotEmpty() } } ?: emptyList()
    }
}
