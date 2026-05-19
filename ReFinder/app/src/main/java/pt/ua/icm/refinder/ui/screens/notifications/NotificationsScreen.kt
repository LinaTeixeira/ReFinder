package pt.ua.icm.refinder.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ua.icm.refinder.data.model.AppNotification
import pt.ua.icm.refinder.ui.theme.*

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onItemClick: (String) -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    var selectedNotification by remember { mutableStateOf<AppNotification?>(null) }

    if (selectedNotification != null) {
        val notification = selectedNotification!!
        AlertDialog(
            onDismissRequest = { selectedNotification = null },
            title = {
                Text(notification.title)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(notification.message)

                    if (notification.relatedItemTitle.isNotBlank()) {
                        Text(
                            text = "Anúncio: ${notification.relatedItemTitle}",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (notification.relatedItemType.isNotBlank()) {
                        Text(
                            text = "Tipo: ${
                                if (notification.relatedItemType == "found") "Achado" else "Perdido"
                            }"
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.markAsRead(notification.id)
                        notification.relatedItemId?.let {
                            onItemClick(it)
                        }
                        selectedNotification = null
                    }
                ) {
                    Text("Ver anúncio")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.markAsRead(notification.id)
                        selectedNotification = null
                    }
                ) {
                    Text("Fechar")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RefinderBackground),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 18.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        text = "Notificações",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Atualizações sobre os teus pedidos.",
                        color = RefinderTextMuted
                    )
                }
            }
        }

        viewModel.errorMessage?.let { error ->
            item {
                Text("Erro: $error", color = MaterialTheme.colorScheme.error)
            }
        }

        if (viewModel.notifications.isEmpty()) {
            item {
                EmptyNotificationsCard()
            }
        } else {
            items(viewModel.notifications) { notification ->
                NotificationCard(
                    notification = notification,
                    onClick = {
                        selectedNotification = notification
                    }
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val icon = when (notification.type) {
        "claim_approved" -> Icons.Outlined.CheckCircle
        "claim_rejected" -> Icons.Outlined.ErrorOutline
        "match_found" -> Icons.Outlined.Search
        else -> Icons.Outlined.Notifications
    }

    val accent = when (notification.type) {
        "claim_approved" -> FoundColor
        "claim_rejected" -> LostColor
        "match_found" -> RefinderAccent
        else -> RefinderAccent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                RefinderSurface
            } else {
                RefinderAccent.copy(alpha = 0.12f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = accent.copy(alpha = 0.16f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = RefinderAccent,
                                    shape = RoundedCornerShape(999.dp)
                                )
                        )
                    }
                }

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RefinderTextMuted
                )
            }
        }
    }
}

@Composable
private fun EmptyNotificationsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = RefinderSurface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Ainda não tens notificações.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Quando houver novidades sobre os teus pedidos, aparecem aqui.",
                style = MaterialTheme.typography.bodyMedium,
                color = RefinderTextMuted
            )
        }
    }
}
