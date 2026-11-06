package app.nivvi.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "readings")
data class HeartRateReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val heartRate: Int,
    val oxygen: Int? = null,
    val source: String = "Bluetooth"
)
