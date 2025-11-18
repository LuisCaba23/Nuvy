package com.lccm.nuvy

import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var selectedNavIndex by remember { mutableIntStateOf(1) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val compilationState by viewModel.compilationState.collectAsState()
    val buildLog by viewModel.buildLog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar notificación automática cuando se descarga
    LaunchedEffect(compilationState) {
        if (compilationState is CompilationState.Success) {
            val fileName = (compilationState as CompilationState.Success).fileName
            snackbarHostState.showSnackbar(
                message = "✅ Descargado: $fileName",
                duration = SnackbarDuration.Long
            )
            viewModel.resetState()
        } else if (compilationState is CompilationState.Error) {
            val error = compilationState as CompilationState.Error
            snackbarHostState.showSnackbar(
                message = "❌ Error: ${error.message}",
                duration = SnackbarDuration.Long
            )
        }
    }

    // Diálogo para nuevo archivo
    if (showNewFileDialog) {
        NewFileDialog(
            onDismiss = { showNewFileDialog = false },
            onCreate = { fileName, code ->
                onNewFile(fileName, code)
                showNewFileDialog = false
            }
        )
    }

    // Diálogo para guardar archivo
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
            CenterAlignedTopAppBar(
                title = { Text("Editor de código") }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedNavIndex == 0,
                    onClick = {
                        selectedNavIndex = 0
                        onNavigate("home")
                    },
                    icon = {
                        Icon(
                            if (selectedNavIndex == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Inicio"
                        )
                    },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 1,
                    onClick = { selectedNavIndex = 1 },
                    icon = {
                        Icon(
                            if (selectedNavIndex == 1) Icons.Filled.Code else Icons.Outlined.Code,
                            contentDescription = "Editor"
                        )
                    },
                    label = { Text("Editor") }
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 2,
                    onClick = { selectedNavIndex = 2 },
                    icon = {
                        Icon(
                            if (selectedNavIndex == 2) Icons.Filled.Link else Icons.Outlined.Link,
                            contentDescription = "Conexión"
                        )
                    },
                    label = { Text("Conexión") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botones superiores
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showNewFileDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Nueva .c")
                    }
                    FilledTonalButton(
                        onClick = onOpenFile,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Abrir")
                    }
                    FilledTonalButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar")
                    }
                }
            }

            // Tabs del archivo y build log
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text(currentFileName) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Build Log") }
                )
            }

            // Contenido según tab seleccionado
            if (selectedTabIndex == 0) {
                // Editor de código
                CodeEditorView(
                    modifier = Modifier.weight(1f),
                    codeText = codeText,
                    onCodeChange = onCodeChange
                )
            } else {
                // Build Log
                BuildLogView(
                    modifier = Modifier.weight(1f),
                    buildLog = buildLog
                )
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        selectedTabIndex = 1 // Cambiar a Build Log
                        onCompile()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = compilationState !is CompilationState.Compiling,
                    shape = RoundedCornerShape(50)
                ) {
                    if (compilationState is CompilationState.Compiling) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Compilar")
                    }
                }

                FilledTonalButton(
                    onClick = onUpload,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Subir a Pico")
                }
            }
        }
    }
}

@Composable
fun BuildLogView(
    modifier: Modifier = Modifier,
    buildLog: String
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (buildLog.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "Sin actividad de compilación",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Text(
                text = buildLog,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun NewFileDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear nuevo archivo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Define el nombre y comienza a editar")
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Text(".c", color = Color.Gray) }
                )
                Text(
                    text = "El nombre no debe contener espacios ni caracteres especiales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isEmpty()) {
                        Toast.makeText(context, "Por favor, escribe un nombre", Toast.LENGTH_SHORT).show()
                    } else if (fileName.contains(" ")) {
                        Toast.makeText(context, "El nombre no debe contener espacios", Toast.LENGTH_SHORT).show()
                    } else {
                        val newFileCode = """
                        // $fileName.c
                        #include "pico/stdlib.h"
                        
                        int main() {
                            printf("Nuevo archivo listo en Nuvy!\n");
                            return 0;
                        }
                        """.trimIndent()

                        onCreate(fileName, newFileCode)
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
    var fileName by remember { 
        mutableStateOf(
            if (currentFileName == "Unnamed.c") "" 
            else currentFileName.removeSuffix(".c")
        ) 
    }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Guardar archivo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Define el nombre del archivo")
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Text(".c", color = Color.Gray) }
                )
                Text(
                    text = "Se guardará en la carpeta Descargas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isEmpty()) {
                        Toast.makeText(context, "Por favor, escribe un nombre", Toast.LENGTH_SHORT).show()
                    } else if (fileName.contains(" ")) {
                        Toast.makeText(context, "El nombre no debe contener espacios", Toast.LENGTH_SHORT).show()
                    } else {
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

@Composable
fun CodeEditorView(
    modifier: Modifier = Modifier,
    codeText: String,
    onCodeChange: (String) -> Unit
) {
    val lineCount = codeText.count { it == '\n' } + 1
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            // Números de línea
            Column(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                (1..lineCount).forEach { lineNumber ->
                    Text(
                        text = "$lineNumber",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(30.dp),
                        lineHeight = 20.sp
                    )
                }
            }
            
            // Editor de texto
            TextField(
                value = codeText,
                onValueChange = onCodeChange,
                modifier = Modifier.fillMaxSize(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditorScreenPreview() {
    NuvyTheme {
        EditorScreen(
            currentFileName = "main.c",
            codeText = "// Código de ejemplo",
            onCodeChange = {},
            onNavigate = {},
            onOpenFile = {},
            onNewFile = { _, _ -> },
            onSaveFile = {},
            onCompile = {},
            onUpload = {},
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        )
    }
}
