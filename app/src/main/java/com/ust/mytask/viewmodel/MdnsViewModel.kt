package com.ust.mytask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ust.mytask.model.MdnsDevice
import com.ust.mytask.model.repository.MdnsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MdnsViewModel(private val repo: MdnsRepository) : ViewModel() {

    val devices: Flow<List<MdnsDevice>> = repo.devices

    fun addDevice(device: MdnsDevice) {
        viewModelScope.launch {
            repo.saveDevice(device)
        }
    }
}
