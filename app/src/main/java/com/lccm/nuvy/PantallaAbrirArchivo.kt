package com.lccm.nuvy

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lccm.nuvy.components.NuvyBottomNavBar
import com.lccm.nuvy.ui.theme.NuvyTheme
import java.io.File

data class FileListItem(
    val name: String,
    val path: String,
    val sizeInBytes: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenFileScreen(
    onNavigate: (String) -> Unit,
    onOpen: () -> Unit,
    onFileClick: (FileListItem) -> Unit,
    files: List<FileListItem>,
    onFileImported: (Uri) -> Unit
) {
    val context = LocalContext.current
    var selectedFile by remember { mutableStateOf<FileListItem?>(null) }
    var showPreview by remember { mutableStateOf(false) }

    // Launcher para importar archivos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onFileImported(uri)
        } else {
            Toast.makeText(context, "No se seleccionó ningún archivo", Toast.LENGTH_SHORT).show()
        }
    }

    // Mostrar vista previa si se seleccionó un archivo
    if (showPreview && selectedFile != null) {
        val fileContent = remember(selectedFile) {
            try {
                File(selectedFile!!.path).readText()
            } catch (e: Exception) {
                "Error al leer el archivo: ${e.message}"
            }
        }

        FilePreviewScreen(
            onNavigate = onNavigate,
            onEdit = {
                onFileClick(selectedFile!!)
                showPreview = false
            },
            onCancel = {
                showPreview = false
                selectedFile = null
            },
            fileName = selectedFile!!.name,
            fileContent = fileContent
        )
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Abrir Archivo") })
        },
        bottomBar = {
            NuvyBottomNavBar(
                currentDestination = NuvyDestinations.OPEN_FILE,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Botón para importar archivos
            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Upload, contentDescription = "Importar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importar desde dispositivo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Archivos en Descargas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (files.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Sin archivos",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay archivos .c en Descargas",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(files) { file ->
                        FileCard(
                            file = file,
                            onClick = {
                                selectedFile = file
                                showPreview = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileCard(file: FileListItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Archivo",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = formatFileSize(file.sizeInBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OpenFileScreenPreview() {
    NuvyTheme {
        OpenFileScreen(
            onNavigate = {},
            onOpen = {},
            onFileClick = {},
            files = listOf(
                FileListItem("ejemplo.c", "/path/to/ejemplo.c", 1234),
                FileListItem("test.c", "/path/to/test.c", 5678)
            ),
            onFileImported = {}
        )
    }
}