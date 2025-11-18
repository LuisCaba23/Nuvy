package com.lccm.nuvy

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import java.io.IOException
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
    currentFileName: String, // Recibe el nombre
    codeText: String,        // Recibe el texto
    onCodeChange: (String) -> Unit, // Reporta cambios en el texto
    onNavigate: (String) -> Unit,
    onOpenFile: () -> Unit,
    onNewFile: () -> Unit,
    onSaveSuccess: () -> Unit, // Reporta el clic en "Guardar"
    onCompile: () -> Unit,
    onUpload: () -> Unit
) {
    val navigationItems = listOf(NuvyDestinations.HOME, NuvyDestinations.CONNECT, NuvyDestinations.EDITOR)
    val navigationIcons = listOf(
        Pair(Icons.Filled.Home, Icons.Outlined.Home),
        Pair(Icons.Filled.Link, Icons.Outlined.Link),
        Pair(Icons.Filled.Code, Icons.Outlined.Code)
    )

    // Ya no hay lógica de "Guardar" aquí

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
                    FilledTonalButton(onClick = onNewFile) { Text("Nueva .c") }
                    FilledTonalButton(onClick = onOpenFile) { Text("Abrir") }
                    FilledTonalButton(
                        onClick = onSaveSuccess // Llama a la acción de guardar del "cerebro"
                    ) { Text("Guardar") }
                }
            }

            // --- PESTAÑAS ELIMINADAS ---

            // --- Editor de Código (ahora permanente) ---
            CodeEditorView(
                modifier = Modifier.weight(1f),
                codeText = codeText,
                onCodeChange = onCodeChange
            )

            // --- Botones de Compilar y Subir ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCompile,
                    modifier = Modifier.weight(1f).height(50.dp),
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
                        .height(50.dp)
                ) {
                    Text("Subir")
                }
            }
        }
    }
}

// --- Pop-up 'NewFileDialog' (Simplificado, solo pide el nombre) ---
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
                    trailingIcon = { Text(".c", color = Color.Gray) }
                )
                Text(
                    text = "El nombre no debe contener espacios.",
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
                    } else {
                        val newFileCode = """
                        // ${fileName}.c
                        #include "pico/stdlib.h"
                        
                        int main() {
                          printf("Nuevo archivo listo en Nuvy!\n");
                          return 0;
                        }
                        """.trimIndent()

                        // Le pasa el nombre y el código al "cerebro"
                        onCreate(fileName, newFileCode)
                    }
                }
            ) {
                Text("Crear y editar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// --- 'CodeEditorView' ---
@Composable
fun CodeEditorView(
    modifier: Modifier = Modifier,
    codeText: String,
    onCodeChange: (String) -> Unit
) {
    val lineNumbers = (1..20).joinToString("\n")
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Text(
                text = lineNumbers,
                textAlign = TextAlign.End,
                color = Color.Gray,
                modifier = Modifier.padding(top = 12.dp, end = 8.dp),
                fontFamily = FontFamily.Monospace,
                lineHeight = 20.sp
            )
            TextField(
                value = codeText,
                onValueChange = onCodeChange,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                )
            )
        }
    }
}

// --- Vista Previa (Actualizada) ---
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
            onNewFile = {},
            onSaveSuccess = {},
            onCompile = {},
            onUpload = {}
        )
    }
}
