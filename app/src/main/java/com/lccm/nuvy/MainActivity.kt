package com.lccm.nuvy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Link
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
import com.lccm.nuvy.ui.theme.NuvyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NuvyTheme {
                var currentScreen by remember { mutableStateOf(NuvyDestinations.HOME) }

                val navigateTo: (String) -> Unit = { screen ->
                    currentScreen = screen
                }

                when (currentScreen) {

                    // --- CAMBIO AQUÍ ---
                    // "HOME" ahora es la única pantalla que usa NuvyScreen.
                    // "CONNECT" y "EDITOR" te llevarán a las pantallas
                    // principales pero con la barra de navegación.
                    NuvyDestinations.HOME -> {
                        NuvyScreen(
                            onConnectClicked = { navigateTo(NuvyDestinations.CONNECT_DEVICE) },
                            onEditorClicked = { navigateTo(NuvyDestinations.EDITOR) }
                        )
                    }

                    // --- NUEVA LÓGICA DE NAVEGACIÓN ---
                    // Si el usuario presiona "Conectar" o "Editor" en la barra de navegación,
                    // lo llevamos a las pantallas correspondientes.
                    NuvyDestinations.CONNECT -> {
                        ConnectDeviceScreen(
                            onPermitAccess = { navigateTo(NuvyDestinations.ACCESS_GRANTED) },
                            onTryAgain = {},
                            onNavigate = { screenName -> navigateTo(screenName) }
                        )
                    }
                    NuvyDestinations.EDITOR -> {
                        EditorScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onOpenFile = { navigateTo(NuvyDestinations.OPEN_FILE) },
                            onNewFile = { navigateTo(NuvyDestinations.NEW_FILE) },
                            onSave = { navigateTo(NuvyDestinations.FILE_SAVED) },
                            onCompile = { navigateTo(NuvyDestinations.BUILD_PROCESS) },
                            onUpload = { navigateTo(NuvyDestinations.OPEN_FILE) }
                        )
                    }

                    NuvyDestinations.CONNECT_DEVICE -> {
                        ConnectDeviceScreen(
                            onPermitAccess = { navigateTo(NuvyDestinations.ACCESS_GRANTED) },
                            onTryAgain = {},
                            onNavigate = { screenName -> navigateTo(screenName) }
                        )
                    }

                    NuvyDestinations.ACCESS_GRANTED -> {
                        AccessGrantedScreen(
                            onContinueClicked = { navigateTo(NuvyDestinations.UPLOAD_PROGRESS) },
                            onNavigate = { screenName -> navigateTo(screenName) }
                        )
                    }

                    NuvyDestinations.OPEN_FILE -> {
                        OpenFileScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onOpen = { /*TODO*/ },
                            onImport = { /*TODO*/ },
                            onFileClick = { navigateTo(NuvyDestinations.FILE_PREVIEW) }
                        )
                    }

                    NuvyDestinations.FILE_PREVIEW -> {
                        FilePreviewScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onGoToEditor = { navigateTo(NuvyDestinations.EDITOR) },
                            onGenerateUf2 = { navigateTo(NuvyDestinations.BUILD_PROCESS) }
                        )
                    }

                    NuvyDestinations.NEW_FILE -> {
                        NewFileScreen(
                            onGoBack = { navigateTo(NuvyDestinations.EDITOR) },
                            onCreate = { navigateTo(NuvyDestinations.EDITOR) }
                        )
                    }

                    NuvyDestinations.FILE_SAVED -> {
                        FileSavedScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onGoBackToEditor = { navigateTo(NuvyDestinations.EDITOR) },
                            onTryAgain = { /*TODO*/ }
                        )
                    }

                    NuvyDestinations.BUILD_PROCESS -> {
                        BuildScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onGoBack = { navigateTo(NuvyDestinations.EDITOR) },
                            onCancelBuild = { navigateTo(NuvyDestinations.EDITOR) },
                            onBuildComplete = { navigateTo(NuvyDestinations.DOWNLOAD_READY) }
                        )
                    }

                    NuvyDestinations.DOWNLOAD_READY -> {
                        DownloadReadyScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onGoBack = { navigateTo(NuvyDestinations.EDITOR) },
                            onUpload = { navigateTo(NuvyDestinations.CONNECT_DEVICE) },
                            onSave = { /*TODO: Lógica de guardado*/ }
                        )
                    }

                    // --- BLOQUE ACTUALIZADO ---
                    NuvyDestinations.UPLOAD_PROGRESS -> {
                        UploadScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onGoBack = { navigateTo(NuvyDestinations.EDITOR) },
                            onCancelUpload = { navigateTo(NuvyDestinations.EDITOR) },
                            onUploadFinished = { navigateTo(NuvyDestinations.UPLOAD_COMPLETE) }
                        )
                    }

                    NuvyDestinations.UPLOAD_COMPLETE -> {
                        UploadCompleteScreen(
                            onNavigate = { screenName -> navigateTo(screenName) },
                            onGoBack = { navigateTo(NuvyDestinations.EDITOR) },
                            // --- CAMBIO AQUÍ ---
                            // "Finalizar" te lleva al menú principal (HOME)
                            onFinalize = { navigateTo(NuvyDestinations.HOME) }
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