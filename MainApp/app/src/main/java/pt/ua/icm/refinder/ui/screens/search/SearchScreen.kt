package pt.ua.icm.refinder.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ua.icm.refinder.data.model.searchCategories
import pt.ua.icm.refinder.ui.components.CompactItemCard
import pt.ua.icm.refinder.ui.theme.RefinderAccent
import pt.ua.icm.refinder.ui.theme.RefinderBackground
import pt.ua.icm.refinder.ui.theme.RefinderSurface
import pt.ua.icm.refinder.ui.theme.RefinderTextMuted


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onItemClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val filteredItems = viewModel.filteredItems

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RefinderBackground),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 22.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SearchHeader()
        }

        item {
            OutlinedTextField(
                value = viewModel.searchText,
                onValueChange = viewModel::onSearchTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search for items...") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = RefinderAccent,
                    unfocusedBorderColor = Color(0xFF2A3145),
                    focusedLabelColor = RefinderAccent,
                    unfocusedLabelColor = RefinderTextMuted,
                    cursorColor = RefinderAccent,
                    focusedContainerColor = RefinderSurface,
                    unfocusedContainerColor = RefinderSurface
                )
            )
        }

        item {
            TypeFilterRow(
                selectedType = viewModel.selectedType,
                onTypeChange = viewModel::onTypeChange
            )
        }

        item {
            CategoryFilterRow(
                selectedCategory = viewModel.selectedCategory,
                onCategoryChange = viewModel::onCategoryChange
            )
        }

        viewModel.errorMessage?.let { error ->
            item {
                Text(
                    text = "Erro: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            Text(
                text = "${filteredItems.size} results found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (filteredItems.isEmpty()) {
            item {
                EmptySearchCard()
            }
        } else {
            items(filteredItems) { item ->
                CompactItemCard(
                    item = item,
                    onClick = { onItemClick(item.id) }
                )
            }
        }
    }
}

@Composable
private fun SearchHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
private fun TypeFilterRow(
    selectedType: String,
    onTypeChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SearchChip(
            text = "Todos",
            selected = selectedType == "all",
            onClick = { onTypeChange("all") }
        )

        SearchChip(
            text = "Perdidos",
            selected = selectedType == "lost",
            onClick = { onTypeChange("lost") }
        )

        SearchChip(
            text = "Achados",
            selected = selectedType == "found",
            onClick = { onTypeChange("found") }
        )
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "CATEGORIAS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = RefinderTextMuted
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            searchCategories.forEach { category ->
                SearchChip(
                    text = category,
                    selected = selectedCategory == category,
                    onClick = { onCategoryChange(category) }
                )
            }
        }
    }
}

@Composable
private fun SearchChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        shape = RoundedCornerShape(999.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = RefinderAccent,
            selectedLabelColor = RefinderBackground,
            containerColor = Color(0xFF202638),
            labelColor = RefinderTextMuted
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color(0xFF2A3145),
            selectedBorderColor = RefinderAccent
        )
    )
}

@Composable
private fun EmptySearchCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = RefinderSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Nenhum item encontrado.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Experimenta mudar os filtros ou pesquisar por outro termo.",
                style = MaterialTheme.typography.bodyMedium,
                color = RefinderTextMuted
            )
        }
    }
}
