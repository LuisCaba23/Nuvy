package com.lccm.nuvy

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lccm.nuvy.ui.theme.NuvyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePreviewScreen(
    onNavigate: (String) -> Unit,
    onGoToEditor: () -> Unit,
    onGenerateUf2: () -> Unit
) {
    val navigationItems = listOf(NuvyDestinations.HOME, NuvyDestinations.CONNECT, NuvyDestinations.EDITOR)
    val navigationIcons = listOf(
        Pair(Icons.Filled.Home, Icons.Outlined.Home),
        Pair(Icons.Filled.Link, Icons.Outlined.Link),
        Pair(Icons.Filled.Code, Icons.Outlined.Code)
    )

    // Texto de ejemplo para el código
    val sampleCode = """
    #include "pico/stdlib.h"
    
    int main() {
      stdio_init_all();
      printf("Hello, Nuvy!\n");
      while (true) {
        sleep_ms(1000);
      }
    }
    """.trimIndent()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Vista previa") })
        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEachIndexed { index, item ->
                    val icons = navigationIcons[index]
                    val isSelected = (item == NuvyDestinations.EDITOR) // Sigue en la sección "Editor"
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Tarjeta de Información del Archivo ---
            FilePreviewInfoCard()

            // --- Contenido del Archivo ---
            Text(
                text = "Contenido",
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Ocupa el espacio sobrante
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = sampleCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // --- Texto de ayuda ---
            Text(
                text = "Vista previa de solo lectura. Toca Editor para editar el archivo.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // --- Botones Inferiores ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledTonalButton(
                    onClick = onGoToEditor,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Editor", fontSize = 16.sp)
                }
                Button(
                    onClick = onGenerateUf2,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Generar .uf2", fontSize = 16.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilePreviewInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila superior
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icono
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                )
                // Textos
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "wifi_setup.c", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "3.8 KB • Nube • modificado hoy", color = Color.Gray, fontSize = 12.sp)
                }
                // Chip ".c"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Text(
                        text = ".c",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Fila de Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { /*TODO*/ }, label = { Text("124 líneas") })
                AssistChip(onClick = { /*TODO*/ }, label = { Text("2 min aprox. build") })
                AssistChip(onClick = { /*TODO*/ }, label = { Text("Sincronizado") })
            }
        }
    }
}

// --- Vista Previa ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FilePreviewScreenPreview() {
    NuvyTheme {
        FilePreviewScreen(
            onNavigate = {},
            onGoToEditor = {},
            onGenerateUf2 = {}
        )
    }
}