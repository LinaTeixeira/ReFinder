package pt.ua.icm.refinder.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pt.ua.icm.refinder.data.model.Claim
import pt.ua.icm.refinder.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClaimsScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: AdminClaimsViewModel = viewModel()
) {
    var claimToConfirm by remember { mutableStateOf<Claim?>(null) }
    var actionToConfirm by remember { mutableStateOf<String?>(null) }

    if (claimToConfirm != null && actionToConfirm != null) {
        val isApprove = actionToConfirm == "approved"

        AlertDialog(
            onDismissRequest = {
                claimToConfirm = null
                actionToConfirm = null
            },
            title = {
                Text(
                    text = if (isApprove) "Aprovar reclamação?" else "Rejeitar reclamação?"
                )
            },
            text = {
                Text(
                    text = if (isApprove) {
                        "Tens a certeza que queres aprovar este pedido? O item será marcado como recuperado."
                    } else {
                        "Tens a certeza que queres rejeitar este pedido?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val claim = claimToConfirm ?: return@TextButton

                        if (isApprove) {
                            viewModel.approveClaim(
                                claimId = claim.id,
                                itemId = claim.itemId
                            )
                        } else {
                            viewModel.rejectClaim(
                                claimId = claim.id
                            )
                        }

                        claimToConfirm = null
                        actionToConfirm = null
                    }
                ) {
                    Text(if (isApprove) "Sim, aprovar" else "Sim, rejeitar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        claimToConfirm = null
                        actionToConfirm = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = RefinderBackground,
        topBar = {
            TopAppBar(
                title = { Text("Gestão de Reclamações", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RefinderBackground)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(RefinderBackground)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = RefinderAccent)
            } else if (viewModel.claims.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.History, contentDescription = null, tint = RefinderTextMuted, modifier = Modifier.size(48.dp))
                    Text("Sem reclamações pendentes", color = RefinderTextMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(viewModel.claims) { claim ->
                        ClaimAdminCard(
                            claim = claim,
                            onApprove = {
                                claimToConfirm = claim
                                actionToConfirm = "approved"
                            },
                            onReject = {
                                claimToConfirm = claim
                                actionToConfirm = "rejected"
                            },
                            onViewItem = {
                                navController.navigate("itemDetail/${claim.itemId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClaimAdminCard(
    claim: Claim,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onViewItem: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = RefinderSurface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = claim.itemTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RefinderSurfaceLight
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Mensagem do reclamante:", style = MaterialTheme.typography.labelSmall, color = RefinderAccent)
                    Text(claim.message, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }

            Text(
                text = "Reclamante: ${claim.claimantEmail}",
                style = MaterialTheme.typography.labelSmall,
                color = RefinderTextMuted
            )

            OutlinedButton(
                onClick = onViewItem,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RefinderAccent)
            ) {
                Text("Ver anúncio")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A1212), contentColor = Color(0xFFFF7A7A))
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rejeitar")
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RefinderAccent.copy(alpha = 0.14f), contentColor = RefinderAccent)
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Aprovar")
                }
            }
        }
    }
}
