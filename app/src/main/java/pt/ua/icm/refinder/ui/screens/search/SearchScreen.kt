package pt.ua.icm.refinder.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ua.icm.refinder.data.model.LostItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel()
) {
    val filteredItems by viewModel.filteredItems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Pesquisar Itens",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 1. Campo de Texto de Pesquisa
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            label = { Text("O que procuras?") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Seleção de Tipo (Perdido / Achado)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterButton(
                label = "Perdidos",
                isSelected = viewModel.selectedType == "lost",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onTypeChange("lost") }
            )
            FilterButton(
                label = "Achados",
                isSelected = viewModel.selectedType == "found",
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onTypeChange("found") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Lista de Resultados
        if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sem resultados encontrados.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredItems) { item ->
                    ItemCard(item)
                }
            }
        }
    }
}

@Composable
fun FilterButton(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(label)
    }
}

@Composable
fun ItemCard(item: LostItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = if (item.type == "lost") "PERDIDO" else "ACHADO",
                    color = if (item.type == "lost") Color.Red else Color(0xFF4CAF50),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(text = item.description, maxLines = 2, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "📍 ${item.locationName}", style = MaterialTheme.typography.bodySmall)
        }
    }
}