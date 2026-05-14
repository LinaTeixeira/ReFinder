package pt.ua.icm.refinder.ui.screens.locker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LockerSelectionScreen(
    itemId: String,
    onBack: () -> Unit,
    viewModel: LockerViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = onBack) {
            Text("Voltar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Escolher cacifo disponível",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        viewModel.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        viewModel.generatedPin?.let { pin ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Item depositado com sucesso!")
                    Text("PIN de levantamento: $pin")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.lockers) { locker ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.depositItem(itemId, locker.id)
                    }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(locker.name, style = MaterialTheme.typography.titleMedium)
                        Text(locker.locationName)
                        Text("Disponível")
                    }
                }
            }
        }
    }
}