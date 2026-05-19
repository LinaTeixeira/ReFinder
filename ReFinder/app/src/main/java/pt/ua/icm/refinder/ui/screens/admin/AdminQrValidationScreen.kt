package pt.ua.icm.refinder.ui.screens.admin

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import pt.ua.icm.refinder.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQrValidationScreen(
    onBack: () -> Unit,
    viewModel: AdminQrValidationViewModel = viewModel()
) {
    var lockerExpanded by remember { mutableStateOf(false) }

    val qrLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        val content = result.contents
        if (!content.isNullOrBlank()) {
            viewModel.validateQr(content)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Aponta para o QR Code de levantamento")
                setBeepEnabled(true)
                setOrientationLocked(false)
            }

            qrLauncher.launch(options)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RefinderBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = RefinderSurface,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Validar QR Code",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Escolhe o cacifo e lê o QR do utilizador.",
                    color = RefinderTextMuted
                )
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RefinderSurface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = lockerExpanded,
                    onExpandedChange = { lockerExpanded = !lockerExpanded }
                ) {
                    OutlinedTextField(
                        value = viewModel.lockers.firstOrNull {
                            it.id == viewModel.selectedLockerId
                        }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cacifo") },
                        placeholder = { Text("Escolhe o cacifo") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            ),
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = lockerExpanded)
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = refinderTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = lockerExpanded,
                        onDismissRequest = { lockerExpanded = false }
                    ) {
                        viewModel.lockers.forEach { locker ->
                            DropdownMenuItem(
                                text = { Text("${locker.name} · ${locker.locationName}") },
                                onClick = {
                                    viewModel.onLockerChange(locker.id)
                                    lockerExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RefinderAccent,
                        contentColor = RefinderBackground
                    )
                ) {
                    Icon(Icons.Outlined.QrCodeScanner, contentDescription = null)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Ler QR Code",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        viewModel.successMessage?.let {
            FeedbackCard(message = it, success = true)
        }

        viewModel.errorMessage?.let {
            FeedbackCard(message = it, success = false)
        }
    }
}

@Composable
private fun refinderTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = RefinderAccent,
    unfocusedBorderColor = Color(0xFF2A3145),
    focusedLabelColor = RefinderAccent,
    unfocusedLabelColor = RefinderTextMuted,
    cursorColor = RefinderAccent,
    focusedContainerColor = RefinderSurfaceLight,
    unfocusedContainerColor = RefinderSurfaceLight,
    focusedPlaceholderColor = RefinderTextMuted,
    unfocusedPlaceholderColor = RefinderTextMuted
)

@Composable
private fun FeedbackCard(
    message: String,
    success: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (success) {
                FoundColor.copy(alpha = 0.15f)
            } else {
                LostColor.copy(alpha = 0.15f)
            }
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = if (success) FoundColor else LostColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
