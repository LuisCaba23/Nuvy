package com.lccm.nuvy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
fun DownloadReadyScreen(
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onUpload: () -> Unit,
    onSave: () -> Unit
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
                title = { Text("Descarga lista") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // --- Icono de Éxito ---
            Surface(
                shape = CircleShape,
                color = Color(0xFF4CAF50), // Color verde
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
                text = "Tu archivo .uf2 está listo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Revisa los detalles y guarda el archivo en tu dispositivo",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Tarjeta de Información del Archivo ---
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
                        Icon(Icons.Default.DataObject, contentDescription = "Archivo Binario", modifier = Modifier.size(32.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "wifi_setup.uf2", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Generado desde wifi_setup.c", color = Color.Gray, fontSize = 12.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Text(
                                text = "1.2 MB",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Detalles
                    InfoRow(label = "Destino", value = "Pico W (UF2)")
                    InfoRow(label = "Compilador", value = "arm-none-eabi")
                    InfoRow(label = "Versión SDK", value = "1.5.1")
                    InfoRow(label = "Checksum", value = "a9c1...f42e")

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Consejo
                    Text(
                        text = "Consejo: Conecta tu placa en modo bootloader y arrastra el .uf2 al volumen que aparece.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // --- SECCIÓN "ACCIONES" ELIMINADA (TAL COMO PEDISTE) ---

            Spacer(modifier = Modifier.weight(1f)) // Empuja los botones al fondo

            // --- Botones Inferiores (MODIFICADOS) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón secundario para Guardar
                FilledTonalButton(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Guardar .uf2", fontSize = 16.sp)
                }
                // Botón primario para Subir
                Button(
                    onClick = onUpload,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Subir", fontSize = 16.sp)
                }
            }
        }
    }
}

// Componente de ayuda (ya lo teníamos en FileSavedScreen, pero lo copiamos aquí
// para mantener el archivo independiente. En un proyecto real, lo moverías a un
// archivo común "SharedComposables.kt")
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

// --- Vista Previa ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DownloadReadyScreenPreview() {
    NuvyTheme {
        DownloadReadyScreen(
            onNavigate = {},
            onGoBack = {},
            onUpload = {},
            onSave = {}
        )
    }
}