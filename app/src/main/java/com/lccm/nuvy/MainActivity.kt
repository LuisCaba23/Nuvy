package com.lccm.nuvy

import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lccm.nuvy.ui.theme.NuvyTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private fun saveFileToDownloads(data: ByteArray, fileName: String) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { fos ->
                fos.write(data)
            }

            runOnUiThread {
                Toast.makeText(
                    this,
                    "✅ Archivo guardado en Descargas/$fileName",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "❌ Error al guardar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NuvyTheme {
                val navController = rememberNavController()
                val viewModel: EditorViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return EditorViewModel(
                                onDownloadFile = { data, fileName ->
                                    saveFileToDownloads(data, fileName)
                                }
                            ) as T
                        }
                    }
                )

                NavHost(navController = navController, startDestination = "nuvy") {
                    composable("nuvy") {
                        NuvyScreen(
                            onConnectClicked = { /* TODO: Implementar conexión por cable */ },
                            onEditorClicked = { navController.navigate("editor") }
                        )
                    }
                    composable("editor") {
                        EditorScreen(
                            onNavigate = { route ->
                                if (route == "home") {
                                    navController.navigate("nuvy")
                                }
                            },
                            onOpenFile = { /* TODO: Implementar abrir archivo */ },
                            onNewFile = { /* TODO: Implementar nuevo archivo */ },
                            onSave = { /* TODO: Implementar guardar */ },
                            onCompile = { /* No usado, el botón llama directo al viewModel */ },
                            onUpload = { /* TODO: Implementar subir a Pico */ },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

// =================================================================================
// PANTALLA 1: NuvyScreen (Esta parte no cambia)
// =================================================================================
@Composable
fun NuvyScreen(
    onConnectClicked: () -> Unit,
    onEditorClicked: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Nuvy", fontSize = 48.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Edita un archivo .c, súbelo a la nube y recibirás un archivo .uf2.",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onConnectClicked,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = "Conectar por cable", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onEditorClicked,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = "Ir al editor", fontSize = 16.sp)
            }
        }
    }
}

// --- Vista Previa (No cambia) ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NuvyScreenPreview() {
    NuvyTheme {
        NuvyScreen(
            onConnectClicked = {},
            onEditorClicked = {}
        )
    }
}
