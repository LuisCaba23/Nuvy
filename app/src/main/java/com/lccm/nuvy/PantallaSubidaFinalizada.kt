package com.lccm.nuvy

import androidx.compose.foundation.layout.*
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
fun UploadCompleteScreen(
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onFinalize: () -> Unit
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
                title = { Text("Transferencia finalizada") }, // Título actualizado
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Icono de Éxito ---
            Icon(
                imageVector = Icons.Default.CheckCircle, // Palomita verde
                contentDescription = "Transferencia finalizada",
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF4CAF50) // Verde
            )
            Text(
                text = "Transferencia finalizada", // Título actualizado
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "El archivo .uf2 se ha subido correctamente a tu dispositivo.", // Descripción actualizada
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            // --- Tarjeta de Progreso (al 100%) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.DataObject, contentDescription = "Archivo Binario", modifier = Modifier.size(32.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "wifi_setup.uf2", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Destino: Pico W (UF2)", color = Color.Gray, fontSize = 12.sp)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface) {
                            Text("1.2 MB", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
                        }
                    }
                    // Barra de progreso al 100%
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(
                            progress = { 1.0f }, // 100%
                            modifier = Modifier.weight(1f)
                        )
                        Text("00:00", fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) // Tiempo final
                    }
                    InfoRow(label = "Puerto", value = "USB • tty.usbmodem14101")
                    InfoRow(label = "Velocidad", value = "480 Mbps")
                    InfoRow(label = "Verificación", value = "Checksum OK")

                    Text(
                        text = "La placa ha sido programada con éxito.", // Mensaje de éxito
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // --- Tarjeta de Dispositivo (No cambia) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Dispositivo", style = MaterialTheme.typography.titleMedium)
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface) {
                            Text("Conectado", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, color = Color(0xFF4CAF50))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.Usb, contentDescription = "Dispositivo USB", modifier = Modifier.size(32.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Raspberry Pi Pico W", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "UF2 Bootloader", color = Color.Gray, fontSize = 12.sp)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface) {
                            Text("USB", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Botón Finalizar (No cambia) ---
            Button(
                onClick = onFinalize,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = "Finalizar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalizar", fontSize = 16.sp)
                }
            }
        }
    }
}

// Componente de ayuda (InfoRow, no cambia)
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// --- Vista Previa ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UploadCompleteScreenPreview() {
    NuvyTheme {
        UploadCompleteScreen(
            onNavigate = {},
            onGoBack = {},
            onFinalize = {}
        )
    }
}