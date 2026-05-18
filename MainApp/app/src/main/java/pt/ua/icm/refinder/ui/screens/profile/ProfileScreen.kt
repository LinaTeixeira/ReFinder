package pt.ua.icm.refinder.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import pt.ua.icm.refinder.ui.theme.*

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RefinderBackground),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 20.dp,
            bottom = 110.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. HEADER PREMIUM
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = RefinderSurface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Surface(
                                modifier = Modifier.size(84.dp),
                                shape = CircleShape,
                                color = RefinderAccent.copy(alpha = 0.18f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = RefinderAccent,
                                    modifier = Modifier.padding(22.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(18.dp)
                                    .background(
                                        color = FoundColor,
                                        shape = CircleShape
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        Column {
                            Text(
                                text = "O meu perfil",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Text(
                                text = viewModel.userEmail,
                                color = RefinderTextMuted
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Membro ReFinder",
                                color = RefinderAccent,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PremiumStatCard(
                            value = viewModel.totalItems.toString(),
                            label = "Itens",
                            color = RefinderAccent,
                            modifier = Modifier.weight(1f)
                        )

                        PremiumStatCard(
                            value = viewModel.lostItemsCount.toString(),
                            label = "Perdidos",
                            color = LostColor,
                            modifier = Modifier.weight(1f)
                        )

                        PremiumStatCard(
                            value = viewModel.foundItemsCount.toString(),
                            label = "Achados",
                            color = FoundColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. MENU PREMIUM
        item {
            SectionTitle("A minha atividade")
        }

        item {
            MenuCard(
                icon = Icons.Outlined.Inventory2,
                title = "Os meus itens",
                onClick = {
                    navController.navigate("myItems")
                }
            )

            MenuCard(
                icon = Icons.Outlined.Lock,
                title = "Smart Lockers",
                onClick = { /* TODO */ }
            )

            MenuCard(
                icon = Icons.Outlined.Notifications,
                title = "Notificações",
                badge = viewModel.unreadNotificationsCount.takeIf { it > 0 }?.toString(),
                onClick = {
                    navController.navigate("notifications")
                }
            )

            if (viewModel.isAdmin) {
                MenuCard(
                    icon = Icons.Outlined.AdminPanelSettings,
                    title = "Admin Panel",
                    onClick = {
                        navController.navigate("adminClaims")
                    }
                )

                MenuCard(
                    icon = Icons.Outlined.Password,
                    title = "Validar PIN de levantamento",
                    onClick = {
                        navController.navigate("adminPinValidation")
                    }
                )

                MenuCard(
                    icon = Icons.Outlined.QrCodeScanner,
                    title = "Validar QR Code",
                    onClick = {
                        navController.navigate("adminQrValidation")
                    }
                )

                MenuCard(
                    icon = Icons.Outlined.History,
                    title = "Histórico de levantamentos",
                    onClick = {
                        navController.navigate("adminPickupHistory")
                    }
                )
            }

            MenuCard(
                icon = Icons.Outlined.HelpOutline,
                title = "Help & Support",
                onClick = {
                    navController.navigate("helpSupport")
                }
            )
        }

        // 3. LOGOUT PREMIUM
        item {
            Button(
                onClick = {
                    viewModel.logout {
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A1212)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = Color(0xFFFF7A7A)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Terminar sessão",
                    color = Color(0xFFFF7A7A)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String
) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = RefinderTextMuted,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun PremiumStatCard(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF202638)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                color = RefinderTextMuted,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun MenuCard(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = RefinderSurface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = RefinderAccent.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RefinderAccent,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            if (badge != null) {
                Surface(
                    shape = CircleShape,
                    color = RefinderAccent
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        color = RefinderBackground,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = RefinderTextMuted
            )
        }
    }
}
