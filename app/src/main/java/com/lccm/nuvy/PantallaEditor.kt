package com.lccm.nuvy

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
import androidx.compose.ui.Alignment
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
    onNavigate: (String) -> Unit,
    onOpenFile: () -> Unit,
    onNewFile: () -> Unit,
    onSave: () -> Unit,
    onCompile: () -> Unit,
    onUpload: () -> Unit,
    viewModel: EditorViewModel
) {
    var codeText by remember { mutableStateOf("// Escribe tu código C aquí...") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedNavIndex by remember { mutableIntStateOf(1) }
    val tabs = listOf("main.c", "Build log")

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

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Editor de código") }) },
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
                    onClick = { 
                        selectedNavIndex = 2
                        // Por ahora no navega a ningún lado, puedes agregar funcionalidad después
                    },
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
                        onClick = onNewFile,
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
                        onClick = onSave,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar")
                    }
                }
            }

            // Pestañas
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Editor o Build Log
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (selectedTabIndex == 0) {
                    // Editor de código
                    Row(modifier = Modifier.padding(8.dp)) {
                        // Números de línea
                        Column(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            val lineCount = codeText.count { it == '\n' } + 1
                            (1..lineCount).forEach { lineNumber ->
                                Text(
                                    text = "$lineNumber",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.width(30.dp)
                                )
                            }
                        }
                        
                        // Editor de texto
                        TextField(
                            value = codeText,
                            onValueChange = { codeText = it },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                } else {
                    // Build log
                    Text(
                        text = buildLog.ifEmpty { "Sin actividad de compilación" },
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

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        selectedTabIndex = 1 // Cambiar a Build log
                        viewModel.compileCode(codeText)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = compilationState !is CompilationState.Compiling
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
                        .height(50.dp)
                ) {
                    Text("Subir")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditorScreenPreview() {
    NuvyTheme {
        EditorScreen(
            onNavigate = {},
            onOpenFile = {},
            onNewFile = {},
            onSave = {},
            onCompile = {},
            onUpload = {},
            viewModel = EditorViewModel()
        )
    }
}
