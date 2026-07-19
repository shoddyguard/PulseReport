package dev.pulsereport.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SourcePriorityDao {
    @Query("SELECT * FROM source_priorities ORDER BY metric, position")
    fun observeAll(): Flow<List<SourcePriorityEntity>>

    @Query("DELETE FROM source_priorities WHERE metric = :metric")
    suspend fun deleteForMetric(metric: String)

    @Insert
    suspend fun insertAll(entities: List<SourcePriorityEntity>)

    @Transaction
    suspend fun replaceForMetric(metric: String, orderedPackageNames: List<String>) {
        deleteForMetric(metric)
        insertAll(
            orderedPackageNames.mapIndexed { index, packageName ->
                SourcePriorityEntity(metric = metric, packageName = packageName, position = index)
            },
        )
    }
}
