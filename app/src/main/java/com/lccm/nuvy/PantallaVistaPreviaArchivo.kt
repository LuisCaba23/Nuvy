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
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    // Parámetros para mostrar el contenido dinámico
    fileName: String,
    fileContent: String
) {
    val navigationItems = listOf(NuvyDestinations.HOME, NuvyDestinations.CONNECT, NuvyDestinations.EDITOR)
    val navigationIcons = listOf(
        Pair(Icons.Filled.Home, Icons.Outlined.Home),
        Pair(Icons.Filled.Link, Icons.Outlined.Link),
        Pair(Icons.Filled.Code, Icons.Outlined.Code)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Vista previa") })
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Pasa el nombre del archivo a la tarjeta de info
            FilePreviewInfoCard(fileName = fileName)

            Text(
                text = "Contenido",
                style = MaterialTheme.typography.titleMedium
            )

            // Muestra el contenido del archivo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = fileContent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Text(
                text = "Vista previa de solo lectura. Toca Editar para editar el archivo.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // Botones "Cancelar" y "Editar"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledTonalButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Cancelar", fontSize = 16.sp)
                }
                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Editar", fontSize = 16.sp)
                }
            }
        }
    }
}

// --- Composable de Ayuda: Tarjeta de Info (no privado) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePreviewInfoCard(fileName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = fileName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    // Detalles de ejemplo
                    Text(text = "3.8 KB • Nube • modificado hoy", color = Color.Gray, fontSize = 12.sp)
                }
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
            onEdit = {},
            onCancel = {},
            fileName = "preview.c",
            fileContent = "// Contenido de preview.c"
        )
    }
}