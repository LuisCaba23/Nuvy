package com.lccm.nuvy

import android.os.Bundle
import android.widget.Toast
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lccm.nuvy.ui.theme.NuvyTheme
import java.io.File
import java.io.FileOutputStream

// --- "BASE DE DATOS" DE ARCHIVOS (Definida una vez) ---
val allFiles = listOf(
    FileListItem(name = "blinky.c", details = "Modificado hace 2 días • 1.2 KB • Local"),
    FileListItem(name = "wifi_setup.c", details = "Modificado hoy • 3.8 KB • Nube"),
    FileListItem(name = "pwm_driver.c", details = "Modificado hace 5 h • 6.1 KB • Local"),
    FileListItem(name = "/Proyectos/pico/", details = "12 archivos • Local", isFolder = true, tag = null),
    FileListItem(name = "/Nuvy Cloud/", details = "8 archivos • Nube", isFolder = true, tag = null)
)

// --- "BASE DE DATOS" DE CONTENIDO (Mutable) ---
var fileContentDatabase = mapOf(
    "blinky.c" to """
    // Archivo: blinky.c
    #include "pico/stdlib.h"
    
    int main() {
      const uint LED_PIN = 25;
      gpio_init(LED_PIN);
      gpio_set_dir(LED_PIN, GPIO_OUT);
      while (true) {
        gpio_put(LED_PIN, 1);
        sleep_ms(250);
        gpio_put(LED_PIN, 0);
        sleep_ms(250);
      }
    }
    """.trimIndent(),
    "wifi_setup.c" to """
    // Archivo: wifi_setup.c
    #include "pico/stdlib.h"
    
    int main() {
      // Lógica de WiFi...
      printf("WiFi conectado!\n");
    }
    """.trimIndent(),
    "pwm_driver.c" to """
    // Archivo: pwm_driver.c
    #include "pico/stdlib.h"
    
    int main() {
      // Lógica de PWM...
    }
    """.trimIndent()
)

class MainActivity : ComponentActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NuvyTheme {
                // --- ESTADOS ---
                var currentScreen by remember { mutableStateOf(NuvyDestinations.HOME) }
                var currentFileName by remember { mutableStateOf("main.c") }
                var editorCode by remember { mutableStateOf("// Escribe tu código C aquí...") }
                var showNewFileDialog by remember { mutableStateOf(false) }
                var fileList by remember { mutableStateOf(allFiles) }

                val navigateTo: (String) -> Unit = { screen ->
                    currentScreen = screen
                }

                when (currentScreen) {

                    NuvyDestinations.HOME, NuvyDestinations.CONNECT -> {
                        NuvyScreen(
                            onConnectClicked = { navigateTo(NuvyDestinations.CONNECT_DEVICE) },
                            onEditorClicked = { navigateTo(NuvyDestinations.EDITOR) }
                        )
                    }
                )

                    NuvyDestinations.EDITOR -> {
                        EditorScreen(
                            currentFileName = currentFileName,
                            codeText = editorCode,
                            onCodeChange = { editorCode = it },
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onOpenFile = { navigateTo(NuvyDestinations.OPEN_FILE) },
                            onSaveSuccess = {
                                fileContentDatabase = fileContentDatabase.toMutableMap().apply {
                                    set(currentFileName, editorCode)
                                }
                                navigateTo(NuvyDestinations.FILE_SAVED)
                            },
                            onCompile = { navigateTo(NuvyDestinations.BUILD_PROCESS) },
                            onUpload = { navigateTo(NuvyDestinations.OPEN_FILE) },
                            onNewFile = { showNewFileDialog = true }
                        )

                        if (showNewFileDialog) {
                            NewFileDialog(
                                onDismiss = { showNewFileDialog = false },
                                onCreate = { newName, newContent ->
                                    val fullFileName = "$newName.c"
                                    currentFileName = fullFileName
                                    editorCode = newContent

                                    fileContentDatabase = fileContentDatabase.toMutableMap().apply {
                                        set(fullFileName, newContent)
                                    }
                                    fileList = listOf(FileListItem(fullFileName, "Creado ahora • Local", false)) + fileList

                                    showNewFileDialog = false
                                }
                            )
                        }
                    }

                    NuvyDestinations.OPEN_FILE -> {
                        OpenFileScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onOpen = { /*TODO*/ },
                            files = fileList,
                            onFileImported = { uri ->

                                var importedFileName = "archivo.c"
                                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                                    if (cursor.moveToFirst()) {
                                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                        if (nameIndex != -1) {
                                            importedFileName = cursor.getString(nameIndex)
                                        }
                                    }
                                }

                                val newFile = FileListItem(
                                    name = importedFileName,
                                    details = "Importado hoy • Local",
                                    isFolder = false
                                )

                                fileList = listOf(newFile) + fileList

                                Toast.makeText(this, "$importedFileName importado", Toast.LENGTH_SHORT).show()
                            },
                            onFileClick = { fileItem ->
                                currentFileName = fileItem.name
                                editorCode = fileContentDatabase[fileItem.name] ?: "// Contenido no encontrado"
                                navigateTo(NuvyDestinations.FILE_PREVIEW)
                            }
                        )
                    }

                    NuvyDestinations.FILE_PREVIEW -> {
                        FilePreviewScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            fileName = currentFileName,
                            fileContent = editorCode,
                            onEdit = { navigateTo(NuvyDestinations.EDITOR) },
                            onCancel = { navigateTo(NuvyDestinations.OPEN_FILE) }
                        )
                    }

                    NuvyDestinations.CONNECT_DEVICE -> {
                        ConnectDeviceScreen(
                            onPermitAccess = { navigateTo(NuvyDestinations.ACCESS_GRANTED) },
                            onTryAgain = {},
                            onNavigate = { screenName -> navigateTo(screenName) }
                        )
                    }
                    NuvyDestinations.ACCESS_GRANTED -> {
                        AccessGrantedScreen(
                            onContinueClicked = { navigateTo(NuvyDestinations.UPLOAD_PROGRESS) },
                            onNavigate = { screenName -> navigateTo(screenName) }
                        )
                    }
                    NuvyDestinations.FILE_SAVED -> {
                        FileSavedScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onGoBackToEditor = { navigateTo(NuvyDestinations.EDITOR) },
                            onTryAgain = { /*TODO*/ },
                            fileName = currentFileName
                        )
                    }
                    NuvyDestinations.BUILD_PROCESS -> {
                        BuildScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onGoBack = { navigateTo(NuvyDestinations.EDITOR) },
                            onCancelBuild = { navigateTo(NuvyDestinations.EDITOR) },
                            onBuildComplete = { navigateTo(NuvyDestinations.DOWNLOAD_READY) }
                        )
                    }
                    NuvyDestinations.DOWNLOAD_READY -> {
                        DownloadReadyScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onGoBack = { navigateTo(NuvyDestinations.EDITOR) },
                            onUpload = { navigateTo(NuvyDestinations.CONNECT_DEVICE) },
                            onSave = { /*TODO: Lógica de guardado*/ }
                        )
                    }

                    // --- AQUÍ ESTABA EL ERROR (TEXTO EXTRA ELIMINADO) ---

                    NuvyDestinations.UPLOAD_PROGRESS -> {
                        UploadScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onGoBack = { navigateTo(NuvyDestinations.EDITOR) },
                            onCancelUpload = { navigateTo(NuvyDestinations.EDITOR) },
                            onUploadFinished = { navigateTo(NuvyDestinations.UPLOAD_COMPLETE) }
                        )
                    }
                    NuvyDestinations.UPLOAD_COMPLETE -> {
                        UploadCompleteScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onGoBack = { navigateTo(NuvyDestinations.EDITOR) },
                            onFinalize = { navigateTo(NuvyDestinations.HOME) }
                        )
                    }
                }
            }
        }
    }
}

// =================================================================================
// PANTALLA 1: NuvyScreen (Esta parte no cambia)
// =================================================================================
@Composable
fun NuvyScreen(
    onConnectClicked: () -> Unit,
    onEditorClicked: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Nuvy", fontSize = 48.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Edita un archivo .c, súbelo a la nube y recibirás un archivo .uf2.",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onConnectClicked,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = "Conectar por cable", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onEditorClicked,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = "Ir al editor", fontSize = 16.sp)
            }
        }
    }
}

// --- Vista Previa (No cambia) ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NuvyScreenPreview() {
    NuvyTheme {
        NuvyScreen(
            onConnectClicked = {},
            onEditorClicked = {}
        )
    }
}
