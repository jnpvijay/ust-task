package com.ust.mytask.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ust.mytask.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class DetailFragment : Fragment() {

    private var txtIPAddress: TextView? = null
    private var txtOrg: TextView? = null
    private var txtCity: TextView? = null
    private var txtRegion: TextView? = null
    private var txtCountry: TextView? = null
    private var txtPostal: TextView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.navigation_detail_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtIPAddress = view.findViewById(R.id.txtIpAddress)
        txtOrg = view.findViewById(R.id.txtOrg)
        txtCity = view.findViewById(R.id.txtCity)
        txtRegion = view.findViewById(R.id.txtRegion)
        txtCountry = view.findViewById(R.id.txtCountry)
        txtPostal = view.findViewById(R.id.txtPostal)


        // Start coroutine
        CoroutineScope(Dispatchers.Main).launch {
            val ip = getPublicIP()
            if (ip != null) {
                val geoInfo = getGeoInfo(ip)
                println("Geo Info: $geoInfo")

                txtIPAddress?.text = "$ip"
                txtOrg?.text = "${geoInfo?.optString("org")}"
                txtCity?.text = "${geoInfo?.optString("city")}"
                txtRegion?.text = "${geoInfo?.optString("region")}"
                txtCountry?.text = "${geoInfo?.optString("country")}"
                txtPostal?.text = "${geoInfo?.optString("postal")}"

                println("Geo Info: $geoInfo")
            }
        }
    }


    // Suspend function to get public IP
    private suspend fun getPublicIP(): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.ipify.org?format=json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String? = reader.readLine()
                while (line != null) {
                    response.append(line)
                    line = reader.readLine()
                }
                reader.close()
                connection.disconnect()
                return@withContext JSONObject(response.toString()).getString("ip")
            } else {
                println("Error getting IP: $responseCode")
                connection.disconnect()
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    // Suspend function to get geo info for the IP
    private suspend fun getGeoInfo(ip: String): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://ipinfo.io/$ip/geo")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String? = reader.readLine()
                while (line != null) {
                    response.append(line)
                    line = reader.readLine()
                }
                reader.close()
                connection.disconnect()
                return@withContext JSONObject(response.toString())
            } else {
                println("Error getting Geo info: $responseCode")
                connection.disconnect()
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
