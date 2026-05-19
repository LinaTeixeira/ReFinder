package pt.ua.icm.refinder.ui.screens.locker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ua.icm.refinder.data.model.Locker
import pt.ua.icm.refinder.ui.theme.RefinderAccent
import pt.ua.icm.refinder.ui.theme.RefinderBackground
import pt.ua.icm.refinder.ui.theme.RefinderSurface
import pt.ua.icm.refinder.ui.theme.RefinderSurfaceLight
import pt.ua.icm.refinder.ui.theme.RefinderTextMuted
import pt.ua.icm.refinder.ui.theme.SuccessColor


@Composable
fun LockerSelectionScreen(
    itemId: String,
    onBack: () -> Unit,
    viewModel: LockerViewModel = viewModel()
) {
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
            Header(onBack = onBack)
        }

        item {
            IntroCard()
        }

        viewModel.errorMessage?.let { error ->
            item {
                Text(
                    text = "Erro: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        viewModel.generatedPin?.let { pin ->
            item {
                SuccessCard(pin = pin)
            }
        }

        if (viewModel.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RefinderAccent)
                }
            }
        }

        item {
            Text(
                text = "Cacifos disponíveis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "${viewModel.lockers.size} cacifo${if (viewModel.lockers.size == 1) "" else "s"} livre${if (viewModel.lockers.size == 1) "" else "s"} neste momento",
                style = MaterialTheme.typography.bodyMedium,
                color = RefinderTextMuted
            )
        }

        if (viewModel.lockers.isEmpty() && !viewModel.isLoading) {
            item {
                EmptyLockerCard()
            }
        } else {
            items(viewModel.lockers) { locker ->
                LockerCard(
                    locker = locker,
                    enabled = !viewModel.isLoading && viewModel.generatedPin == null,
                    onClick = {
                        viewModel.depositItem(itemId, locker.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = RefinderSurface,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Voltar"
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = "Smart Lockers",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Deposita o item em segurança.",
                style = MaterialTheme.typography.bodyMedium,
                color = RefinderTextMuted
            )
        }
    }
}

@Composable
private fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = RefinderAccent.copy(alpha = 0.14f)
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = RefinderSurface
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = RefinderAccent,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Entrega sem contacto",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Escolhe um cacifo livre. A app gera automaticamente um PIN de levantamento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RefinderTextMuted
                )
            }
        }
    }
}

@Composable
private fun SuccessCard(pin: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SuccessColor.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = SuccessColor,
                modifier = Modifier.size(34.dp)
            )

            Text(
                text = "Item depositado com sucesso!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = RefinderSurface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Password,
                        contentDescription = null,
                        tint = RefinderAccent
                    )

                    Text(
                        text = pin,
                        style = MaterialTheme.typography.headlineSmall,
                        color = RefinderAccent,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Text(
                text = "Guarda este PIN. Ele também vai aparecer no detalhe do item.",
                style = MaterialTheme.typography.bodySmall,
                color = RefinderTextMuted
            )
        }
    }
}

@Composable
private fun LockerCard(
    locker: Locker,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = RefinderSurface,
            disabledContainerColor = RefinderSurface.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = RefinderSurfaceLight
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = RefinderAccent,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = locker.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = RefinderTextMuted,
                        modifier = Modifier.size(15.dp)
                    )

                    Text(
                        text = locker.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = RefinderTextMuted
                    )
                }
            }

            AvailabilityBadge()
        }
    }
}

@Composable
private fun AvailabilityBadge() {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = SuccessColor.copy(alpha = 0.16f)
    ) {
        Text(
            text = "Livre",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = SuccessColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyLockerCard() {
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
                text = "Não há cacifos disponíveis.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Todos os cacifos estão ocupados neste momento. Tenta novamente mais tarde.",
                style = MaterialTheme.typography.bodyMedium,
                color = RefinderTextMuted
            )
        }
    }
}
