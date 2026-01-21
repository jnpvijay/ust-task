package com.ust.mytask.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mdns_devices")
data class MdnsDevice(
    @PrimaryKey val name: String,
    val ip: String?,
    val port: Int,
    val type: String
)