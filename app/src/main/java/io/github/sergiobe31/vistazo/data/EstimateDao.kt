package io.github.sergiobe31.vistazo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EstimateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(estimate: DailyEstimate)

    @Query("SELECT * FROM daily_estimates WHERE dayStartMillis = :start")
    fun observeForDay(start: Long): Flow<DailyEstimate?>
}
