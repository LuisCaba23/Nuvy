package com.lccm.nuvy

// --- IMPORTACIONES NUEVAS ---
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import java.io.IOException
// --- FIN DE IMPORTACIONES NUEVAS ---

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lccm.nuvy.ui.theme.NuvyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFileScreen(
    onGoBack: () -> Unit,
    // --- ¡CAMBIO CLAVE AQUÍ! ---
    // 'onCreate' ahora debe aceptar el nombre y el contenido
    onCreate: (String, String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }

    val newFileCode = """
    // nuevo.c
    #include "pico/stdlib.h"
    
    int main() {
      printf("Nuevo archivo listo en Nuvy!\n");
      return 0;
    }
    """.trimIndent()

    val context = LocalContext.current

    val onFileCreated: (Uri?) -> Unit = { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(newFileCode.toByteArray())
                }
                Toast.makeText(context, "¡Archivo creado!", Toast.LENGTH_SHORT).show()
                // --- ¡CAMBIO CLAVE AQUÍ! ---
                // Le pasamos el nombre (sin .c) y el código de plantilla
                // de vuelta al "cerebro" (MainActivity)
                onCreate(fileName, newFileCode)

            } catch (e: IOException) {
                Toast.makeText(context, "Error al crear el archivo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Creación cancelada", Toast.LENGTH_SHORT).show()
        }
    }

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = onFileCreated
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo archivo .c") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Tarjeta 1 (El botón 'Crear y editar' ya está bien) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Crear nuevo archivo", style = MaterialTheme.typography.titleMedium)
                    // ... (El resto de la tarjeta no cambia)
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        // ...
                    )
                    // ...
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onGoBack,
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancelar") }

                        Button(
                            onClick = {
                                if (fileName.isEmpty()) {
                                    Toast.makeText(context, "Por favor, escribe un nombre", Toast.LENGTH_SHORT).show()
                                } else {
                                    createFileLauncher.launch("$fileName.c")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Crear y editar") }
                    }
                }
            }

            // --- Tarjeta 2 (La lógica reactiva del nombre ya está bien) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (fileName.isEmpty()) "nuevo.c" else "$fileName.c",
                            style = MaterialTheme.typography.titleMedium
                        )
                        // ... (El resto no cambia)
                    }
                }
            }
        }
    }
}

// --- Vista Previa Actualizada ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewFileScreenPreview() {
    NuvyTheme {
        NewFileScreen(
            onGoBack = {},
            onCreate = { _, _ -> } // La vista previa solo le pasa acciones vacías
        )
    }
}