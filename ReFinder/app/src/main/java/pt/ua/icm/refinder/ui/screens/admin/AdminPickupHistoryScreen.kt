package pt.ua.icm.refinder.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminPickupHistoryScreen(
    onBack: () -> Unit,
    viewModel: AdminPickupHistoryViewModel = viewModel()
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RefinderBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
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
                        text = "Histórico de levantamentos",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Itens já entregues aos utilizadores.",
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

        if (viewModel.items.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = RefinderSurface)
                ) {
                    Text(
                        text = "Ainda não existem levantamentos concluídos.",
                        modifier = Modifier.padding(18.dp),
                        color = RefinderTextMuted
                    )
                }
            }
        } else {
            items(viewModel.items) { item ->
                PickupHistoryCard(item)
            }
        }
    }
}

@Composable
private fun PickupHistoryCard(item: LostItem) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = RefinderSurface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = FoundColor.copy(alpha = 0.16f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = FoundColor,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Cacifo: ${item.lockerId ?: "Indisponível"}",
                    color = RefinderTextMuted
                )

                Text(
                    text = "Levantado por: ${item.pickedUpByUserEmail ?: item.pickedUpByUserId ?: "Indisponível"}",
                    color = RefinderTextMuted
                )

                Text(
                    text = "Data: ${formatTimestamp(item.pickedUpAt)}",
                    color = RefinderTextMuted
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return "Indisponível"

    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
