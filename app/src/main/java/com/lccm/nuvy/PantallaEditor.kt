package com.lccm.nuvy

import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lccm.nuvy.components.NuvyBottomNavBar
import com.lccm.nuvy.ui.theme.NuvyTheme

@OptIn(ExperimentalMaterial3Api::class)
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

    val compilationState by viewModel.compilationState.collectAsState()
    val buildLog by viewModel.buildLog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(compilationState) {
        if (compilationState is CompilationState.Success) {
            val fileName = (compilationState as CompilationState.Success).fileName
            snackbarHostState.showSnackbar("✅ Descargado: $fileName")
            viewModel.resetState()
        } else if (compilationState is CompilationState.Error) {
            val error = compilationState as CompilationState.Error
            snackbarHostState.showSnackbar("❌ ${error.message}")
        }
    }

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Editor de código") })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NuvyBottomNavBar(
                currentDestination = NuvyDestinations.EDITOR,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Código") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Compilar") }
                )
            }

            when (selectedTabIndex) {
                0 -> CodeEditorTab(
                    codeText = codeText,
                    onCodeChange = onCodeChange,
                    currentFileName = currentFileName,
                    onNewFile = { showNewFileDialog = true },
                    onOpenFile = onOpenFile,
                    onSaveFile = { showSaveDialog = true }
                )
                1 -> CompileTab(
                    buildLog = buildLog,
                    compilationState = compilationState,
                    onCompile = onCompile,
                    onUpload = onUpload
                )
            }
        }
    }
}

@Composable
private fun CodeEditorTab(
    codeText: String,
    onCodeChange: (String) -> Unit,
    currentFileName: String,
    onNewFile: () -> Unit,
    onOpenFile: () -> Unit,
    onSaveFile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Editando: $currentFileName",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = codeText,
            onValueChange = onCodeChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            ),
            placeholder = { Text("Escribe tu código C aquí...") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNewFile,
                modifier = Modifier.weight(1f)
            ) {
                Text("Nuevo")
            }
            OutlinedButton(
                onClick = onOpenFile,
                modifier = Modifier.weight(1f)
            ) {
                Text("Abrir")
            }
            Button(
                onClick = onSaveFile,
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar")
            }
        }
    }
}

@Composable
private fun CompileTab(
    buildLog: String,
    compilationState: CompilationState,
    onCompile: () -> Unit,
    onUpload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = buildLog.ifEmpty { "🔨 Presiona Compilar para iniciar\n" },
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCompile,
                modifier = Modifier.weight(1f),
                enabled = compilationState !is CompilationState.Compiling
            ) {
                Text(if (compilationState is CompilationState.Compiling) "Compilando..." else "Compilar")
            }
            OutlinedButton(
                onClick = onUpload,
                modifier = Modifier.weight(1f),
                enabled = compilationState is CompilationState.Success
            ) {
                Text("Subir OTA")
            }
        }
    }
}

@Composable
fun NewFileDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo archivo") },
        text = {
            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = { Text("Nombre del archivo") },
                placeholder = { Text("main") }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotEmpty()) {
                        onCreate(fileName, "// Escribe tu código C aquí...")
                    }
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SaveFileDialog(
    currentFileName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var fileName by remember { mutableStateOf(currentFileName.removeSuffix(".c")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Guardar archivo") },
        text = {
            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = { Text("Nombre del archivo") }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotEmpty()) {
                        onSave(fileName)
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}