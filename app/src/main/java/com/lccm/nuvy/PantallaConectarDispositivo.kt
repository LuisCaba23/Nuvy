package com.lccm.nuvy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lccm.nuvy.ui.theme.NuvyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectDeviceScreen(
    onPermitAccess: () -> Unit,
    onTryAgain: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val navigationItems = listOf(NuvyDestinations.HOME, NuvyDestinations.CONNECT, NuvyDestinations.EDITOR)
    val navigationIcons = listOf(
        Pair(Icons.Filled.Home, Icons.Outlined.Home),
        Pair(Icons.Filled.Link, Icons.Outlined.Link),
        Pair(Icons.Filled.Code, Icons.Outlined.Code)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Conectar dispositivo") }
            )
        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEachIndexed { index, item ->
                    val icons = navigationIcons[index]
                    val isSelected = (item == NuvyDestinations.CONNECT)
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
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio automático entre cada elemento
        ) {

            // --- Tarjeta 1: Esperando conexión ---
            StatusCard(
                title = "Esperando conexión",
                subtitle = "Conecta tu placa por USB y autoriza el acceso"
            )

            // --- Tarjeta 2: Pasos ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StepItem(
                        number = "1",
                        title = "Conecta el cable USB",
                        description = "Usa un cable de datos y asegúrate de que la placa esté encendida."
                    )
                    StepItem(
                        number = "2",
                        title = "Selecciona tu dispositivo",
                        description = "Elige de la lista cuando aparezca abajo."
                    )
                    StepItem(
                        number = "3",
                        title = "Compila y carga",
                        description = "Te entregaremos el archivo .uf2 automáticamente."
                    )
                }
            }

            // --- Tarjeta 3: Dispositivo ---
            DeviceCard(
                name = "Raspberry Pi Pico W",
                details = "USB • rp2040"
            )

            // Spacer para empujar los botones hacia abajo si hay espacio
            Spacer(modifier = Modifier.weight(1f))

            // --- Botones ---
            Button(
                onClick = onPermitAccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = "Permitir acceso USB", fontSize = 16.sp)
            }

            TextButton(
                onClick = onTryAgain,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Probar de nuevo", fontSize = 16.sp)
            }
        }
    }
}

// --- Componentes de Ayuda para esta pantalla ---

@Composable
private fun StatusCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun StepItem(number: String, title: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Círculo con el número
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        // Columna con los textos
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = description,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun DeviceCard(name: String, details: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = details,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}


// --- Vista Previa ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConnectDeviceScreenPreview() {
    NuvyTheme {
        ConnectDeviceScreen(
            onPermitAccess = {},
            onTryAgain = {},
            onNavigate = {}
        )
    }
}