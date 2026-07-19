package dev.pulsereport.core.database

import androidx.room.Entity

@Entity(tableName = "source_priorities", primaryKeys = ["metric", "packageName"])
data class SourcePriorityEntity(
    /** [dev.pulsereport.core.model.HealthMetric] name. */
    val metric: String,
    val packageName: String,
    /** Rank within [metric], lowest wins. */
    val position: Int,
)
