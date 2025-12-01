package com.lccm.nuvy.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

data class BuildResponse(
    val success: Boolean,
    val downloadUrl: String? = null,
    val error: String? = null,
    val log: String? = null,
    val jobId: String? = null
)

class NuvyHubApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://nuvyhub.online"

    suspend fun compileCode(codeContent: String, fileName: String = "main.c"): Result<BuildResponse> =
        withContext(Dispatchers.IO) {
            try {
                // Crear archivo temporal con el código
                val tempFile = File.createTempFile("code_", ".c")
                tempFile.writeText(codeContent)

                // Crear FormData con el archivo
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "code",
                        fileName,
                        tempFile.asRequestBody("text/x-c".toMediaType())
                    )
                    .build()

                val request = Request.Builder()
                    .url("$baseUrl/build")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                // Limpiar archivo temporal
                tempFile.delete()

                if (response.isSuccessful) {
                    // Parsear respuesta JSON manualmente
                    val success = responseBody.contains("\"success\":true")
                    val downloadUrl = extractJsonValue(responseBody, "downloadUrl")
                    val log = extractJsonValue(responseBody, "log")
                    val jobId = extractJsonValue(responseBody, "jobId")

                    Result.success(
                        BuildResponse(
                            success = success,
                            downloadUrl = downloadUrl,
                            log = log,
                            jobId = jobId
                        )
                    )
                } else {
                    val error = extractJsonValue(responseBody, "error") ?: "Error desconocido"
                    Result.success(
                        BuildResponse(
                            success = false,
                            error = error,
                            log = extractJsonValue(responseBody, "log")
                        )
                    )
                }
            } catch (e: IOException) {
                Result.failure(Exception("Error de red: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(Exception("Error inesperado: ${e.message}"))
            }
        }

    suspend fun downloadBIN(downloadUrl: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl$downloadUrl")
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val bytes = response.body?.bytes() ?: throw Exception("Respuesta vacía")
                Result.success(bytes)
            } else {
                Result.failure(Exception("Error al descargar: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error descargando BIN: ${e.message}"))
        }
    }

    private fun extractJsonValue(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]*)\""
        val regex = Regex(pattern)
        return regex.find(json)?.groupValues?.get(1)
    }
}
