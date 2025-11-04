package com.lccm.nuvy

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lccm.nuvy.ui.theme.NuvyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewFileScreen(
    onGoBack: () -> Unit,
    onCreate: () -> Unit
    // Se eliminó 'onSave'
) {
    // Estado para el nombre del archivo
    var fileName by remember { mutableStateOf("") }

    // Texto de ejemplo para el nuevo archivo
    val newFileCode = """
    // nuevo.c
    #include "pico/stdlib.h"
    
    int main() {
      printf("Nuevo archivo listo en Nuvy!\n");
      return 0;
    }
    """.trimIndent()

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

            // --- Tarjeta 1: Crear nuevo archivo ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Crear nuevo archivo", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Define el nombre y comienza a editar", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Text(".c", color = Color.Gray) }
                    )

                    Text(
                        text = "El nombre no debe contener espacios. Se guardará en el proyecto Nuvy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onGoBack, // Acción de "Cancelar"
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancelar") }
                        Button(
                            onClick = onCreate, // Acción de "Crear"
                            modifier = Modifier.weight(1f)
                        ) { Text("Crear y editar") }
                    }
                }
            }

            // --- Tarjeta 2: Vista previa del nuevo archivo ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Ocupa el espacio sobrante
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (fileName.isEmpty()) "nuevo.c" else "$fileName.c",
                            style = MaterialTheme.typography.titleMedium
                        )
                        AssistChip(onClick = {}, label = { Text("Proyecto Nuvy") })
                        AssistChip(onClick = {}, label = { Text("Formato") })

                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = newFileCode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                    )
                }
            }

            // --- BOTONES INFERIORES ELIMINADOS ---
            // Se quitó la fila de botones "Volver al editor" y "Guardar"
            // porque era redundante, tal como mencionaste.
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
            onCreate = {}
            // Se eliminó 'onSave'
        )
    }
}