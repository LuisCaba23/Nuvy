package com.lccm.nuvy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight // 👈 IMPORTANTE
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lccm.nuvy.components.NuvyBottomNavBar

@OptIn(ExperimentalMaterial3Api::class) // 👈 IMPORTANTE para FilterChip
@Composable
fun EditorScreen(
    currentFileName: String,
    codeText: String,
    onCodeChange: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenFile: () -> Unit,
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

    // Diálogos y Scaffold (Igual que antes) ...
    // ...
    // Asegúrate de copiar el resto del Scaffold, TabRow y Tabs de tu código anterior
    // Lo importante aquí es que TabRow llama a CodeEditorTab

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
                    onNewFile = { showNewFileDialog = true },
                    onOpenFile = onOpenFile,
                    onSaveFile = { showSaveDialog = true },
                    onCompile = {
                        viewModel.setFileType(selectedFileType)
                        onCompile()
                        selectedTabIndex = 1 // Cambiar a tab de logs automáticamente
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
    // ... Diálogos aquí ...
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CodeEditorTab(
    codeText: String,
    onCodeChange: (String) -> Unit,
    currentFileName: String,
    onNewFile: () -> Unit,
    onOpenFile: () -> Unit,
    onSaveFile: () -> Unit,
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

        // Botones extra (Nuevo, Abrir, Guardar) ...
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onNewFile, modifier = Modifier.weight(1f)) { Text("Nuevo") }
            OutlinedButton(onClick = onSaveFile, modifier = Modifier.weight(1f)) { Text("Guardar") }
        }
    }
}

// CompileTab se mantiene igual que en tu código anterior
@Composable
private fun CompileTab(
    buildLog: String,
    compilationState: CompilationState,
    onNavigateToConnect: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF222222))
        ) {
            Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
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