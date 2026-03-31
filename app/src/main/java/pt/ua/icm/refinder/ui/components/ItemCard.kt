package pt.ua.icm.refinder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.ua.icm.refinder.data.model.LostItem

@Composable
fun ItemCard(item: LostItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Tipo: ${if (item.type == "lost") "Perdido" else "Achado"}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Data: ${item.date}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Local: ${item.locationName}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}