package pt.ua.icm.refinder.ui.screens.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ua.icm.refinder.ui.theme.*

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    viewModel: HelpSupportViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RefinderBackground)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = RefinderSurface,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Help & Support",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Assistente IA do ReFinder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RefinderTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(viewModel.messages) { message ->
                ChatBubble(message)
            }

            if (viewModel.isLoading) {
                item {
                    ChatBubble(
                        SupportMessage(
                            text = "A escrever...",
                            isUser = false
                        )
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.inputText,
                onValueChange = viewModel::onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escreve a tua dúvida...") },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = RefinderAccent,
                    unfocusedBorderColor = Color(0xFF2A3145),
                    cursorColor = RefinderAccent,
                    focusedContainerColor = RefinderSurface,
                    unfocusedContainerColor = RefinderSurface,
                    focusedPlaceholderColor = RefinderTextMuted,
                    unfocusedPlaceholderColor = RefinderTextMuted
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = viewModel::sendMessage,
                enabled = !viewModel.isLoading,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = RefinderAccent,
                    contentColor = RefinderBackground,
                    disabledContainerColor = RefinderSurface,
                    disabledContentColor = RefinderTextMuted
                )
            ) {
                Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Enviar")
            }
        }
    }
}

@Composable
private fun ChatBubble(message: SupportMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 290.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isUser) 20.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) RefinderAccent else RefinderSurface
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!message.isUser) {
                    Icon(
                        imageVector = Icons.Outlined.SupportAgent,
                        contentDescription = null,
                        tint = RefinderAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = message.text,
                    color = if (message.isUser) RefinderBackground else Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
