package com.ust.mytask.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ust.mytask.model.MdnsDevice
import com.ust.mytask.model.dao.MdnsDeviceDao

@Database(entities = [MdnsDevice::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mdnsDao(): MdnsDeviceDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "mdns_db"
                ).build().also { INSTANCE = it }
            }
    }
}
