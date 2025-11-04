package com.lccm.nuvy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lccm.nuvy.ui.theme.NuvyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenFileScreen(
    onNavigate: (String) -> Unit,
    onOpen: () -> Unit,
    onImport: () -> Unit,
    onFileClick: () -> Unit // Parámetro para manejar el clic en un archivo
) {
    val navigationItems = listOf(NuvyDestinations.HOME, NuvyDestinations.CONNECT, NuvyDestinations.EDITOR)
    val navigationIcons = listOf(
        Pair(Icons.Filled.Home, Icons.Outlined.Home),
        Pair(Icons.Filled.Link, Icons.Outlined.Link),
        Pair(Icons.Filled.Code, Icons.Outlined.Code)
    )

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Abrir archivo") })
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Buscador ---
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar .c en este dispositivo") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50)
                )
            }

            // --- Chip de Filtro ---
            item {
                FilterChip(
                    selected = true,
                    onClick = { /*TODO*/ },
                    label = { Text("Archivos locales") }
                )
            }

            // --- Sección de Recientes ---
            item {
                Text(
                    text = "Recientes",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            item {
                FileItem(
                    name = "blinky.c",
                    details = "Modificado hace 2 días • 1.2 KB • Local",
                    iconColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onFileClick // Pasa la acción de clic
                )
            }
            item {
                FileItem(
                    name = "wifi_setup.c",
                    details = "Modificado hoy • 3.8 KB • Nube",
                    iconColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onFileClick // Pasa la acción de clic
                )
            }
            item {
                FileItem(
                    name = "pwm_driver.c",
                    details = "Modificado hace 5 h • 6.1 KB • Local",
                    iconColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onFileClick // Pasa la acción de clic
                )
            }

            // --- Sección de Carpetas ---
            item {
                Text(
                    text = "Carpetas",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            item {
                FolderItem(
                    name = "/Proyectos/pico/",
                    details = "12 archivos • Local",
                    iconColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { /* TODO: Navegar a carpeta */ }
                )
            }
            item {
                FolderItem(
                    name = "/Nuvy Cloud/",
                    details = "8 archivos • Nube",
                    iconColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { /* TODO: Navegar a carpeta */ }
                )
            }

            // --- Botones Inferiores ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilledTonalButton(
                        onClick = onImport,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Importar", fontSize = 16.sp)
                    }
                    Button(
                        onClick = onOpen,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Abrir", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// --- Componentes de Ayuda (Actualizados con onClick) ---

@OptIn(ExperimentalMaterial3Api::class) // Necesario para Card clicable
@Composable
fun FileItem(name: String, details: String, iconColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick // <-- Tarjeta clicable
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = details, color = Color.Gray, fontSize = 12.sp)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Necesario para Card clicable
@Composable
fun FolderItem(name: String, details: String, iconColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick // <-- Tarjeta clicable
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = details, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

// --- Vista Previa ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OpenFileScreenPreview() {
    NuvyTheme {
        OpenFileScreen(
            onNavigate = {},
            onOpen = {},
            onImport = {},
            onFileClick = {} // Parámetro añadido para la vista previa
        )
    }
}