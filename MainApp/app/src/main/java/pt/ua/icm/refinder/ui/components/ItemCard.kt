package pt.ua.icm.refinder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.ua.icm.refinder.data.model.LostItem
import pt.ua.icm.refinder.ui.theme.FoundColor
import pt.ua.icm.refinder.ui.theme.LostColor
import pt.ua.icm.refinder.ui.theme.RefinderAccent
import pt.ua.icm.refinder.ui.theme.RefinderCard
import pt.ua.icm.refinder.ui.theme.RefinderTextMuted


@Composable
fun ItemCard(
    item: LostItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = RefinderCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (item.imageUrl.isNotBlank()) {
                Box {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )

                    TypeBadge(
                        type = item.type,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF202638)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sem imagem",
                        color = RefinderTextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(item.category)

                if (item.status == "deposited") {
                    LockerBadge()
                }

                if (item.imageUrl.isBlank()) {
                    TypeBadge(type = item.type)
                }
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = RefinderTextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            InfoRow(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = RefinderTextMuted
                    )
                },
                text = item.date
            )

            InfoRow(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = RefinderTextMuted
                    )
                },
                text = item.locationName
            )
        }
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
private fun CategoryBadge(category: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = RefinderAccent.copy(alpha = 0.14f)
    ) {
        Text(
            text = category.ifBlank { "Outro" },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = RefinderAccent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LockerBadge() {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0xFFFFD166).copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = Color(0xFFFFD166),
                modifier = Modifier.size(14.dp)
            )

            Text(
                text = "No cacifo",
                color = Color(0xFFFFD166),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(18.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Text(
            text = text.ifBlank { "Local não definido" },
            style = MaterialTheme.typography.bodySmall,
            color = RefinderTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
