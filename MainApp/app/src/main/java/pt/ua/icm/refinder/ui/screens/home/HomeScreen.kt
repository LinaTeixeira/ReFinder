package pt.ua.icm.refinder.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ua.icm.refinder.ui.components.ItemCard
import pt.ua.icm.refinder.ui.theme.ReFinderTheme
import pt.ua.icm.refinder.ui.theme.RefinderAccent
import pt.ua.icm.refinder.ui.theme.RefinderBackground
import pt.ua.icm.refinder.ui.theme.RefinderSurface
import pt.ua.icm.refinder.ui.theme.RefinderTextMuted


@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    onItemClick: (String) -> Unit
) {
    val items = homeViewModel.items
    val isLoading = homeViewModel.isLoading
    val errorMessage = homeViewModel.errorMessage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RefinderBackground)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = RefinderAccent
                )
            }

            errorMessage != null -> {
                Text(
                    text = "Erro: $errorMessage",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 22.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        HomeHeader()
                    }

                    item {
                        HomeStats(
                            total = items.size,
                            found = items.count { it.type == "found" },
                            deposited = items.count { it.status == "deposited" }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Itens recentes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Vê os últimos objetos registados na comunidade.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RefinderTextMuted
                        )
                    }

                    if (items.isEmpty()) {
                        item {
                            EmptyStateCard()
                        }
                    } else {
                        items(items) { item ->
                            ItemCard(
                                item = item,
                                onClick = { onItemClick(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "ReFinder",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        Text(
            text = "Perdeste algo? A comunidade pode ajudar-te a encontrar.",
            style = MaterialTheme.typography.bodyLarge,
            color = RefinderTextMuted
        )
    }
}

@Composable
private fun HomeStats(
    total: Int,
    found: Int,
    deposited: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            title = "Itens",
            value = total.toString(),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "Achados",
            value = found.toString(),
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "Cacifos",
            value = deposited.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = RefinderSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RefinderAccent
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = RefinderTextMuted
            )
        }
    }
}

@Composable
private fun EmptyStateCard() {
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
                text = "Ainda não existem itens registados.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Quando alguém registar um objeto perdido ou achado, ele vai aparecer aqui.",
                style = MaterialTheme.typography.bodyMedium,
                color = RefinderTextMuted
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ReFinderTheme {
        HomeScreen(onItemClick = {})
    }
}
