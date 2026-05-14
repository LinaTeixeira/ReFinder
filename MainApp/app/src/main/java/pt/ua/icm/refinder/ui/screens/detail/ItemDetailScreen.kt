package pt.ua.icm.refinder.ui.screens.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository
import pt.ua.icm.refinder.ui.components.generateQrCodeBitmap

@Composable
fun ItemDetailScreen(
    itemId: String,
    onBack: () -> Unit,
    onDepositInLocker: (String) -> Unit
) {
    val repository = remember { FirebaseItemRepository() }

    var item by remember { mutableStateOf<LostItem?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(itemId) {
        repository.getItemById(
            itemId = itemId,
            onSuccess = { item = it },
            onFailure = { error = it.message }
        )
    }

    when {
        error != null -> Text("Erro: $error", modifier = Modifier.padding(16.dp))
        item == null -> Box(Modifier.fillMaxSize()) { CircularProgressIndicator() }
        else -> ItemDetailContent(item = item!!, onBack = onBack, onDepositInLocker = onDepositInLocker)
    }
}

@Composable
private fun ItemDetailContent(
    item: LostItem,
    onBack: () -> Unit,
    onDepositInLocker: (String) -> Unit
) {
    val context = LocalContext.current
    var isImageExpanded by remember { mutableStateOf(false) }

    if (isImageExpanded && item.imageUrl.isNotBlank()) {
        Dialog(
            onDismissRequest = { isImageExpanded = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { isImageExpanded = false },
                        contentScale = ContentScale.Fit
                    )
                    
                    IconButton(
                        onClick = { isImageExpanded = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Button(onClick = onBack) {
            Text("Voltar")
        }

        if (item.imageUrl.isNotBlank()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clickable { isImageExpanded = true },
                contentScale = ContentScale.Crop
            )
        }

        Text(item.title, style = MaterialTheme.typography.headlineSmall)
        Text("Categoria: ${item.category}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(item.description, style = MaterialTheme.typography.bodyLarge)

        Text("Tipo: ${if (item.type == "lost") "Perdido" else "Achado"}")
        Text("Data: ${item.date}")
        Text("Local: ${item.locationName}")

        if (item.latitude != null && item.longitude != null) {
            Button(
                onClick = {
                    val uri = Uri.parse(
                        "geo:${item.latitude},${item.longitude}?q=${item.latitude},${item.longitude}(${Uri.encode(item.locationName)})"
                    )
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Abrir no Google Maps")
            }
        }

        if (item.type == "found" && item.status == "reported") {
            Button(
                onClick = { onDepositInLocker(item.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Depositar em cacifo")
            }
        }

        if (item.status == "deposited") {
            val pin = item.pickupPin ?: ""

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Item depositado em cacifo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text("Cacifo: ${item.lockerId ?: "Indisponível"}")

                    Text(
                        text = "PIN: $pin",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    if (pin.isNotBlank()) {
                        val qrBitmap = remember(pin) {
                            generateQrCodeBitmap("REFINDER_PICKUP:$pin")
                        }

                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code do PIN",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }
        }
    }
}
