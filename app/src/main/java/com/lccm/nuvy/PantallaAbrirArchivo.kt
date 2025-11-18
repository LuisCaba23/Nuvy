package com.lccm.nuvy

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// --- ESTRUCTURA DE DATOS (NO PRIVADA) ---
// MainActivity necesita ver esto
data class FileListItem(
    val name: String,
    val details: String,
    val isFolder: Boolean = false,
    val tag: String? = ".c"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenFileScreen(
    onNavigate: (String) -> Unit,
    onOpen: () -> Unit,
    // 'onFileClick' ahora pasa el FileListItem completo
    onFileClick: (FileListItem) -> Unit,
    files: List<FileListItem>, // Recibe la lista desde el "cerebro"
    onFileImported: (Uri) -> Unit // Reporta un archivo importado
) {
    val navigationItems = listOf(NuvyDestinations.HOME, NuvyDestinations.CONNECT, NuvyDestinations.EDITOR)
    val navigationIcons = listOf(
        Pair(Icons.Filled.Home, Icons.Outlined.Home),
        Pair(Icons.Filled.Link, Icons.Outlined.Link),
        Pair(Icons.Filled.Code, Icons.Outlined.Code)
    )

    var searchQuery by remember { mutableStateOf("") }

    // Filtra la lista 'files' que viene de MainActivity
    val filteredList = remember(searchQuery, files) {
        if (searchQuery.isEmpty()) {
            files
        } else {
            files.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val context = LocalContext.current

    // Lógica de importación
    val onFileSelected: (Uri?) -> Unit = { uri ->
        if (uri != null) {
            onFileImported(uri)
        } else {
            Toast.makeText(context, "Importación cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = onFileSelected
    )

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

            // --- Título ---
            item {
                Text(
                    text = "Archivos",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // --- Lista de Archivos (desde 'filteredList') ---
            items(filteredList) { fileItem ->
                if (fileItem.isFolder) {
                    FolderItem(
                        name = fileItem.name,
                        details = fileItem.details,
                        iconColor = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { /* TODO: Navegar a carpeta */ }
                    )
                } else {
                    FileItem(
                        name = fileItem.name,
                        details = fileItem.details,
                        iconColor = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { onFileClick(fileItem) } // Pasa el 'fileItem'
                    )
                }
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
                        onClick = {
                            val mimeTypes = arrayOf("text/plain", "text/x-c", "application/octet-stream")
                            filePickerLauncher.launch(mimeTypes)
                        },
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

// --- COMPONENTES DE AYUDA (NO PRIVADOS) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileItem(name: String, details: String, iconColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderItem(name: String, details: String, iconColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick
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
            onFileClick = {},
            files = listOf(
                FileListItem(name = "preview_file.c", details = "Preview details"),
                FileListItem(name = "/PreviewFolder/", details = "Preview folder", isFolder = true)
            ),
            onFileImported = {}
        )
    }
}