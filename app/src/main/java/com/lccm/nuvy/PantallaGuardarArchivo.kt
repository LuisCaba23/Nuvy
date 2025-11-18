package com.lccm.nuvy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lccm.nuvy.ui.theme.NuvyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSavedScreen(
    onNavigate: (String) -> Unit,
    onGoBackToEditor: () -> Unit,
    onTryAgain: () -> Unit,
    fileName: String
) {
    val navigationItems = listOf(NuvyDestinations.HOME, NuvyDestinations.CONNECT, NuvyDestinations.EDITOR)
    val navigationIcons = listOf(
        Pair(Icons.Filled.Home, Icons.Outlined.Home),
        Pair(Icons.Filled.Link, Icons.Outlined.Link),
        Pair(Icons.Filled.Code, Icons.Outlined.Code)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cambios guardados") },
                navigationIcon = {
                    IconButton(onClick = onGoBackToEditor) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // --- Icono de Éxito ---
            Surface(
                shape = CircleShape,
                color = Color(0xFF4CAF50),
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Éxito",
                    tint = Color.White,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Textos de Estado ---
            Text(
                text = "Guardado exitoso",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tu archivo $fileName fue guardado y almacenado.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Detalles del Guardado ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(label = "Archivo", value = fileName)
                InfoRow(label = "Destino", value = "Almacenamiento/Descargas")
            }

            // Spacer para empujar el botón hacia abajo
            Spacer(modifier = Modifier.weight(1f))

            // --- Botón de Acción ---
            Button(
                onClick = onGoBackToEditor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Volver al editor", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 16.sp)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FileSavedScreenPreview() {
    NuvyTheme {
        FileSavedScreen(
            onNavigate = {},
            onGoBackToEditor = {},
            onTryAgain = {},
            fileName = "preview.c"
        )
    }
}
