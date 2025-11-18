package com.lccm.nuvy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lccm.nuvy.network.NuvyHubApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CompilationState {
    object Idle : CompilationState()
    object Compiling : CompilationState()
    data class Success(val fileName: String) : CompilationState()
    data class Error(val message: String, val log: String? = null) : CompilationState()
}

class EditorViewModel(
    private val onDownloadFile: (ByteArray, String) -> Unit = { _, _ -> }
) : ViewModel() {
    private val apiService = NuvyHubApiService()

    private val _compilationState = MutableStateFlow<CompilationState>(CompilationState.Idle)
    val compilationState: StateFlow<CompilationState> = _compilationState

    private val _buildLog = MutableStateFlow("")
    val buildLog: StateFlow<String> = _buildLog

    fun compileCode(code: String, fileName: String = "main.c") {
        viewModelScope.launch {
            _compilationState.value = CompilationState.Compiling
            _buildLog.value = "🔨 Enviando código al servidor...\n"

            val result = apiService.compileCode(code, fileName)

            result.fold(
                onSuccess = { response ->
                    _buildLog.value += (response.log ?: "")

                    if (response.success && response.downloadUrl != null) {
                        _buildLog.value += "\n✅ Compilación exitosa\n📥 Descargando UF2...\n"
                        downloadAndSaveUF2(response.downloadUrl, response.jobId ?: "output")
                    } else {
                        _compilationState.value = CompilationState.Error(
                            response.error ?: "Error desconocido",
                            response.log
                        )
                        _buildLog.value += "\n❌ Error: ${response.error}\n"
                    }
                },
                onFailure = { error ->
                    _compilationState.value = CompilationState.Error(error.message ?: "Error desconocido")
                    _buildLog.value += "\n❌ ${error.message}\n"
                }
            )
        }
    }

    private suspend fun downloadAndSaveUF2(downloadUrl: String, jobId: String) {
        val result = apiService.downloadUF2(downloadUrl)

        result.fold(
            onSuccess = { uf2Data ->
                val fileName = "firmware_$jobId.uf2"
                // Descargar automáticamente
                onDownloadFile(uf2Data, fileName)
                _compilationState.value = CompilationState.Success(fileName)
                _buildLog.value += "✅ Archivo descargado: $fileName\n"
            },
            onFailure = { error ->
                _compilationState.value = CompilationState.Error("Error descargando: ${error.message}")
                _buildLog.value += "\n❌ ${error.message}\n"
            }
        )
    }

    fun resetState() {
        _compilationState.value = CompilationState.Idle
    }
}
