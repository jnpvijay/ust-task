package com.ust.mytask.model.repository

import com.ust.mytask.model.MdnsDevice
import com.ust.mytask.model.dao.MdnsDeviceDao

class MdnsRepository(private val dao: MdnsDeviceDao) {

    val devices = dao.getAllDevices()

    suspend fun saveDevice(device: MdnsDevice) {
        dao.insertDevice(device)
    }
}
