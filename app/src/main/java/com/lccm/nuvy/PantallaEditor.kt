package com.lccm.nuvy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
    onNavigate: (String) -> Unit,
    onOpenFile: () -> Unit,
    onNewFile: () -> Unit,
    onSave: () -> Unit,
    onCompile: () -> Unit,
    onUpload: () -> Unit // <-- 1. AÑADE EL PARÁMETRO DE NUEVO
) {
    val navigationItems = listOf(NuvyDestinations.HOME, NuvyDestinations.CONNECT, NuvyDestinations.EDITOR)
    val navigationIcons = listOf(
        Pair(Icons.Filled.Home, Icons.Outlined.Home),
        Pair(Icons.Filled.Link, Icons.Outlined.Link),
        Pair(Icons.Filled.Code, Icons.Outlined.Code)
    )

    // ... (Estados de codeText, selectedTabIndex, etc. no cambian)
    var codeText by remember {
        mutableStateOf("// Escribe tu código C aquí...")
    }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("main.c", "Build log")
    val lineNumbers = (1..20).joinToString("\n")


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Editor de código") })
        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEachIndexed { index, item ->
                    val icons = navigationIcons[index]
                    val isSelected = (item == NuvyDestinations.EDITOR)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onNavigate(item) },
                        label = { Text(text = item) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) icons.first else icons.second,
                                contentDescription = item
                            )
                        }
                    )
                }
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

            // --- Fila de botones superior ---
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    FilledTonalButton(onClick = onNewFile) { Text("Nueva .c") }
                    FilledTonalButton(onClick = onOpenFile) { Text("Abrir") }
                    FilledTonalButton(onClick = onSave) { Text("Guardar") }
                }
            }

            // --- Pestañas de Archivos ---
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // --- Área del Editor de Código ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(modifier = Modifier.padding(8.dp)) {
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
                        onValueChange = { codeText = it },
                        modifier = Modifier.fillMaxSize(),
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

            // --- Botones de Compilar y Subir ---
            // 2. REVIERTE LOS CAMBIOS AQUÍ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCompile, // Botón de compilar
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Compilar", fontSize = 16.sp)
                }
                // Vuelve a añadir el botón "Subir"
                FilledTonalButton(
                    onClick = onUpload,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Subir", fontSize = 16.sp)
                }
            }
        }
    }
}

// --- Vista Previa ---
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
            onUpload = {} // <-- 3. AÑÁDELO A LA VISTA PREVIA
        )
    }
}