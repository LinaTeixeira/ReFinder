package pt.ua.icm.refinder.ui.screens.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import pt.ua.icm.refinder.data.model.ItemMatch
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.data.model.findPossibleMatches
import pt.ua.icm.refinder.data.repository.FirebaseItemRepository
import pt.ua.icm.refinder.ui.components.generateQrCodeBitmap
import pt.ua.icm.refinder.ui.theme.FoundColor
import pt.ua.icm.refinder.ui.theme.LostColor
import pt.ua.icm.refinder.ui.theme.RefinderAccent
import pt.ua.icm.refinder.ui.theme.RefinderBackground
import pt.ua.icm.refinder.ui.theme.RefinderSurface
import pt.ua.icm.refinder.ui.theme.RefinderSurfaceLight
import pt.ua.icm.refinder.ui.theme.RefinderTextMuted

@Composable
fun ItemDetailScreen(
    itemId: String,
    onBack: () -> Unit,
    onDepositInLocker: (String) -> Unit,
    onMatchClick: (String) -> Unit
) {
    val repository = remember { FirebaseItemRepository() }

    var item by remember { mutableStateOf<LostItem?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var matches by remember { mutableStateOf<List<ItemMatch>>(emptyList()) }

    LaunchedEffect(itemId) {
        repository.getItemById(
            itemId = itemId,
            onSuccess = { loadedItem ->
                item = loadedItem
                repository.observeItems(
                    onDataChanged = { allItems ->
                        matches = findPossibleMatches(loadedItem, allItems)
                    },
                    onError = { }
                )
            },
            onFailure = { error = it.message }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RefinderBackground)
    ) {
        when {
            error != null -> {
                Text(
                    text = "Erro: $error",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = Color.White
                )
            }

            item == null -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = RefinderAccent
                )
            }

            else -> {
                ItemDetailContent(
                    item = item!!,
                    matches = matches,
                    onBack = onBack,
                    onDepositInLocker = onDepositInLocker,
                    onMatchClick = onMatchClick
                )
            }
        }
    }
}

@Composable
private fun ItemDetailContent(
    item: LostItem,
    matches: List<ItemMatch>,
    onBack: () -> Unit,
    onDepositInLocker: (String) -> Unit,
    onMatchClick: (String) -> Unit
) {
    val context = LocalContext.current
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }
    var isImageExpanded by remember { mutableStateOf(false) }

    var showClaimDialog by remember { mutableStateOf(false) }
    var claimMessage by remember { mutableStateOf("") }
    var claimLoading by remember { mutableStateOf(false) }

    if (showClaimDialog) {
        AlertDialog(
            onDismissRequest = { showClaimDialog = false },
            title = {
                Text("Reclamar item")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Explica porque achas que este item é teu.")

                    OutlinedTextField(
                        value = claimMessage,
                        onValueChange = { claimMessage = it },
                        label = { Text("Mensagem") },
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        claimLoading = true

                        FirebaseItemRepository().createClaim(
                            item = item,
                            message = claimMessage,
                            onSuccess = {
                                claimLoading = false
                                showClaimDialog = false
                                claimMessage = ""
                                Toast.makeText(context, "Pedido enviado com sucesso.", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = {
                                claimLoading = false
                                Toast.makeText(context, it.message ?: "Erro ao enviar pedido.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = !claimLoading && claimMessage.isNotBlank()
                ) {
                    Text("Enviar pedido")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClaimDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (isImageExpanded && item.imageUrl.isNotBlank()) {
        FullscreenImageDialog(
            imageUrl = item.imageUrl,
            title = item.title,
            onDismiss = { isImageExpanded = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
    ) {
        HeroSection(
            item = item,
            onBack = onBack,
            onImageClick = { isImageExpanded = true }
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DetailHeader(item)

            DescriptionCard(item.description)

            InfoCard(
                title = "Detalhes",
                rows = listOf(
                    DetailRowData("Tipo", if (item.type == "lost") "Perdido" else "Achado"),
                    DetailRowData("Categoria", item.category.ifBlank { "Outro" }),
                    DetailRowData("Data", item.date.ifBlank { "Sem data" }),
                    DetailRowData("Local", item.locationName.ifBlank { "Local não definido" })
                )
            )

            if (item.latitude != null && item.longitude != null) {
                PrimaryActionButton(
                    text = "Abrir localização",
                    icon = Icons.Outlined.Map,
                    onClick = {
                        val uri = Uri.parse(
                            "geo:${item.latitude},${item.longitude}?q=${item.latitude},${item.longitude}(${Uri.encode(item.locationName)})"
                        )
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                )
            }

            if (item.type == "found" && item.userId != currentUserId && item.status in listOf("reported", "deposited")) {
                PrimaryActionButton(
                    text = "Acho que este item é meu",
                    icon = Icons.Outlined.CheckCircle,
                    onClick = { showClaimDialog = true }
                )
            }

            if (item.type == "found" && item.status == "reported" && item.userId == currentUserId) {
                PrimaryActionButton(
                    text = "Depositar em cacifo",
                    icon = Icons.Outlined.Lock,
                    onClick = { onDepositInLocker(item.id) }
                )
            }

            if (item.status == "ready_for_pickup" && item.claimedByUserId == currentUserId) {
                LockerPickupCard(item)
            }

            if (matches.isNotEmpty() && item.type == "lost" && item.userId == currentUserId) {
                PossibleMatchesSection(
                    matches = matches,
                    onMatchClick = onMatchClick
                )
            }
        }
    }
}

@Composable
private fun HeroSection(
    item: LostItem,
    onBack: () -> Unit,
    onImageClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        if (item.imageUrl.isNotBlank()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onImageClick() },
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(RefinderSurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sem imagem",
                    color = RefinderTextMuted,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(999.dp))
                .background(RefinderSurface.copy(alpha = 0.88f))
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }

        TypeBadge(
            type = item.type,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusBadge(item.status)

            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DetailHeader(item: LostItem) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = item.category.ifBlank { "Outro" },
            color = RefinderAccent,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallInfoPill(
                icon = Icons.Outlined.Schedule,
                text = item.date.ifBlank { "Sem data" }
            )

            SmallInfoPill(
                icon = Icons.Outlined.LocationOn,
                text = item.locationName.ifBlank { "Sem local" }
            )
        }
    }
}

@Composable
private fun DescriptionCard(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = RefinderSurface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Descrição",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description.ifBlank { "Sem descrição disponível." },
                style = MaterialTheme.typography.bodyMedium,
                color = RefinderTextMuted
            )
        }
    }
}

private data class DetailRowData(
    val label: String,
    val value: String
)

@Composable
private fun InfoCard(
    title: String,
    rows: List<DetailRowData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = RefinderSurface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = row.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = RefinderTextMuted
                    )

                    Text(
                        text = row.value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PossibleMatchesSection(
    matches: List<ItemMatch>,
    onMatchClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = RefinderSurface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Possíveis correspondências",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Encontrámos itens que podem corresponder a este objeto.",
                style = MaterialTheme.typography.bodyMedium,
                color = RefinderTextMuted
            )

            matches.forEach { match ->
                MatchPreviewCard(
                    match = match,
                    onClick = { onMatchClick(match.item.id) }
                )
            }
        }
    }
}

@Composable
private fun MatchPreviewCard(
    match: ItemMatch,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = RefinderSurfaceLight
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = match.item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${match.score}%",
                    color = RefinderAccent,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = match.reason,
                style = MaterialTheme.typography.bodySmall,
                color = RefinderTextMuted
            )

            Text(
                text = "${match.item.category} · ${if (match.item.type == "lost") "Perdido" else "Achado"}",
                style = MaterialTheme.typography.labelMedium,
                color = RefinderAccent
            )
        }
    }
}

@Composable
private fun LockerPickupCard(item: LostItem) {
    val pin = item.pickupPin.orEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = RefinderAccent.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Pronto para levantamento",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Cacifo: ${item.lockerId ?: "Indisponível"}",
                style = MaterialTheme.typography.bodyMedium,
                color = RefinderTextMuted
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = RefinderSurface
            ) {
                Text(
                    text = pin.ifBlank { "PIN indisponível" },
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    color = RefinderAccent,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (pin.isNotBlank()) {
                val qrBitmap = remember(pin) {
                    generateQrCodeBitmap("REFINDER_PICKUP:$pin")
                }

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code do PIN",
                        modifier = Modifier
                            .padding(14.dp)
                            .size(210.dp)
                    )
                }
            }

            Text(
                text = "Mostra este QR Code ou introduz o PIN no cacifo.",
                style = MaterialTheme.typography.bodySmall,
                color = RefinderTextMuted
            )
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RefinderAccent,
            contentColor = RefinderBackground
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TypeBadge(
    type: String,
    modifier: Modifier = Modifier
) {
    val isLost = type == "lost"

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = if (isLost) LostColor.copy(alpha = 0.18f) else FoundColor.copy(alpha = 0.18f)
    ) {
        Text(
            text = if (isLost) "Perdido" else "Achado",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (isLost) LostColor else FoundColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val label = when (status) {
        "deposited" -> "No cacifo"
        "claimed" -> "Levantado"
        else -> "Registado"
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = RefinderSurface.copy(alpha = 0.9f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = RefinderAccent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SmallInfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = RefinderSurfaceLight
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RefinderTextMuted,
                modifier = Modifier.size(15.dp)
            )

            Text(
                text = text,
                color = RefinderTextMuted,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FullscreenImageDialog(
    imageUrl: String,
    title: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onDismiss() },
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = onDismiss,
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
