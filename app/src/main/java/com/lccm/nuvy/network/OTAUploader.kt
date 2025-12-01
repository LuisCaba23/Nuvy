package com.lccm.nuvy.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

// CAMBIO: Ahora recibe 'context' en el constructor
class OTAUploader(private val context: Context) {
    companion object {
        private const val TAG = "OTAUploader"
        private const val ESP32_IP = "192.168.4.1"
        private const val UPLOAD_URL = "http://$ESP32_IP/update"
        private const val TIMEOUT_SECONDS = 60L
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    // --- MAGIA AQUÍ: Forzar uso de WiFi ---
    private fun bindToWifiNetwork(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Buscar entre todas las redes una que sea WIFI
        val wifiNetwork: Network? = connectivityManager.allNetworks.firstOrNull { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }

        return if (wifiNetwork != null) {
            Log.d(TAG, "Forzando tráfico por la red WiFi encontrada")
            // Esto le dice a Android: "Todo lo que haga esta app, mándalo por este WiFi"
            connectivityManager.bindProcessToNetwork(wifiNetwork)
            true
        } else {
            Log.e(TAG, "No se encontró ninguna red WiFi conectada")
            false
        }
    }

    // Es buena práctica liberar la red cuando termines, pero para esta pantalla está bien así.
    fun unbindNetwork() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.bindProcessToNetwork(null)
    }

    suspend fun uploadFirmware(
        binFile: File,
        onProgress: (Int) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        // Paso 1: Asegurar que usamos WiFi
        if (!bindToWifiNetwork()) {
            return@withContext Result.failure(IOException("No estás conectado a WiFi"))
        }

        try {
            Log.d(TAG, "Iniciando upload a $UPLOAD_URL")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "firmware",
                    binFile.name,
                    binFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: "Sin respuesta"
                if (response.isSuccessful) {
                    Result.success("✅ Firmware subido. ESP32 reiniciándose...")
                } else {
                    Result.failure(IOException("Error ${response.code}: $responseBody"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error upload", e)
            Result.failure(e)
        }
    }

    fun checkConnection(): Boolean {
        // Paso 1: Asegurar que usamos WiFi antes de chequear
        if (!bindToWifiNetwork()) {
            Log.e(TAG, "Check fallido: No hay WiFi conectado")
            return false
        }

        return try {
            val request = Request.Builder()
                .url("http://$ESP32_IP/") // Intentamos conectar a la raíz
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                // Cualquier respuesta (incluso 404) significa que el dispositivo está ahí
                Log.d(TAG, "Check conexión: Código ${response.code}")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "No se puede conectar al ESP32 (Timeout o error)", e)
            false
        }
    }
}