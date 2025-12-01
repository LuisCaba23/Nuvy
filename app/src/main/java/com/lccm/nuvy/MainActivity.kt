package com.lccm.nuvy

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lccm.nuvy.ui.theme.NuvyTheme
import java.io.File
import java.io.FileOutputStream

// Asegúrate de que FileListItem esté definido en algún lugar o descomenta esto:
// data class FileListItem(val name: String, val details: String, val isFolder: Boolean, val tag: String)

class MainActivity : ComponentActivity() {

    private var currentFileName = mutableStateOf("main.c")
    private var currentCode = mutableStateOf(getDefaultWiFiCode())
    private var filesList = mutableStateOf<List<FileListItem>>(emptyList())

    private fun saveFileToDownloads(data: ByteArray, fileName: String) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { it.write(data) }
            runOnUiThread {
                Toast.makeText(this, "✅ Guardado: $fileName", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "❌ Error guardando: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ... (Tus funciones saveCodeFile, loadFilesFromDownloads, openFile se quedan igual) ...
    // Solo las omito aquí para ahorrar espacio, pero NO LAS BORRES.
    private fun saveCodeFile(fileName: String, code: String) { /* Tu código aquí */ }
    private fun loadFilesFromDownloads() { /* Tu código aquí */ }
    private fun openFile(fileItem: FileListItem) { /* Tu código aquí */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadFilesFromDownloads()

        setContent {
            NuvyTheme {
                val navController = rememberNavController()

                // FACTORY CORREGIDA
                val viewModel: EditorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return EditorViewModel(
                                context = applicationContext,
                                onDownloadFile = { data, name -> saveFileToDownloads(data, name) }
                            ) as T
                        }
                    }
                )

                NavHost(navController = navController, startDestination = NuvyDestinations.HOME) {
                    composable(NuvyDestinations.HOME) {
                        HomeScreen(onNavigate = { route -> navController.navigate(route) })
                    }

                    composable(NuvyDestinations.CONNECT) {
                        ConnectScreen(onNavigate = { route -> navController.navigate(route) })
                    }

                    composable(NuvyDestinations.EDITOR) {
                        EditorScreen(
                            currentFileName = currentFileName.value,
                            codeText = currentCode.value,
                            onCodeChange = { currentCode.value = it },
                            onNavigate = { route -> navController.navigate(route) },
                            onOpenFile = { navController.navigate(NuvyDestinations.OPEN_FILE) },
                            onNewFile = { fileName, code ->
                                currentFileName.value = fileName
                                currentCode.value = code
                            },
                            onSaveFile = { fileName -> saveCodeFile(fileName, currentCode.value) },
                            onCompile = { viewModel.compileCode(currentCode.value, currentFileName.value) },
                            onUpload = { navController.navigate(NuvyDestinations.CONNECT) },
                            viewModel = viewModel
                        )
                    }

                    composable(NuvyDestinations.OPEN_FILE) {
                        // Asumo que tienes esta pantalla definida en otro archivo
                        // Si no, avísame.
                        OpenFileScreen(
                            onNavigate = { navController.navigate(it) },
                            onOpen = { navController.navigate(NuvyDestinations.EDITOR) },
                            onFileClick = { file ->
                                // Lógica simple para abrir
                                currentFileName.value = file.name
                                navController.navigate(NuvyDestinations.EDITOR)
                            },
                            files = filesList.value,
                            onFileImported = { /* Lógica import */ }
                        )
                    }
                }
            }
        }
    }
}