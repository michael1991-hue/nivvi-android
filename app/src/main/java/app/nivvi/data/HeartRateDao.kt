package app.nivvi.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartRateDao {
    @Insert
    suspend fun insert(reading: HeartRateReading)

    @Query("SELECT * FROM readings WHERE timestamp >= :from ORDER BY timestamp DESC")
    fun readingsFrom(from: Long): Flow<List<HeartRateReading>>

    @Query("SELECT * FROM readings ORDER BY timestamp DESC LIMIT 1")
    suspend fun latest(): HeartRateReading?

    @Query("DELETE FROM readings WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
