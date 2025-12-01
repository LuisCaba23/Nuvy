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
import com.lccm.nuvy.components.NuvyBottomNavBar
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
    onFileClick: (FileListItem) -> Unit,
    files: List<FileListItem>,
    onFileImported: (Uri) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

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
            NuvyBottomNavBar(
                currentDestination = NuvyDestinations.EDITOR,
                onNavigate = onNavigate
            )
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
            }}}}
