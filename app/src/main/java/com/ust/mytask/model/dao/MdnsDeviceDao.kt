package com.ust.mytask.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ust.mytask.model.MdnsDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface MdnsDeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: MdnsDevice)

    @Query("SELECT * FROM mdns_devices ORDER BY name ASC")
    fun getAllDevices(): Flow<List<MdnsDevice>>
}
