package com.lccm.nuvy

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lccm.nuvy.components.NuvyBottomNavBar
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    currentFileName: String,
    codeText: String,
    onCodeChange: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenFile: () -> Unit, // Se ignora, usamos el launcher interno
    onNewFile: (String, String) -> Unit,
    onSaveFile: (String) -> Unit,
    onCompile: () -> Unit,
    onUpload: () -> Unit,
    viewModel: EditorViewModel
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var selectedFileType by remember { mutableStateOf("c") }

    val context = LocalContext.current

    // --- LÓGICA DE ABRIR ARCHIVO (Explorador Nativo) ---
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // 1. Obtener nombre real del archivo
                var filename = "archivo_importado.c"
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            filename = it.getString(nameIndex)
                        }
                    }
                }

                // 2. Leer contenido
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = StringBuilder()
                var line = reader.readLine()
                while (line != null) {
                    content.append(line).append("\n")
                    line = reader.readLine()
                }
                inputStream?.close()

                // 3. Actualizar editor
                onNewFile(filename, content.toString())
                Toast.makeText(context, "📂 Abierto: $filename", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(context, "Error al abrir archivo", Toast.LENGTH_LONG).show()
            }
        }
    }

    val compilationState by viewModel.compilationState.collectAsState()
    val buildLog by viewModel.buildLog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(compilationState) {
        if (compilationState is CompilationState.Success) {
            snackbarHostState.showSnackbar("✅ Compilación exitosa")
        } else if (compilationState is CompilationState.Error) {
            val msg = (compilationState as CompilationState.Error).message
            snackbarHostState.showSnackbar("❌ $msg")
        }
    }

    // --- DIÁLOGOS MODALES ---

    if (showNewFileDialog) {
        NewFileDialog(
            onDismiss = { showNewFileDialog = false },
            onCreate = { fileName, code ->
                onNewFile(fileName, code)
                showNewFileDialog = false
            }
        )
    }

    if (showSaveDialog) {
        SaveFileDialog(
            currentFileName = currentFileName,
            onDismiss = { showSaveDialog = false },
            onSave = { fileName ->
                onSaveFile(fileName)
                showSaveDialog = false
            }
        )
    }

    // --- UI PRINCIPAL ---

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Editor") }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = { NuvyBottomNavBar(NuvyDestinations.EDITOR, onNavigate) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Código") })
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Logs") })
            }

            when (selectedTabIndex) {
                0 -> CodeEditorTab(
                    codeText = codeText,
                    onCodeChange = onCodeChange,
                    currentFileName = currentFileName,
                    onNewFilePress = { showNewFileDialog = true },
                    onOpenFilePress = { filePickerLauncher.launch("*/*") }, // Lanza explorador
                    onSaveFilePress = { showSaveDialog = true },
                    onCompile = {
                        viewModel.setFileType(selectedFileType)
                        onCompile()
                        selectedTabIndex = 1
                    },
                    compilationState = compilationState,
                    selectedFileType = selectedFileType,
                    onFileTypeChange = { selectedFileType = it }
                )
                1 -> CompileTab(
                    buildLog = buildLog,
                    compilationState = compilationState,
                    onNavigateToConnect = { onNavigate(NuvyDestinations.CONNECT) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CodeEditorTab(
    codeText: String,
    onCodeChange: (String) -> Unit,
    currentFileName: String,
    onNewFilePress: () -> Unit,
    onOpenFilePress: () -> Unit,
    onSaveFilePress: () -> Unit,
    onCompile: () -> Unit,
    compilationState: CompilationState,
    selectedFileType: String,
    onFileTypeChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("📄 $currentFileName", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = codeText,
            onValueChange = onCodeChange,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
            placeholder = { Text("// Tu código aquí...") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de archivo
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Lenguaje:", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedFileType == "c",
                        onClick = { onFileTypeChange("c") },
                        label = { Text("C / IDF") }
                    )
                    FilterChip(
                        selected = selectedFileType == "ino",
                        onClick = { onFileTypeChange("ino") },
                        label = { Text("Arduino") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCompile,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = compilationState !is CompilationState.Compiling
        ) {
            if (compilationState is CompilationState.Compiling) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compilando...")
            } else {
                Text("COMPILAR")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- BOTONES (Nuevo | Abrir | Guardar) ---
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onNewFilePress, modifier = Modifier.weight(1f)) {
                Text("Nuevo")
            }
            OutlinedButton(onClick = onOpenFilePress, modifier = Modifier.weight(1f)) {
                Text("Abrir")
            }
            Button(onClick = onSaveFilePress, modifier = Modifier.weight(1f)) {
                Text("Guardar")
            }
        }
    }
}

// --- DIÁLOGOS (Lógica corregida) ---

@Composable
fun NewFileDialog(onDismiss: () -> Unit, onCreate: (String, String) -> Unit) {
    var fileName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Archivo") },
        text = {
            Column {
                Text("Nombre del archivo (sin extensión):")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Ej: main") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if(fileName.isNotEmpty()) {
                    onCreate(fileName, getDefaultWiFiCode())
                }
            }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun SaveFileDialog(currentFileName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    // Limpiamos la extensión visualmente para evitar "archivo.c.c"
    val cleanName = remember(currentFileName) {
        currentFileName.removeSuffix(".c").removeSuffix(".ino")
    }

    // Usamos remember con key para que se actualice si cambia el archivo
    var fileName by remember(currentFileName) { mutableStateOf(cleanName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Guardar Archivo") },
        text = {
            Column {
                Text("Nombre para guardar (se añadirá .c/.ino):")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if(fileName.isNotEmpty()) onSave(fileName)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// CompileTab (Sin cambios)
@Composable
private fun CompileTab(
    buildLog: String,
    compilationState: CompilationState,
    onNavigateToConnect: () -> Unit
) {
    // ... (Tu código de CompileTab existente)
    // Para no alargar la respuesta, copia aquí tu función CompileTab idéntica
    // Si no la tienes a la mano, te la dejo simplificada aquí:

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF222222))
        ) {
            // Necesitas importar foundation.verticalScroll y rememberScrollState
            Column(modifier = Modifier.padding(12.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                Text(text = buildLog, color = Color.Green, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateToConnect,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Link, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ir a Conexión (OTA/USB)")
        }
    }
}

fun getDefaultWiFiCode(): String {
    return """
#include <WiFi.h>
#include <WebServer.h>
#include <Update.h>

const char* ssid = "ESP32_SETUP";
const char* password = "12345678";

WebServer server(80);

void handleUpdateUpload() {
  HTTPUpload& upload = server.upload();

  if (upload.status == UPLOAD_FILE_START) {
    Serial.printf("Subiendo: %s\n", upload.filename.c_str());
    if (!Update.begin()) { 
      Update.printError(Serial);
    }
  } 
  else if (upload.status == UPLOAD_FILE_WRITE) {
    if (Update.write(upload.buf, upload.currentSize) != upload.currentSize) {
      Update.printError(Serial);
    }
  } 
  else if (upload.status == UPLOAD_FILE_END) {
    if (Update.end(true)) {
      Serial.printf("Actualización completa! Reiniciando...\n");
    } else {
      Update.printError(Serial);
    }
  }
}

void handleUpdatePage() {
  server.sendHeader("Connection", "close");
  server.send(200, "text/html",
    "<form method='POST' action='/update' enctype='multipart/form-data'>"
    "<input type='file' name='firmware'><input type='submit' value='Update'>"
    "</form>"
  );
}

void setup() {
  Serial.begin(9600);

  WiFi.softAP(ssid, password);  
  Serial.println("AP listo. Conéctate a:");
  Serial.println(ssid);

  server.on("/", HTTP_GET, handleUpdatePage);

  server.on("/update", HTTP_POST, []() {
    server.sendHeader("Connection", "close");
    server.send(200, "text/plain", Update.hasError() ? "FAIL" : "OK");
  }, handleUpdateUpload);

  server.begin();
}

void loop() {
  server.handleClient();
}
""".trimIndent()
}