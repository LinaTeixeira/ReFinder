package pt.ua.icm.refinder.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pt.ua.icm.refinder.ui.components.ItemCard
import pt.ua.icm.refinder.ui.theme.RefinderBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyItemsScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {

    Scaffold(
        containerColor = RefinderBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Os meus itens",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RefinderBackground
                )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(RefinderBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            items(viewModel.userItems) { item ->

                ItemCard(
                    item = item,
                    onClick = {
                        navController.navigate("itemDetail/${item.id}")
                    }
                )
            }
        }
    }
}
