package com.lccm.nuvy

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lccm.nuvy.ui.theme.NuvyTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private var currentFileName = mutableStateOf("Unnamed.c")
    private var currentCode = mutableStateOf("// Escribe tu código C aquí...")
    private var filesList = mutableStateOf<List<FileListItem>>(emptyList())

    private fun saveFileToDownloads(data: ByteArray, fileName: String) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { it.write(data) }
            runOnUiThread {
                Toast.makeText(this, "✅ Guardado en Descargas/$fileName", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveCodeFile(fileName: String, code: String) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "$fileName.c")
            file.writeText(code)
            currentFileName.value = "$fileName.c"
            loadFilesFromDownloads()
            runOnUiThread {
                Toast.makeText(this, "✅ Guardado: $fileName.c", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFilesFromDownloads() {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val files = downloadsDir.listFiles()?.filter { it.extension == "c" } ?: emptyList()
            filesList.value = files.map { file ->
                FileListItem(
                    name = file.name,
                    details = "${file.length()} bytes",
                    isFolder = false,
                    tag = ".c"
                )
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFile(fileItem: FileListItem) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileItem.name)
            if (file.exists()) {
                currentCode.value = file.readText()
                currentFileName.value = fileItem.name
                Toast.makeText(this, "📂 Abriendo: ${fileItem.name}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadFilesFromDownloads()

        setContent {
            NuvyTheme {
                val navController = rememberNavController()
                val viewModel: EditorViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return EditorViewModel(
                                onDownloadFile = { data, fileName ->
                                    saveFileToDownloads(data, fileName)
                                }
                            ) as T
                        }
                    }
                )

                NavHost(navController = navController, startDestination = NuvyDestinations.HOME) {
                    composable(NuvyDestinations.HOME) {
                        HomeScreen(onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        })
                    }

                    composable(NuvyDestinations.CONNECT) {
                        ConnectScreen(onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        })
                    }

                    composable(NuvyDestinations.EDITOR) {
                        EditorScreen(
                            currentFileName = currentFileName.value,
                            codeText = currentCode.value,
                            onCodeChange = { currentCode.value = it },
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpenFile = { navController.navigate(NuvyDestinations.OPEN_FILE) },
                            onNewFile = { fileName, code ->
                                currentFileName.value = "$fileName.c"
                                currentCode.value = code
                            },
                            onSaveFile = { fileName ->
                                saveCodeFile(fileName, currentCode.value)
                            },
                            onCompile = { viewModel.compileCode(currentCode.value, currentFileName.value) },
                            onUpload = { /* TODO: OTA */ },
                            viewModel = viewModel
                        )
                    }

                    composable(NuvyDestinations.OPEN_FILE) {
                        OpenFileScreen(
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpen = { navController.navigate(NuvyDestinations.EDITOR) },
                            onFileClick = { fileItem ->
                                openFile(fileItem)
                                navController.navigate(NuvyDestinations.EDITOR)
                            },
                            files = filesList.value,
                            onFileImported = { uri ->
                                try {
                                    val code = contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
                                    currentCode.value = code
                                    currentFileName.value = uri.lastPathSegment ?: "Unnamed.c"
                                    navController.navigate(NuvyDestinations.EDITOR)
                                } catch (e: Exception) {
                                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}