package com.ust.mytask.ui.home

import android.content.Context
import android.net.wifi.WifiManager
import com.ust.mytask.model.MdnsDevice
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

class MdnsScanner(
    private val context: Context,
    private val onDeviceFound: (MdnsDevice) -> Unit
) {

    private var jmdns: JmDNS? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    fun startScanning() {
        Thread {
            try {
                val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

                multicastLock = wifiManager.createMulticastLock("mdns").apply {
                    setReferenceCounted(true)
                    acquire()
                }

                val ip = wifiManager.connectionInfo.ipAddress
                val inet = InetAddress.getByAddress(
                    ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ip).array()
                )

                jmdns = JmDNS.create(inet)

               jmdns?.addServiceListener("_services._dns-sd._udp.local.", listener)

                val device = MdnsDevice(
                    name = inet.hostName,
                    ip = inet.hostAddress,
                    port = 8080,
                    type = "My Device"
                )

                onDeviceFound(device) // 🚀 Send to ViewModel


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private val listener = object : ServiceListener {
        override fun serviceAdded(event: ServiceEvent) {
            jmdns?.requestServiceInfo(event.type, event.name)
        }

        override fun serviceRemoved(event: ServiceEvent) {}

        override fun serviceResolved(event: ServiceEvent) {
            val info = event.info
            val device = MdnsDevice(
                name = info.name,
                ip = info.inet4Addresses.firstOrNull()?.hostAddress,
                port = info.port,
                type = info.type
            )

            onDeviceFound(device) // 🚀 Send to ViewModel
        }
    }

    fun stopScanning() {
        jmdns?.close()
        multicastLock?.release()
    }
}
