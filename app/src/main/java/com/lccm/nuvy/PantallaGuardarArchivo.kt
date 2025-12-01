package com.lccm.nuvy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.lccm.nuvy.components.NuvyBottomNavBar
import com.lccm.nuvy.ui.theme.NuvyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSavedScreen(
    onNavigate: (String) -> Unit,
    onGoBackToEditor: () -> Unit,
    fileName: String
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Archivo guardado") })
        },
        bottomBar = {
            NuvyBottomNavBar(
                currentDestination = NuvyDestinations.EDITOR,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícono de éxito
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Guardado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Guardado exitoso",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tu archivo $fileName fue guardado en Descargas.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(label = "Archivo", value = fileName)
                InfoRow(label = "Destino", value = "Descargas")
            }

            Spacer(modifier = Modifier.weight(1f))

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
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FileSavedScreenPreview() {
    NuvyTheme {
        FileSavedScreen(
            onNavigate = {},
            onGoBackToEditor = {},
            fileName = "main.c"
        )
    }
}