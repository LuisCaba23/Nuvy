package com.lccm.nuvy

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    // Estado global del archivo actual
    private var currentFileName = mutableStateOf("Unnamed.c")
    private var currentCode = mutableStateOf("// Escribe tu código C aquí...")
    private var filesList = mutableStateOf<List<FileListItem>>(emptyList())

    private fun saveFileToDownloads(data: ByteArray, fileName: String) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { fos ->
                fos.write(data)
            }

            runOnUiThread {
                Toast.makeText(
                    this,
                    "✅ Archivo guardado en Descargas/$fileName",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "❌ Error al guardar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun saveCodeFile(fileName: String, code: String) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "$fileName.c")

            file.writeText(code)

            currentFileName.value = "$fileName.c"
            
            // Actualizar la lista de archivos
            loadFilesFromDownloads()

            runOnUiThread {
                Toast.makeText(
                    this,
                    "✅ Archivo guardado: $fileName.c",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "❌ Error al guardar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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
                    details = "Tamaño: ${file.length()} bytes • Modificado: ${java.text.SimpleDateFormat("dd/MM/yyyy").format(file.lastModified())}",
                    isFolder = false,
                    tag = ".c"
                )
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al cargar archivos: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFile(fileItem: FileListItem) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileItem.name)
            
            if (file.exists()) {
                currentCode.value = file.readText()
                currentFileName.value = fileItem.name
                
                Toast.makeText(
                    this,
                    "📂 Abriendo: ${fileItem.name}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir archivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cargar archivos al inicio
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

                NavHost(navController = navController, startDestination = "nuvy") {
                    composable("nuvy") {
                        // Pantalla temporal hasta que implementes NuvyScreen
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "🚧 En Desarrollo",
                                    style = MaterialTheme.typography.headlineMedium
                                )

                                Button(onClick = {
                                    navController.navigate("editor")
                                }) {
                                    Text("Ir al Editor")
                                }

                                Button(onClick = {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "🚧 Función de conexión en desarrollo",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }) {
                                    Text("Conectar (Próximamente)")
                                }
                            }
                        }
                    }
                    
                    composable("editor") {
                        EditorScreen(
                            currentFileName = currentFileName.value,
                            codeText = currentCode.value,
                            onCodeChange = { currentCode.value = it },
                            onNavigate = { route ->
                                when (route) {
                                    "home" -> navController.navigate("nuvy")
                                    "openFile" -> navController.navigate("openFile")
                                    "fileSaved" -> navController.navigate("fileSaved")
                                }
                            },
                            onOpenFile = { navController.navigate("openFile") },
                            onNewFile = { fileName, code ->
                                currentFileName.value = "$fileName.c"
                                currentCode.value = code
                                Toast.makeText(
                                    this@MainActivity,
                                    "📝 Trabajando en: $fileName.c",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            onSaveFile = { fileName ->
                                saveCodeFile(fileName, currentCode.value)
                                navController.navigate("fileSaved")
                            },
                            onCompile = {
                                viewModel.compileCode(currentCode.value, currentFileName.value)
                            },
                            onUpload = { /* TODO: Implementar subir a Pico */ },
                            viewModel = viewModel
                        )
                    }
                    
                    composable("openFile") {
                        OpenFileScreen(
                            onNavigate = { route ->
                                when (route) {
                                    NuvyDestinations.HOME -> navController.navigate("nuvy")
                                    NuvyDestinations.EDITOR -> navController.navigate("editor")
                                }
                            },
                            onOpen = { navController.navigate("editor") },
                            onFileClick = { fileItem ->
                                openFile(fileItem)
                                navController.navigate("editor")
                            },
                            files = filesList.value,
                            onFileImported = { uri ->
                                try {
                                    val inputStream = contentResolver.openInputStream(uri)
                                    val code = inputStream?.bufferedReader()?.readText() ?: ""
                                    currentCode.value = code
                                    currentFileName.value = uri.lastPathSegment ?: "Unnamed.c"
                                    navController.navigate("editor")
                                    Toast.makeText(this@MainActivity, "Archivo importado", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    
                    composable("fileSaved") {
                        FileSavedScreen(
                            onNavigate = { route ->
                                when (route) {
                                    NuvyDestinations.HOME -> navController.navigate("nuvy")
                                    NuvyDestinations.EDITOR -> navController.navigate("editor")
                                }
                            },
                            onGoBackToEditor = { navController.navigate("editor") },
                            onTryAgain = { navController.navigate("editor") },
                            fileName = currentFileName.value
                        )
                    }
                }
            }
        }
    }
}
