package com.lccm.nuvy

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lccm.nuvy.network.NuvyHubApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CompilationState {
    object Idle : CompilationState()
    object Compiling : CompilationState()
    data class Success(val message: String) : CompilationState()
    data class Error(val message: String) : CompilationState()
}

class EditorViewModel(
    private val context: Context,
    private val onDownloadFile: (ByteArray, String) -> Unit // Callback para guardar archivo
) : ViewModel() {

    // Instanciamos el servicio directamente
    private val apiService = NuvyHubApiService()

    private val _compilationState = MutableStateFlow<CompilationState>(CompilationState.Idle)
    val compilationState: StateFlow<CompilationState> = _compilationState

    private val _buildLog = MutableStateFlow("")
    val buildLog: StateFlow<String> = _buildLog

    private var currentFileType = "c"

    fun setFileType(type: String) {
        currentFileType = type
    }

    fun compileCode(code: String, currentFileName: String) {
        viewModelScope.launch {
            _compilationState.value = CompilationState.Compiling
            _buildLog.value = "🔄 Preparando compilación ($currentFileType)...\n"

            // Determinar nombre y extensión
            val extension = if (currentFileType == "ino") ".ino" else ".c"
            // Usamos el nombre actual pero forzamos la extensión correcta para el compilador
            val fileNameToSend = if (currentFileName.endsWith(extension)) currentFileName else "main$extension"

            // Llamada al servicio (solo pasamos strings)
            val result = apiService.compileCode(code, fileNameToSend)

            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _buildLog.value += "\n✅ Compilación exitosa!\nLogs:\n${response.log ?: ""}"
                        _compilationState.value = CompilationState.Success("Compilación exitosa")

                        // Si hay URL de descarga, descargar automáticamente
                        response.downloadUrl?.let { url ->
                            _buildLog.value += "\n📥 Descargando binario..."
                            downloadFirmware(url, response.jobId ?: "firmware")
                        }
                    } else {
                        _buildLog.value += "\n❌ Error del servidor:\n${response.error}\nLogs:\n${response.log}"
                        _compilationState.value = CompilationState.Error(response.error ?: "Error desconocido")
                    }
                },
                onFailure = { error ->
                    _buildLog.value += "\n❌ Error de red: ${error.message}"
                    _compilationState.value = CompilationState.Error(error.message ?: "Error de red")
                }
            )
        }
    }

    private fun downloadFirmware(url: String, jobId: String) {
        viewModelScope.launch {
            val result = apiService.downloadBIN(url)
            result.fold(
                onSuccess = { bytes ->
                    val finalName = "firmware_$jobId.bin"
                    onDownloadFile(bytes, finalName)
                    _buildLog.value += "\n💾 Guardado como $finalName"
                },
                onFailure = {
                    _buildLog.value += "\n❌ Error descargando BIN: ${it.message}"
                }
            )
        }
    }
}