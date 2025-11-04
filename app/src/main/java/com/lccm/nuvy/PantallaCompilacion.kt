package com.lccm.nuvy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lccm.nuvy.ui.theme.NuvyTheme

// (La estructura de datos BuildStep no cambia)
data class BuildStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconDescription: String,
    val statusColor: Color = Color.Gray,
    val iconTint: Color = Color.Gray
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildScreen(
    onNavigate: (String) -> Unit,
    onGoBack: () -> Unit,
    onCancelBuild: () -> Unit,
    onBuildComplete: () -> Unit
    // Se eliminó 'onDownloadUf2'
) {

    val navigationItems = listOf(NuvyDestinations.HOME, NuvyDestinations.CONNECT, NuvyDestinations.EDITOR)
    val navigationIcons = listOf(
        Pair(Icons.Filled.Home, Icons.Outlined.Home),
        Pair(Icons.Filled.Link, Icons.Outlined.Link),
        Pair(Icons.Filled.Code, Icons.Outlined.Code)
    )

    val greenColor = Color(0xFF4CAF50)
    val primaryColor = MaterialTheme.colorScheme.primary // Obtenemos el color primario

    // Lista de pasos (simulamos un estado)
    val buildSteps = listOf(
        BuildStep("Validación del código", "Sin errores encontrados", Icons.Default.CheckCircle, "Validado", greenColor, greenColor),
        BuildStep("Subiendo a la nube", "Proyecto sincronizado", Icons.Default.CheckCircle, "Subido", greenColor, greenColor),
        BuildStep("Compilando", "Generando binarios...", Icons.Outlined.Build, "Compilando", primaryColor, primaryColor),
        BuildStep("Empaquetando .uf2", "Preparando descarga", Icons.Outlined.Archive, "Empaquetando", Color.Gray, Color.Gray)
    )
    onBuildComplete() // Llama a la siguiente pantalla cuando termina
    // Se eliminaron 'showDownloadButton' y 'LaunchedEffect'

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generando .uf2") },
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
                .padding(horizontal = 16.dp),
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Información del Archivo ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "wifi_setup.c", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "Compilando en la nube • aprox. 2 min", color = Color.Gray, fontSize = 13.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Text(
                                text = ".c -> .uf2",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // --- Pasos de la Compilación ---
                itemsIndexed(buildSteps) { index, step ->
                    BuildProcessStep(step = step, isLast = (index == buildSteps.lastIndex))
                }

                // --- Salida de la Compilación ---
                item {
                    Text(
                        text = "Salida de la compilación",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp, max = 250.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = """
                            $ cmake ..
                            -- Configuring done
                            -- Generating done
                            $ make -j4
                            [ 35%] Building C object src/main.c.obj
                            [ 82%] Linking C executable app
                            [100%] Built target app
                            """.trimIndent(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // --- Botón Inferior (MODIFICADO) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // El botón "Cancelar" ahora ocupa todo el ancho
                FilledTonalButton(
                    onClick = onCancelBuild,
                    modifier = Modifier
                        .fillMaxWidth() // Ocupa todo el ancho
                        .height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Cancelar", fontSize = 16.sp)
                }

                // Se eliminó el botón "Descargar .uf2"
            }
        }
    }
}

// --- Componente de Ayuda (No cambia) ---
@Composable
fun BuildProcessStep(step: BuildStep, isLast: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(step.statusColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = step.iconDescription,
                    tint = step.iconTint
                )
            }
            Column {
                Text(text = step.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(text = step.description, color = Color.Gray, fontSize = 12.sp)
            }
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .padding(start = 11.dp)
                    .width(2.dp)
                    .height(24.dp)
                    .background(Color.LightGray)
            )
        }
    }
}


// --- Vista Previa (Actualizada) ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BuildScreenPreview() {
    NuvyTheme {
        BuildScreen(
            onNavigate = {},
            onGoBack = {},
            onCancelBuild = {},
            onBuildComplete = {}
            // Se eliminó 'onDownloadUf2'
        )
    }
}